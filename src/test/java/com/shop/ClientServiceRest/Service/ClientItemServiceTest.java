package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Category;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Item;
import com.shop.ClientServiceRest.Repository.ClientItemRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ClientItemServiceTest {
    @Autowired
    private ClientItemService clientItemService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private ClientItemRepo clientItemRepo;

    private ClientItem clientItem;

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
        item.setId(1L);
        this.clientItem = new ClientItem(item, 3);
    }

    @Test
    public void shouldCalculateGeneralPrice() {
        double price = clientItemService.generalPrice(Collections.singleton(clientItem));
        assertThat(price).isEqualTo(1800D);
    }

    @Test
    public void shouldCalculateGeneralWeight() {
        double weight = clientItemService.generalWeight(Collections.singleton(clientItem));
        assertThat(weight).isEqualTo(9D);
    }

    @Test
    public void shouldFindClientItemById() {
        Mockito
                .doReturn(Optional.of(clientItem))
                .when(clientItemRepo)
                .findById(1L);

        ClientItem item = clientItemService.findById(1L);

        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getItem().getName()).isEqualTo("item");
        Mockito.verify(clientItemRepo, Mockito.times(1))
                .findById(1L);
    }

    @Test
    public void shouldRaiseExceptionWhenFindClientItemByIncorrectId() {
        assertThrows(NoSuchElementException.class,
                () -> clientItemService.findById(1L));

        Mockito.verify(clientItemRepo, Mockito.times(1))
                .findById(1L);
    }

    @Test
    public void shouldSaveClientItem() {
        clientItemService.save(clientItem);

        Mockito.verify(clientItemRepo, Mockito.times(1))
                .save(clientItem);
    }
}
