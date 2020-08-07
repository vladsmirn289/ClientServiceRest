package com.shop.ClientServiceRest.Repository;

import com.shop.ClientServiceRest.Model.Category;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ClientItemRepoTest {
    @Autowired
    private ClientItemRepo clientItemRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void init() {
        Category books = new Category("Books");
        Category book = new Category("Book", books);
        Item item = new Item("item", 30L, 3D
                , 600D, "123");
        item.setDescription("description...");
        item.setCharacteristics("characteristics...");
        item.setCategory(book);
        ClientItem clientItem = new ClientItem(item, 2);

        categoryRepo.save(books);
        categoryRepo.save(book);
        itemRepo.save(item);
        clientItemRepo.save(clientItem);
    }

    @AfterEach
    public void resetSequence() {
        entityManager.getEntityManager()
                .createNativeQuery("alter sequence hibernate_sequence restart 1")
                .executeUpdate();
    }

    @Test
    public void shouldFindClientItemById() {
        ClientItem clientItem = clientItemRepo.findById(4L).orElse(null);

        assertThat(clientItem).isNotNull();
        assertThat(clientItem.getItem().getName()).isEqualTo("item");
        assertThat(clientItem.getQuantity()).isEqualTo(2);
    }

    @Test
    public void shouldSaveClientItem() {
        Category laptops = new Category("Laptops");
        Category laptop = new Category("Laptop", laptops);
        Item item = new Item("laptop", 40L, 2D
                , 56000D, "567");
        item.setDescription("LaptopDescription...");
        item.setCharacteristics("LaptopCharacteristics...");
        item.setCategory(laptop);
        ClientItem clientItem = new ClientItem(item, 1);

        categoryRepo.save(laptops);
        categoryRepo.save(laptop);
        itemRepo.save(item);
        clientItemRepo.save(clientItem);

        assertThat(itemRepo.findAll().size()).isEqualTo(2);
        ClientItem clientItem1 = clientItemRepo.findById(8L).orElse(null);

        assertThat(clientItem1).isNotNull();
        assertThat(clientItem1.getItem().getName()).isEqualTo("laptop");
        assertThat(clientItem1.getQuantity()).isEqualTo(1);
    }

    @Test
    public void shouldDeleteClientItem() {
        ClientItem clientItem = clientItemRepo.findById(4L).orElse(null);
        assertThat(clientItem).isNotNull();
        clientItemRepo.delete(clientItem);

        assertThat(clientItemRepo.findAll().size()).isEqualTo(0);
    }

    @Test
    public void shouldDeleteClientItemById() {
        clientItemRepo.deleteById(4L);

        assertThat(clientItemRepo.findAll().size()).isEqualTo(0);
    }
}
