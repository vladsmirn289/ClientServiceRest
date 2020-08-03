package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.*;
import com.shop.ClientServiceRest.Repository.ClientRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ClientServiceTest {
    @Autowired
    private ClientService clientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ClientRepo clientRepo;

    private Client client;

    @BeforeEach
    public void init() {
        this.client = new Client("f@f","123456", "ABC", "DEF", "A");

        Category books = new Category("Books");
        Category book = new Category("Book", books);
        Item item = new Item("item", 30L, 3D
                , 600D, "123");
        item.setDescription("description...");
        item.setCharacteristics("characteristics...");
        item.setCategory(book);
        ClientItem clientItem = new ClientItem(item, 3);
        ClientItem clientItem1 = new ClientItem(item, 4);
        Contacts contacts = new Contacts("123456", "Country", "City", "Street", "PhoneNumber");
        Order order = new Order(new HashSet<>(Arrays.asList(clientItem, clientItem1)), contacts, "C.O.D");

        this.client.setBasket(new HashSet<>(Arrays.asList(clientItem, clientItem1)));
        this.client.setOrders(Collections.singleton(order));
    }

    @Test
    public void shouldFindClientById() {
        Mockito
                .doReturn(Optional.of(client))
                .when(clientRepo)
                .findById(1L);

        Client client = clientService.findById(1L);

        assertThat(client.getLogin()).isEqualTo("A");
        Mockito
                .verify(clientRepo, Mockito.times(1))
                .findById(1L);
    }

    @Test
    public void shouldThrowExceptionWhenFindClientByIncorrectId() {
        assertThrows(NoSuchElementException.class, () -> {
            Client client = clientService.findById(1L);
        });

        Mockito
                .verify(clientRepo, Mockito.times(1))
                .findById(1L);
    }

    @Test
    public void shouldFindAllClients() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> page = new PageImpl<>(Collections.singletonList(client));
        Mockito
                .doReturn(page)
                .when(clientRepo)
                .findAll(pageable);

        Page<Client> clientsPage = clientService.findAll(pageable);
        List<Client> clients = clientsPage.getContent();

        assertThat(clients.size()).isEqualTo(1);
        Mockito
                .verify(clientRepo, Mockito.times(1))
                .findAll(pageable);
    }

    @Test
    public void shouldFindClientByLogin() {
        Mockito
                .doReturn(client)
                .when(clientRepo)
                .findByLogin("A");

        Client client = clientService.findByLogin("A");

        assertThat(client.getLogin()).isEqualTo("A");
        Mockito
                .verify(clientRepo, Mockito.times(1))
                .findByLogin("A");
    }

    @Test
    public void shouldFindClientByConfirmationCode() {
        client.setConfirmationCode("123");
        Mockito
                .doReturn(client)
                .when(clientRepo)
                .findByConfirmationCode("123");

        Client founded = clientService.findByConfirmationCode("123");

        assertThat(founded).isNotNull();
        assertThat(founded.getConfirmationCode()).isEqualTo("123");

        Mockito.verify(clientRepo, Mockito.times(1))
                .findByConfirmationCode("123");
    }

    @Test
    public void shouldFindBasketItemsByClientId() {
        Mockito
                .doReturn(Optional.of(client))
                .when(clientRepo)
                .findById(200L);

        List<ClientItem> founded = clientService.findBasketItemsByClientId(200L);

        assertThat(founded).isNotNull();
        assertThat(founded.size()).isEqualTo(2);

        Mockito.verify(clientRepo, Mockito.times(1))
                .findById(200L);
    }

    @Test
    public void shouldFindOrdersByClientId() {
        Mockito
                .doReturn(Optional.of(client))
                .when(clientRepo)
                .findById(200L);

        List<Order> founded = clientService.findOrdersByClientId(200L);

        assertThat(founded).isNotNull();
        assertThat(founded.size()).isEqualTo(1);

        Mockito.verify(clientRepo, Mockito.times(1))
                .findById(200L);
    }

    @Test
    public void shouldSaveOrUpdateClient() {
        clientService.save(client);

        assertThat(client.getRoles().size()).isEqualTo(1);
        assertThat(client.getRoles().iterator().next()).isEqualTo(Role.USER);
        assertThat(client.getPassword()).isEqualTo("123456");
        Mockito.verify(clientRepo, Mockito.times(1))
                .save(client);

        Long id = client.getId();

        Mockito
                .doReturn(client)
                .when(clientRepo)
                .findByLogin(client.getLogin());

        client.setPatronymic("Patronymic");
        client.setEmail("g@g");
        clientService.save(client);

        assertThat(client.getId()).isEqualTo(id);
        assertThat(client.getPassword()).isEqualTo("123456");
        Mockito.verify(clientRepo, Mockito.times(2))
                .save(client);
    }

    @Test
    public void shouldEncodePasswordWhenConfirmationCodeIsNotNull() {
        client.setConfirmationCode("123");
        clientService.save(client);

        Mockito
                .doReturn(client)
                .when(clientRepo)
                .findByLogin("A");

        clientService.save(client);

        assertThat(client.getConfirmationCode()).isNull();
        assertThat(passwordEncoder.matches("123456", client.getPassword()));
    }

    @Test
    public void shouldDeleteClient() {
        clientService.delete(client);

        Mockito.verify(clientRepo, Mockito.times(1))
                .delete(client);
    }

    @Test
    public void shouldDeleteBasketItems() {
        Category books = new Category("Books");
        Category book = new Category("Book", books);
        Item item = new Item("item", 30L, 3D
                , 600D, "123");
        item.setDescription("description...");
        item.setCharacteristics("characteristics...");
        item.setCategory(book);
        ClientItem clientItem = new ClientItem(item, 3);
        ClientItem clientItem1 = new ClientItem(item, 4);

        client.setBasket(new HashSet<>(Arrays.asList(clientItem, clientItem1)));

        Mockito
                .doReturn(Optional.of(client))
                .when(clientRepo)
                .findById(200L);

        clientService.deleteBasketItems(new HashSet<>(Collections.singleton(clientItem)), 200L);

        assertThat(client.getBasket().size()).isEqualTo(1);
        Mockito
                .verify(clientRepo, Mockito.times(1))
                .findById(200L);
        Mockito
                .verify(clientRepo, Mockito.times(1))
                .save(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRaiseExceptionWhenTryToDeleteBasketItemByNonExistsClient() {
        assertThrows(NoSuchElementException.class, () ->
                clientService.deleteBasketItems(Collections.EMPTY_SET, 500L));

        Mockito
                .verify(clientRepo, Mockito.times(1))
                .findById(500L);
    }

    @Test
    public void shouldReturnUserDetailsWhenLoadClientByUsernameWithNullConfirmationCode() {
        client.setConfirmationCode(null);
        Mockito
                .doReturn(client)
                .when(clientRepo)
                .findByLogin("A");

        UserDetails userDetails = clientService.loadUserByUsername("A");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("A");
    }
}
