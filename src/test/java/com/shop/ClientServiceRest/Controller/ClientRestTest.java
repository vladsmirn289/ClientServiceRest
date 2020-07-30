package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@PropertySource(value = "classpath:application.properties")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(value = {
        "classpath:db/H2/after-test.sql",
        "classpath:db/H2/category-test.sql",
        "classpath:db/H2/user-test.sql",
        "classpath:db/H2/item-test.sql",
        "classpath:db/H2/order-test.sql",
        "classpath:db/H2/clientItem-test.sql",
        "classpath:db/H2/basket-test.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ClientRestTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientService clientService;

    @Test
    void showListOfClients() {
        ResponseEntity<List<Client>> responseClients =
                restTemplate.exchange(
                        "/api/clients?page=0&size=2",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Client>>(){});

        assertThat(responseClients.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClients.getBody()).isNotNull();

        List<Client> clients = responseClients.getBody();
        assertThat(clients.size()).isEqualTo(2);

        Client first = clients.get(0);
        assertThat(first.getId()).isEqualTo(12);
        assertThat(first.getFirstName()).isEqualTo("ABC");
        assertThat(first.getLastName()).isEqualTo("DEF");
        assertThat(first.getLogin()).isEqualTo("simpleUser");
        assertThat(passwordEncoder.matches("12345", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("vladsmirn289@gmail.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();

        Client second = clients.get(1);
        assertThat(second.getId()).isEqualTo(13);
        assertThat(second.getFirstName()).isEqualTo("GHI");
        assertThat(second.getLastName()).isEqualTo("GKL");
        assertThat(second.getLogin()).isEqualTo("manager");
        assertThat(passwordEncoder.matches("67891", second.getPassword())).isTrue();
        assertThat(second.getEmail()).isEqualTo("xwitting@powlowski.com");
        assertThat(second.getPatronymic()).isNull();
        assertThat(second.isAccountNonLocked()).isTrue();
    }

    @Test
    void showClientById() {
        ResponseEntity<Client> responseClient =
                restTemplate.getForEntity("/api/clients/12", Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(12);
        assertThat(first.getFirstName()).isEqualTo("ABC");
        assertThat(first.getLastName()).isEqualTo("DEF");
        assertThat(first.getLogin()).isEqualTo("simpleUser");
        assertThat(passwordEncoder.matches("12345", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("vladsmirn289@gmail.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldNotFoundWhenFindClientByIncorrectId() {
        ResponseEntity<Client> responseClient =
                restTemplate.getForEntity("/api/clients/200", Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void showClientByLogin() {
        ResponseEntity<Client> responseClient =
                restTemplate.getForEntity("/api/clients/byLogin/simpleUser", Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(12);
        assertThat(first.getFirstName()).isEqualTo("ABC");
        assertThat(first.getLastName()).isEqualTo("DEF");
        assertThat(first.getLogin()).isEqualTo("simpleUser");
        assertThat(passwordEncoder.matches("12345", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("vladsmirn289@gmail.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldNotFoundWhenFindClientByIncorrectLogin() {
        ResponseEntity<Client> responseClient =
                restTemplate.getForEntity("/api/clients/byLogin/incorrect", Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void showClientByConfirmCode() {
        ResponseEntity<Client> responseClient =
                restTemplate.getForEntity("/api/clients/byConfirmCode/123", Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(15);
        assertThat(first.getFirstName()).isEqualTo("STU");
        assertThat(first.getLastName()).isEqualTo("VWX");
        assertThat(first.getLogin()).isEqualTo("userWithCode");
        assertThat(first.getPassword()).isEqualTo("01112");
        assertThat(first.getEmail()).isEqualTo("g@g.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldNotFoundWhenFindClientByIncorrectConfirmCode() {
        ResponseEntity<Client> responseClient =
                restTemplate.getForEntity("/api/clients/byConfirmCode/1234567890", Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void shouldSuccessUpdateClient() {
        Client client = clientService.findById(12L);
        client.setFirstName("firstName");
        client.setLastName("lastName");
        client.setLogin("RandomLogin");
        HttpEntity<Client> httpEntity = new HttpEntity<>(client);

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "/api/clients/12",
                        HttpMethod.PUT,
                        httpEntity,
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(12);
        assertThat(first.getFirstName()).isEqualTo("firstName");
        assertThat(first.getLastName()).isEqualTo("lastName");
        assertThat(first.getLogin()).isEqualTo("RandomLogin");
        assertThat(passwordEncoder.matches("12345", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("vladsmirn289@gmail.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldBadRequestWhenTryToUpdateClient() {
        Client client = clientService.findById(12L);
        client.setFirstName("");
        client.setLastName("");
        client.setLogin("");
        HttpEntity<Client> httpEntity = new HttpEntity<>(client);

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "/api/clients/12",
                        HttpMethod.PUT,
                        httpEntity,
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(12);
        assertThat(first.getFirstName()).isEqualTo("");
        assertThat(first.getLastName()).isEqualTo("");
        assertThat(first.getLogin()).isEqualTo("");
        assertThat(passwordEncoder.matches("12345", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("vladsmirn289@gmail.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldNotFoundWhenTryToUpdateClient() {
        Client client = clientService.findById(12L);
        client.setFirstName("firstName");
        client.setLastName("lastName");
        client.setLogin("RandomLogin");
        HttpEntity<Client> httpEntity = new HttpEntity<>(client);

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "/api/clients/200",
                        HttpMethod.PUT,
                        httpEntity,
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void shouldSuccessCreateNewClient() {
        Client client = clientService.findById(12L);
        client.setId(null);
        client.setEmail("hello@world.com");
        client.setFirstName("first-name");
        client.setLastName("last-name");
        client.setLogin("login");
        client.setConfirmationCode("12345678");
        client.setPassword(passwordEncoder.encode("randomPass"));
        client.setPatronymic("Patronymic");
        HttpEntity<Client> httpEntity = new HttpEntity<>(client);

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "/api/clients",
                        HttpMethod.POST,
                        httpEntity,
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(100);
        assertThat(first.getFirstName()).isEqualTo("first-name");
        assertThat(first.getLastName()).isEqualTo("last-name");
        assertThat(first.getLogin()).isEqualTo("login");
        assertThat(first.getConfirmationCode()).isEqualTo("12345678");
        assertThat(passwordEncoder.matches("randomPass", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("hello@world.com");
        assertThat(first.getPatronymic()).isEqualTo("Patronymic");
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldBadRequestWhenTryToCreateNewClientWithWrongData() {
        Client client = clientService.findById(12L);
        client.setId(null);
        client.setEmail("hello@world.com");
        client.setFirstName("");
        client.setLastName("");
        client.setLogin("");
        client.setPassword(passwordEncoder.encode("randomPass"));
        client.setPatronymic("Patronymic");
        HttpEntity<Client> httpEntity = new HttpEntity<>(client);

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "/api/clients",
                        HttpMethod.POST,
                        httpEntity,
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isNull();
        assertThat(first.getFirstName()).isEqualTo("");
        assertThat(first.getLastName()).isEqualTo("");
        assertThat(first.getLogin()).isEqualTo("");
        assertThat(passwordEncoder.matches("randomPass", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("hello@world.com");
        assertThat(first.getPatronymic()).isEqualTo("Patronymic");
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldDeleteClientById() {
        restTemplate.delete("/api/clients/12");

        assertThrows(NoSuchElementException.class, () -> clientService.findById(12L));
    }

    @Test
    void shouldDoNothingWhenTryToDeleteClientWithIncorrectId() {
        restTemplate.delete("/api/clients/100");

        assertDoesNotThrow(() -> {
            clientService.findById(12L);
        });
    }

    @Test
    void shouldSuccessGetBasketByClientId() {
        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "/api/clients/12/basket",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(basketResponse.getBody()).isNotNull();

        List<ClientItem> clientItems = basketResponse.getBody();
        assertThat(clientItems.size()).isEqualTo(2);
    }

    @Test
    void shouldNotFoundWhenTryToGetBasketByClientWithIncorrectId() {
        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "/api/clients/100/basket",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }

    @Test
    void shouldSuccessDeleteBasketByClientId() {
        restTemplate.delete("/api/clients/12/basket");

        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "/api/clients/12/basket",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getBody()).isNotNull();
        assertThat(basketResponse.getBody().size()).isEqualTo(0);
    }

    @Test
    void shouldDoNothingWhenTryToDeleteBasketByIncorrectData() {
        restTemplate.delete("/api/clients/13/basket");
    }

    @Test
    void shouldSuccessGetOrdersByClientId() {
        ResponseEntity<List<Order>> basketResponse =
                restTemplate.exchange(
                        "/api/clients/12/orders",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Order>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(basketResponse.getBody()).isNotNull();

        List<Order> clientItems = basketResponse.getBody();
        assertThat(clientItems.size()).isEqualTo(2);
    }

    @Test
    void shouldNotFoundWhenTryToGetOrdersByClientWithIncorrectId() {
        ResponseEntity<List<Order>> basketResponse =
                restTemplate.exchange(
                        "/api/clients/100/orders",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Order>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }
}
