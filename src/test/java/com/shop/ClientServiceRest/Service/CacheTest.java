package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(value = {
        "classpath:db/PostgreSQL/after-test.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CacheTest {
    @Autowired
    private ClientService clientService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void init() {
        Client client1 = new Client(
                "one@gmail.com",
                "12345",
                "1_first_name",
                "1_last_name",
                "1_patronymic",
                "1_login");
        clientService.save(client1);

        Client client2 = new Client(
                "two@gmail.com",
                "12345",
                "2_first_name",
                "2_last_name",
                "2_patronymic",
                "2_login");
        clientService.save(client2);

        Client client3 = new Client(
                "three@gmail.com",
                "12345",
                "3_first_name",
                "3_last_name",
                "3_patronymic",
                "3_login");
        clientService.save(client3);
    }

    @Test
    public void findByIdCachingTest() {
        clientService.findById(100L);
        clientService.findById(101L);
        clientService.findById(100L);
        clientService.findById(101L);
        clientService.findById(102L);

        Cache cache = cacheManager.getCache("clients");
        assertThat(cache).isNotNull();

        Client cached1 = cache.get(100L, Client.class);
        Client cached2 = cache.get(101L, Client.class);
        Client cached3 = cache.get(102L, Client.class);
        assertThat(cached1).isNull();
        assertThat(cached2).isNotNull();
        assertThat(cached3).isNotNull();

        assertThat(cached2.getFirstName()).isEqualTo("2_first_name");
        assertThat(cached3.getFirstName()).isEqualTo("3_first_name");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findAllCachingTest() {
        Pageable pageable1 = PageRequest.of(0, 1);
        Pageable pageable2 = PageRequest.of(1, 1);
        clientService.findAll(pageable1);
        clientService.findAll(pageable2);

        Cache cache = cacheManager.getCache("pagination");
        assertThat(cache).isNotNull();

        Page<Client> clientPage1 = cache.get(pageable1, Page.class);
        Page<Client> clientPage2 = cache.get(pageable2, Page.class);
        assertThat(clientPage1).isNotNull();
        assertThat(clientPage2).isNotNull();
        List<Client> clients1 = clientPage1.getContent();
        List<Client> clients2 = clientPage2.getContent();
        assertThat(clients1.get(0).getFirstName()).isEqualTo("1_first_name");
        assertThat(clients2.get(0).getFirstName()).isEqualTo("2_first_name");
    }

    @Test
    public void findByLoginCachingTest() {
        clientService.findByLogin("1_login");
        clientService.findByLogin("2_login");
        clientService.findByLogin("1_login");
        clientService.findByLogin("2_login");
        clientService.findByLogin("3_login");

        Cache cache = cacheManager.getCache("clients");
        assertThat(cache).isNotNull();

        Client cached1 = cache.get("1_login", Client.class);
        Client cached2 = cache.get("2_login", Client.class);
        Client cached3 = cache.get("3_login", Client.class);
        assertThat(cached1).isNull();
        assertThat(cached2).isNotNull();
        assertThat(cached3).isNotNull();

        assertThat(cached2.getFirstName()).isEqualTo("2_first_name");
        assertThat(cached3.getFirstName()).isEqualTo("3_first_name");
    }

    @Test
    public void deleteCachingTest() {
        Cache cache = cacheManager.getCache("clients");
        assertThat(cache).isNotNull();

        Client toDelete = clientService.findById(102L);

        clientService.findByLogin("3_login");
        assertThat(cache.get(102L, Client.class)).isNotNull();
        assertThat(cache.get("3_login", Client.class)).isNotNull();

        clientService.delete(toDelete);

        assertThat(cache.get(102L, Client.class)).isNull();
        assertThat(cache.get("3_login", Client.class)).isNull();
    }

    @Test
    public void loadUserByUsernameCachingTest() {
        clientService.loadUserByUsername("1_login");
        clientService.loadUserByUsername("2_login");
        clientService.loadUserByUsername("1_login");
        clientService.loadUserByUsername("2_login");
        clientService.loadUserByUsername("3_login");

        Cache cache = cacheManager.getCache("clients");
        assertThat(cache).isNotNull();

        Client cached1 = cache.get("1_login", Client.class);
        Client cached2 = cache.get("2_login", Client.class);
        Client cached3 = cache.get("3_login", Client.class);
        assertThat(cached1).isNull();
        assertThat(cached2).isNotNull();
        assertThat(cached3).isNotNull();

        assertThat(cached2.getFirstName()).isEqualTo("2_first_name");
        assertThat(cached3.getFirstName()).isEqualTo("3_first_name");
    }
}
