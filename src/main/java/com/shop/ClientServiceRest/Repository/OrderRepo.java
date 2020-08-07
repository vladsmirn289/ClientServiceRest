package com.shop.ClientServiceRest.Repository;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.orderStatus <> 'COMPLETED'")
    Page<Order> findOrdersForManagers(Pageable pageable);

    Page<Order> findOrdersByClient(Client client, Pageable pageable);
}
