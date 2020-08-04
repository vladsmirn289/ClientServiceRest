package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.DTO.AuthRequest;
import com.shop.ClientServiceRest.DTO.AuthResponse;
import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@PropertySource(value = "classpath:application.properties")
@Sql(value = {
        "classpath:db/PostgreSQL/after-test.sql",
        "classpath:db/PostgreSQL/category-test.sql",
        "classpath:db/PostgreSQL/user-test.sql",
        "classpath:db/PostgreSQL/item-test.sql",
        "classpath:db/PostgreSQL/order-test.sql",
        "classpath:db/PostgreSQL/clientItem-test.sql",
        "classpath:db/PostgreSQL/basket-test.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ClientRestTest {
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientService clientService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void init() {
        cacheManager.getCache("clients").clear();
    }

    private HttpHeaders getHeaderWithJwt(String name, String password) {
        AuthRequest authRequest = new AuthRequest(name, password);
        AuthResponse authResponse = restTemplate.postForEntity(
                "http://localhost:9001/api/authentication",
                authRequest,
                AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + authResponse.getJwtToken());

        return headers;
    }

    @Test
    void showListOfClients() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<List<Client>> responseClients =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients?page=0&size=2",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
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
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/12",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Client.class);

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
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/200",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void showClientByLogin() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/byLogin/simpleUser",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Client.class);

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
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/byLogin/incorrect",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void showClientByConfirmCode() {
        HttpHeaders headers = getHeaderWithJwt("userWithCode", "01112");
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/byConfirmCode/123",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClient.getBody()).isNotNull();

        Client first = responseClient.getBody();
        assertThat(first.getId()).isEqualTo(15);
        assertThat(first.getFirstName()).isEqualTo("STU");
        assertThat(first.getLastName()).isEqualTo("VWX");
        assertThat(first.getLogin()).isEqualTo("userWithCode");
        assertThat(passwordEncoder.matches("01112", first.getPassword())).isTrue();
        assertThat(first.getEmail()).isEqualTo("g@g.com");
        assertThat(first.getPatronymic()).isNull();
        assertThat(first.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldNotFoundWhenFindClientByIncorrectConfirmCode() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/byConfirmCode/1234567890",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Client.class);

        assertThat(responseClient.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClient.getBody()).isNull();
    }

    @Test
    void shouldSuccessUpdateClient() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        Client client = clientService.findById(12L);
        client.setFirstName("firstName");
        client.setLastName("lastName");
        client.setLogin("RandomLogin");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/12",
                        HttpMethod.PUT,
                        new HttpEntity<>(client, headers),
                        Client.class,
                        headers);

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
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        Client client = clientService.findById(12L);
        client.setFirstName("");
        client.setLastName("");
        client.setLogin("");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/12",
                        HttpMethod.PUT,
                        new HttpEntity<>(client, headers),
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
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        Client client = clientService.findById(12L);
        client.setFirstName("firstName");
        client.setLastName("lastName");
        client.setLogin("RandomLogin");

        ResponseEntity<Client> responseClient =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/200",
                        HttpMethod.PUT,
                        new HttpEntity<>(client, headers),
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
                        "http://localhost:9002/api/clients",
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
                        "http://localhost:9002/api/clients",
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
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        restTemplate.exchange(
                "http://localhost:9002/api/clients/12",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        assertThrows(NoSuchElementException.class, () -> clientService.findById(12L));
    }

    @Test
    void shouldDoNothingWhenTryToDeleteClientWithIncorrectId() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        restTemplate.exchange(
                "http://localhost:9002/api/clients/100",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        assertDoesNotThrow(() -> {
            clientService.findById(12L);
        });
    }

    @Test
    void shouldSuccessGetBasketByClientId() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/12/basket",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(basketResponse.getBody()).isNotNull();

        List<ClientItem> clientItems = basketResponse.getBody();
        assertThat(clientItems.size()).isEqualTo(2);
    }

    @Test
    void shouldNotFoundWhenTryToGetBasketByClientWithIncorrectId() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/100/basket",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }

    @Test
    void shouldSuccessDeleteBasketByClientId() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        restTemplate.exchange(
                "http://localhost:9002/api/clients/12/basket",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/12/basket",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getBody()).isNotNull();
        assertThat(basketResponse.getBody().size()).isEqualTo(0);
    }

    @Test
    void shouldDoNothingWhenTryToDeleteBasketByIncorrectData() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        restTemplate.exchange(
                "http://localhost:9002/api/13/basket",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);
    }

    @Test
    void shouldSuccessGetOrdersByClientId() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<List<Order>> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/12/orders",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<Order>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(basketResponse.getBody()).isNotNull();

        List<Order> clientItems = basketResponse.getBody();
        assertThat(clientItems.size()).isEqualTo(2);
    }

    @Test
    void shouldNotFoundWhenTryToGetOrdersByClientWithIncorrectId() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<List<Order>> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/api/clients/100/orders",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<Order>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }
}
