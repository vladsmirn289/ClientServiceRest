package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
public class OrderCacheTest {
    @Autowired
    private ClientService clientService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void findOrdersForManagersCachingTest() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));
        orderService.findOrdersForManagers(pageable);

        Cache cache = cacheManager.getCache("pagination");
        assertThat(cache).isNotNull();

        List<Order> orders = cache.get(pageable, Page.class).getContent();
        assertThat(orders).isNotNull();
        assertThat(orders.size()).isEqualTo(2);
    }

    @Test
    public void findOrdersByClientCachingTest() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));
        Client client = clientService.findById(12L);
        orderService.findOrdersByClient(client, pageable);

        Cache cache = cacheManager.getCache("pagination");
        assertThat(cache).isNotNull();

        List<Order> orders = cache.get(pageable, Page.class).getContent();
        assertThat(orders).isNotNull();
        assertThat(orders.size()).isEqualTo(2);
    }

    @Test
    public void findByIdCachingTest() {
        orderService.findById(20L);

        Cache cache = cacheManager.getCache("orders");
        assertThat(cache).isNotNull();

        Order order = cache.get(20L, Order.class);
        assertThat(order).isNotNull();
        assertThat(order.getContacts().getCity()).isEqualTo("Южноуральск");
    }

    @Test
    public void deleteCachingTest() {
        Order toDelete = orderService.findById(20L);

        orderService.delete(toDelete);

        Cache cache = cacheManager.getCache("orders");
        assertThat(cache).isNotNull();

        Order order = cache.get(20L, Order.class);
        assertThat(order).isNull();
    }
}
