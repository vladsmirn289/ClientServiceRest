package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<Order> findOrdersForManagers(Pageable pageable);
    Page<Order> findOrdersByClient(Client client, Pageable pageable);
    Order findById(Long id);

    void save(Order order);

    void delete(Order order);
}
