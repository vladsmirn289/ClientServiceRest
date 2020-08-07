package com.shop.ClientServiceRest.Repository;

import com.shop.ClientServiceRest.Model.ClientItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientItemRepo extends JpaRepository<ClientItem, Long> {
}
