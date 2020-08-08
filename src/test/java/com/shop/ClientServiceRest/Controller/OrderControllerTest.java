package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.DTO.AuthRequest;
import com.shop.ClientServiceRest.DTO.AuthResponse;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Contacts;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Service.ClientItemService;
import com.shop.ClientServiceRest.Service.ClientService;
import com.shop.ClientServiceRest.Service.OrderService;
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

import java.util.Arrays;
import java.util.HashSet;
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
public class OrderControllerTest {
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private ClientItemService clientItemService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private OrderService orderService;

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
    void shouldSuccessGetOrdersByClientId() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<List<Order>> orderResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders?page=0&size=5",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<Order>>() {});

        assertThat(orderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderResponse.getBody()).isNotNull();

        List<Order> orders = orderResponse.getBody();
        assertThat(orders.size()).isEqualTo(2);
    }

    @Test
    void shouldSuccessGetOrderById() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<Order> orderResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders/20",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<Order>() {});

        assertThat(orderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderResponse.getBody()).isNotNull();

        Order order = orderResponse.getBody();
        assertThat(order.getContacts().getCity()).isEqualTo("Южноуральск");
    }

    @Test
    void shouldNotFoundWhenTryToGetOrderThatNotExistsByThisClient() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ResponseEntity<Order> orderResponse =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders/21",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<Order>() {});

        assertThat(orderResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(orderResponse.getBody()).isNull();
    }

    @Test
    void shouldSuccessfulUpdateOrder() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        Order order = orderService.findById(20L);
        order.setPaymentMethod("Card");
        order.setTrackNumber("trackNumber");

        ResponseEntity<Order> responseOrder =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders/20",
                        HttpMethod.PUT,
                        new HttpEntity<>(order, headers),
                        Order.class);

        assertThat(responseOrder.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseOrder.getBody()).isNotNull();

        Order first = responseOrder.getBody();
        assertThat(first.getId()).isEqualTo(20);
        assertThat(first.getPaymentMethod()).isEqualTo("Card");
        assertThat(first.getTrackNumber()).isEqualTo("trackNumber");
    }

    @Test
    void shouldBadRequestWhenTryToUpdateOrderInBasketWithIncorrectInfo() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        Order order = orderService.findById(20L);
        order.setPaymentMethod(null);

        ResponseEntity<Order> responseOrder =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders/20",
                        HttpMethod.PUT,
                        new HttpEntity<>(order, headers),
                        Order.class);

        assertThat(responseOrder.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseOrder.getBody()).isNotNull();

        Order first = responseOrder.getBody();
        assertThat(first.getId()).isEqualTo(20);
        assertThat(first.getPaymentMethod()).isEqualTo(null);
    }

    @Test
    void shouldSuccessCreateNewOrder() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ClientItem clientItem1 = clientItemService.findById(30L);
        ClientItem clientItem2 = clientItemService.findById(31L);
        Contacts contacts = new Contacts("zipCode",
                "country_test",
                "city_test",
                "street_test",
                "phoneNumber_test");
        Order order = new Order(new HashSet<>(Arrays.asList(clientItem1, clientItem2)), contacts, "PaymentMethod");

        ResponseEntity<Order> responseOrder =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders",
                        HttpMethod.POST,
                        new HttpEntity<>(order, headers),
                        Order.class);

        assertThat(responseOrder.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseOrder.getBody()).isNotNull();

        Order first = responseOrder.getBody();
        assertThat(first.getId()).isEqualTo(100);

        Contacts c = first.getContacts();
        assertThat(c.getCity()).isEqualTo("city_test");
        assertThat(c.getCountry()).isEqualTo("country_test");
        assertThat(c.getPhoneNumber()).isEqualTo("phoneNumber_test");
        assertThat(c.getStreet()).isEqualTo("street_test");
        assertThat(c.getZipCode()).isEqualTo("zipCode");
    }

    @Test
    void shouldBadRequestWhenTryToCreateNewOrderWithInvalidData() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        ClientItem clientItem1 = clientItemService.findById(30L);
        ClientItem clientItem2 = clientItemService.findById(31L);
        Order order = new Order(new HashSet<>(Arrays.asList(clientItem1, clientItem2)), null, null);

        ResponseEntity<Order> responseOrder =
                restTemplate.exchange(
                        "http://localhost:9002/client-rest-swagger/api/clients/12/orders",
                        HttpMethod.POST,
                        new HttpEntity<>(order, headers),
                        Order.class);

        assertThat(responseOrder.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseOrder.getBody()).isNotNull();

        Order first = responseOrder.getBody();
        assertThat(first.getId()).isEqualTo(null);
        assertThat(first.getPaymentMethod()).isNull();

        Contacts c = first.getContacts();
        assertThat(c).isNull();
    }

    @Test
    void shouldDeleteOrderById() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        restTemplate.exchange(
                "http://localhost:9002/client-rest-swagger/api/clients/12/orders/19",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        List<Order> basket = clientService.findOrdersByClientId(12L);
        assertThat(basket.size()).isEqualTo(1);
    }

    @Test
    void shouldClearOrders() {
        HttpHeaders headers = getHeaderWithJwt("simpleUser", "12345");

        restTemplate.exchange(
                "http://localhost:9002/client-rest-swagger/api/clients/12/orders",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class);

        List<Order> basket = clientService.findOrdersByClientId(12L);
        assertThat(basket.size()).isEqualTo(0);
    }
}
