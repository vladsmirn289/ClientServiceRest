package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.DTO.AuthRequest;
import com.shop.ClientServiceRest.DTO.AuthResponse;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Item;
import com.shop.ClientServiceRest.Repository.ItemRepo;
import com.shop.ClientServiceRest.Service.ClientItemService;
import com.shop.ClientServiceRest.Service.ClientService;
import org.assertj.core.data.Percentage;
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
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
public class BasketControllerTest {
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private ClientItemService clientItemService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void init() {
        cacheManager.getCache("clients").clear();
        cacheManager.getCache("basket").clear();
        cacheManager.getCache("orders").clear();
        cacheManager.getCache("pagination").clear();
    }

    private HttpHeaders getHeaderWithJwt(String name, String password) {
        AuthRequest authRequest = new AuthRequest(name, password);
        AuthResponse authResponse = restTemplate.postForEntity(
                "http://localhost:9001/auth-server-swagger/api/authentication",
                authRequest,
                AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + authResponse.getJwtToken());

        return headers;
    }

    @Test
    void shouldSuccessGetBasketByClientId() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<List<ClientItem>> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket",
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
                        "http://localhost:9002/client-rest-swagger/api/clients/100/basket",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<ClientItem>>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }

    @Test
    void shouldCalcGeneralPriceOfBasket() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<Double> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket/generalPrice",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<Double>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(basketResponse.getBody()).isNotNull();

        Double generalPrice = basketResponse.getBody();
        assertThat(generalPrice).isEqualTo(221980);
    }

    @Test
    void shouldReturnNullWhenTryToCalcGeneralPriceOfIncorrectClient() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<Double> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/100/basket/generalPrice",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Double.class);

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }

    @Test
    void shouldCalcGeneralWeightOfBasket() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<Double> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket/generalWeight",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<Double>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(basketResponse.getBody()).isNotNull();

        Double generalPrice = basketResponse.getBody();
        assertThat(generalPrice).isCloseTo(11, Percentage.withPercentage(99));
    }

    @Test
    void shouldReturnNullWhenTryToCalcGeneralWeightOfIncorrectClient() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<Double> basketResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/100/basket/generalWeight",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<Double>() {});

        assertThat(basketResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(basketResponse.getBody()).isNull();
    }

    @Test
    void shouldGetItemOfBasketById() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<ClientItem> responseClientItem =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket/30",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        ClientItem.class);

        assertThat(responseClientItem.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClientItem.getBody()).isNotNull();

        ClientItem clientItem = responseClientItem.getBody();
        assertThat(clientItem.getQuantity()).isEqualTo(3);

        Item first = clientItem.getItem();
        assertThat(first.getId()).isEqualTo(11);
    }

    @Test
    void shouldNotFoundWhenTryToGetItemOfBasketByIncorrectId() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        ResponseEntity<ClientItem> responseClientItem =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket/100",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        ClientItem.class);

        assertThat(responseClientItem.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseClientItem.getBody()).isNull();
    }

    @Test
    void shouldSuccessUpdateItemInBasket() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        Item item = itemRepo.findById(7L).get();
        ClientItem clientItem = clientItemService.findById(16L);
        clientItem.setQuantity(10);
        clientItem.setItem(item);

        ResponseEntity<ClientItem> responseClientItem =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket/16",
                        HttpMethod.PUT,
                        new HttpEntity<>(clientItem, headers),
                        ClientItem.class);

        assertThat(responseClientItem.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClientItem.getBody()).isNotNull();

        ClientItem first = responseClientItem.getBody();
        assertThat(first.getId()).isEqualTo(16);
        assertThat(first.getQuantity()).isEqualTo(10);
        assertThat(first.getItem().getId()).isEqualTo(7);
    }

    @Test
    void shouldBadRequestWhenTryToUpdateItemInBasketWithIncorrectInfo() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ClientItem clientItem = clientItemService.findById(16L);
        clientItem.setQuantity(-10);

        ResponseEntity<ClientItem> responseClientItem =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket/16",
                        HttpMethod.PUT,
                        new HttpEntity<>(clientItem, headers),
                        ClientItem.class);

        assertThat(responseClientItem.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseClientItem.getBody()).isNotNull();

        ClientItem first = responseClientItem.getBody();
        assertThat(first.getId()).isEqualTo(16);
        assertThat(first.getQuantity()).isEqualTo(-10);
    }

    @Test
    void shouldSuccessAddItemToBasket() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ClientItem clientItem = clientItemService.findById(16L);

        ResponseEntity<ClientItem> responseClientItem =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket",
                        HttpMethod.POST,
                        new HttpEntity<>(clientItem, headers),
                        ClientItem.class);

        assertThat(responseClientItem.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseClientItem.getBody()).isNotNull();

        ClientItem first = responseClientItem.getBody();
        assertThat(first.getId()).isEqualTo(16);
        assertThat(first.getQuantity()).isEqualTo(2);
        assertThat(first.getItem().getId()).isEqualTo(6);

        List<ClientItem> basket = clientService.findBasketItemsByClientId(12L);
        assertThat(basket.size()).isEqualTo(3);
    }

    @Test
    void shouldBadRequestWhenTryToAddItemToBasketWithInvalidData() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ClientItem clientItem = new ClientItem(null, -2);

        ResponseEntity<ClientItem> responseClientItem =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/basket",
                        HttpMethod.POST,
                        new HttpEntity<>(clientItem, headers),
                        ClientItem.class);

        assertThat(responseClientItem.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseClientItem.getBody()).isNotNull();

        ClientItem first = responseClientItem.getBody();
        assertThat(first.getQuantity()).isEqualTo(-2);
        assertThat(first.getItem()).isNull();

        List<ClientItem> basket = clientService.findBasketItemsByClientId(12L);
        assertThat(basket.size()).isEqualTo(2);
    }

    @Test
    void shouldDeleteItemFromBasketById() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        restTemplate.exchange(
                "http://localhost:9002/client-rest-swagger/api/clients/12/basket/30",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        List<ClientItem> basket = clientService.findBasketItemsByClientId(12L);
        assertThat(basket.size()).isEqualTo(1);
    }

    @Test
    void shouldDoNothingWhenTryToDeleteItemFromBasketWithIncorrectId() {
        HttpHeaders headers = getHeaderWithJwt("admin", "01112");

        restTemplate.exchange(
                "http://localhost:9002/client-rest-swagger/api/clients/12/basket/100",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        List<ClientItem> basket = clientService.findBasketItemsByClientId(12L);
        assertThat(basket.size()).isEqualTo(2);
    }

    @Test
    void shouldClearBasket() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        restTemplate.exchange(
                "http://localhost:9002/client-rest-swagger/api/clients/12/basket",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        List<ClientItem> basket = clientService.findBasketItemsByClientId(12L);
        assertThat(basket.size()).isEqualTo(0);
    }
}
