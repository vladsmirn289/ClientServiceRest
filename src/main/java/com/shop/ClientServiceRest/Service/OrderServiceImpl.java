package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Repository.OrderRepo;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    private OrderRepo orderRepo;

    @Autowired
    public void setOrderRepo(OrderRepo orderRepo) {
        logger.debug("Setting orderRepo");
        this.orderRepo = orderRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrdersForManagers(Pageable pageable) {
        logger.info("findOrdersForManagers method called");
        return orderRepo.findOrdersForManagers(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrdersByClient(Client client, Pageable pageable) {
        return orderRepo.findOrdersByClient(client, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        logger.info("findById method called for order with id = " + id);
        return orderRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Client findClientByOrderId(Long id) {
        Order order = findById(id);
        Hibernate.initialize(order.getClient());

        return order.getClient();
    }

    @Override
    public void save(Order order) {
        logger.info("Saving order with id = " + order.getId() + " to database");
        orderRepo.save(order);
    }

    @Override
    public void delete(Order order) {
        logger.info("Deleting order with id = " + order.getId() + " from database");
        orderRepo.delete(order);
    }
}
