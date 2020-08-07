package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Service.ClientService;
import com.shop.ClientServiceRest.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/clients/{id}/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private ClientService clientService;
    private OrderService orderService;

    private static final String ACCESS_BY_ID_OR_NOT_USER_ROLE = "#authClient.id == #id" +
            " || hasAnyRole('ADMIN', 'MANAGER')";

    @Autowired
    public void setClientService(ClientService clientService) {
        logger.debug("Setting clientService");
        this.clientService = clientService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        logger.debug("Setting orderService");
        this.orderService = orderService;
    }

    @GetMapping(params = {"page", "size"})
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<List<Order>> getOrdersByClientId(@AuthenticationPrincipal Client authClient,
                                                           @PathVariable("id") Long id,
                                                           @RequestParam("page") int page,
                                                           @RequestParam("size") int size) {
        logger.info("Called getOrdersByClientId method");
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        List<Order> orders = orderService.findOrdersByClient(authClient, pageable).getContent();

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{order_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Order> getOrderById(@AuthenticationPrincipal Client authClient,
                                              @PathVariable("id") Long id,
                                              @PathVariable("order_id") Long orderId) {
        logger.info("Called getOrderById");
        Order order = orderService.findById(orderId);

        if (order == null) {
            logger.warn("Order with id - " + orderId + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        List<Order> orders = clientService.findOrdersByClientId(id);
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                return new ResponseEntity<>(o, HttpStatus.OK);
            }
        }

        logger.warn("Client with id -" + id + " not contain order with id - " + orderId);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{order_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Order> updateOrder(@AuthenticationPrincipal Client authClient,
                                             @PathVariable("id") Long id,
                                             @PathVariable("order_id") Long orderId,
                                             @RequestBody @Valid Order order,
                                             BindingResult bindingResult) {
        logger.info("Called updateOrder method");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on update order information");
            return new ResponseEntity<>(order, HttpStatus.BAD_REQUEST);
        }

        try {
            Order persistentOrder = orderService.findById(orderId);

            BeanUtils.copyProperties(order, persistentOrder, "id");
            orderService.save(persistentOrder);
            return new ResponseEntity<>(persistentOrder, HttpStatus.OK);
        } catch (NoSuchElementException | NullPointerException ex) {
            logger.warn("Order with id - " + orderId + " not found");
            logger.error(ex.toString());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Order> createNewOrder(@AuthenticationPrincipal Client authClient,
                                                @PathVariable("id") Long id,
                                                @RequestBody @Valid Order order,
                                                BindingResult bindingResult) {
        logger.info("Called createNewOrder");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on order information");
            return new ResponseEntity<>(order, HttpStatus.BAD_REQUEST);
        }

        orderService.save(order);

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @DeleteMapping("/{order_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void deleteOrderById(@AuthenticationPrincipal Client authClient,
                                @PathVariable("id") Long id,
                                @PathVariable("order_id") Long orderId) {
        logger.info("Called deleteOrderById method");

        Order order = getOrderById(authClient, authClient.getId(), orderId).getBody();
        if (order == null) {
            return;
        }

        orderService.delete(order);
    }

    @DeleteMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void clearOrdersByClientId(@AuthenticationPrincipal Client authClient,
                                      @PathVariable("id") Long id) {
        logger.info("Called clearOrdersByClientId method");

        List<Order> orders = clientService.findOrdersByClientId(id);
        if (orders == null || orders.isEmpty()) {
            return;
        }

        orders.forEach(o -> orderService.delete(o));
    }
}
