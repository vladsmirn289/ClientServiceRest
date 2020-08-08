package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.ClientItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;

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
public class ClientItemCacheTest {
    @Autowired
    private ClientItemService clientItemService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void findByIdCachingTest() {
        clientItemService.findById(16L);

        Cache cache = cacheManager.getCache("basket");
        assertThat(cache).isNotNull();

        ClientItem clientItem = cache.get(16L, ClientItem.class);
        assertThat(clientItem).isNotNull();
        assertThat(clientItem.getQuantity()).isEqualTo(2);
    }
}
