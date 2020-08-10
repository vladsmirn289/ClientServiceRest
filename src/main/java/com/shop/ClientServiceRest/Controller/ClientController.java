package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Service.ClientService;
import com.shop.ClientServiceRest.Service.OrderService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private ClientService clientService;
    private OrderService orderService;

    private static final String ACCESS_BY_ID_OR_NOT_USER_ROLE = "#authClient.id == #id" +
            " || hasAnyRole('ADMIN', 'MANAGER')";
    private static final String ACCESS_BY_USERNAME_OR_NOT_USER_ROLE = "#authClient.login == #login" +
            " || hasAnyRole('ADMIN', 'MANAGER')";
    private static final String ACCESS_BY_CONFIRM_CODE_OR_NOT_USER_ROLE = "#authClient.confirmationCode == #code" +
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

    @ApiOperation(value = "Show list of clients")
    @GetMapping(params = {"page", "size"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Client>> listOfClients(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                      @RequestParam("page") int page,
                                                      @RequestParam("size") int size) {
        logger.info("Called listOfClients method");
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Client> allClients = clientService.findAll(pageable);

        return new ResponseEntity<>(allClients, HttpStatus.OK);
    }

    @ApiOperation(value = "Show client by id")
    @GetMapping("/{id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Client> clientById(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                             @PathVariable("id") Long id) {
        logger.info("Called clientById method");

        try {
            Client client = clientService.findById(id);
            return new ResponseEntity<>(client, HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            logger.warn("Client with id " + id + " not found");
            logger.error(ex.toString());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Show client by login")
    @GetMapping("/byLogin/{login}")
    @PreAuthorize(ACCESS_BY_USERNAME_OR_NOT_USER_ROLE)
    public ResponseEntity<Client> clientByLogin(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                @PathVariable("login") String login) {
        logger.info("Called clientByLogin method");

        Client client = clientService.findByLogin(login);
        if (client == null) {
            logger.warn("Client with login - " + login + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    @ApiOperation(value = "Show client by confirmation code")
    @GetMapping("/byConfirmCode/{code}")
    @PreAuthorize(ACCESS_BY_CONFIRM_CODE_OR_NOT_USER_ROLE)
    public ResponseEntity<Client> clientByConfirmCode(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                      @PathVariable("code") String code) {
        logger.info("Called clientByConfirmCode method");

        Client client = clientService.findByConfirmationCode(code);
        if (client == null) {
            logger.warn("Client with confirmation code - " + code + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    @ApiOperation(value = "Update exists client")
    @PutMapping("/{id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Client> updateClient(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                               @PathVariable("id") Long id,
                                               @RequestBody @Valid Client client,
                                               BindingResult bindingResult) {
        logger.info("Called updateClient method");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on update client information");
            return new ResponseEntity<>(client, HttpStatus.BAD_REQUEST);
        }

        try {
            Client persistentClient = clientService.findById(id);

            BeanUtils.copyProperties(client, persistentClient, "id");
            clientService.save(persistentClient);
            return new ResponseEntity<>(persistentClient, HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            logger.warn("Client with id - " + id + " not found");
            logger.error(ex.toString());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Create new client")
    @PostMapping
    public ResponseEntity<Client> createNewClient(@RequestBody @Valid Client client,
                                                  BindingResult bindingResult) {
        logger.info("Called createNewClient method");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on update client information");
            return new ResponseEntity<>(client, HttpStatus.BAD_REQUEST);
        }

        clientService.save(client);
        return new ResponseEntity<>(client, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Delete client")
    @DeleteMapping("/{id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void deleteClient(@ApiIgnore @AuthenticationPrincipal Client authClient,
                             @PathVariable("id") Long id) {
        logger.info("Called deleteClient method");

        try {
            Client client = clientService.findById(id);
            clientService.delete(client);
        } catch (NoSuchElementException ex) {
            logger.warn("Client with id - " + id + " not found");
            logger.error(ex.toString());
        }
    }

    @ApiOperation(value = "Show list of not completed orders", notes = "Only for managers or admins")
    @GetMapping(value = "/managerOrders", params = {"page", "size"})
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<Order>> getOrdersForManagers(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                            @RequestParam("page") int page,
                                                            @RequestParam("size") int size) {
        logger.info("Called getOrdersForManagers method");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Order> orders = orderService.findOrdersForManagers(pageable);

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @ApiOperation(value = "Show order by id", notes = "Only for managers or admins")
    @GetMapping("/orders/{order_id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Order> getOrderById(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                              @PathVariable("order_id") Long orderId) {
        logger.info("Called getOrderById method");

        Order order = orderService.findById(orderId);

        if (order == null) {
            logger.warn("Order with id - " + orderId + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
