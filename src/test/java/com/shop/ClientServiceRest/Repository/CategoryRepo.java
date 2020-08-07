package com.shop.ClientServiceRest.Repository;

import com.shop.ClientServiceRest.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    List<Category> findByParent(Category parent);
    List<Category> findByParentIsNull();
    Category findByName(String name);
}
