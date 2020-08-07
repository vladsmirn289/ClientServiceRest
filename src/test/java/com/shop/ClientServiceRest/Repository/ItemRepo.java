package com.shop.ClientServiceRest.Repository;

import com.shop.ClientServiceRest.Model.Category;
import com.shop.ClientServiceRest.Model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepo extends JpaRepository<Item, Long> {
    List<Item> findByName(String name);
    List<Item> findByPrice(Double price);
    Page<Item> findByCategory(Category category, Pageable pageable);
    Item findByCode(String code);

    @Query("SELECT i FROM Item i WHERE lower(i.name) LIKE lower(concat('%', :keyword, '%'))")
    Page<Item> findBySearch(@Param("keyword") String keyword, Pageable pageable);
}
