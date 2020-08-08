package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.*;
import com.shop.ClientServiceRest.Repository.OrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private OrderRepo orderRepo;

    private Order order;

    @BeforeEach
    public void init() {
        cacheManager.getCache("clients").clear();
        cacheManager.getCache("basket").clear();
        cacheManager.getCache("orders").clear();
        cacheManager.getCache("pagination").clear();

        Category books = new Category("Books");
        Category book = new Category("Book", books);
        Item item = new Item("item", 30L, 3D
                , 600D, "123");
        item.setDescription("description...");
        item.setCharacteristics("characteristics...");
        item.setCategory(book);
        ClientItem clientItem = new ClientItem(item, 2);
        Contacts contacts = new Contacts("123456", "Russia", "Moscow", "...", "89441234567");
        this.order = new Order(new HashSet<>(Collections.singleton(clientItem)), contacts, "C.O.D");
        this.order.setId(1L);
    }

    @Test
    public void shouldFindOrdersForManagers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = new PageImpl<>(Collections.singletonList(order));
        Mockito
                .doReturn(orders)
                .when(orderRepo)
                .findOrdersForManagers(pageable);

        Page<Order> orderListPage = orderService.findOrdersForManagers(pageable);
        List<Order> orderList = orderListPage.getContent();

        assertThat(orderList).isNotNull();
        assertThat(orderList).isNotEmpty();
        Mockito.verify(orderRepo, Mockito.times(1))
                .findOrdersForManagers(pageable);
    }

    @Test
    public void shouldFindOrdersByClient() {
        Client client = new Client("w@w", "12345", "firstName", "lastName", "login");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = new PageImpl<>(Collections.singletonList(order));
        Mockito
                .doReturn(orders)
                .when(orderRepo)
                .findOrdersByClient(client, pageable);

        Page<Order> orderListPage = orderService.findOrdersByClient(client, pageable);
        List<Order> orderList = orderListPage.getContent();

        assertThat(orderList).isNotNull();
        assertThat(orderList).isNotEmpty();
        Mockito.verify(orderRepo, Mockito.times(1))
                .findOrdersByClient(client, pageable);
    }

    @Test
    public void shouldFindOrderById() {
        Mockito
                .doReturn(Optional.of(order))
                .when(orderRepo)
                .findById(1L);

        Order order1 = orderService.findById(1L);
        
        Mockito.verify(orderRepo, Mockito.times(1))
                .findById(1L);
    }

    @Test
    public void shouldReturnNullWhenFindOrderByIncorrectId() {
        assertThrows(NoSuchElementException.class, () -> orderService.findById(1L));

        Mockito.verify(orderRepo, Mockito.times(1))
                .findById(1L);
    }

    @Test
    public void shouldSaveOrder() {
        orderService.save(order);

        Mockito.verify(orderRepo, Mockito.times(1))
                .save(order);
    }

    @Test
    public void shouldDeleteOrder() {
        orderService.delete(order);

        Mockito.verify(orderRepo, Mockito.times(1))
                .delete(order);
    }
}
