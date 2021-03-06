package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.Aop.BadRequestClientItemPointcut;
import com.shop.ClientServiceRest.Aop.NoSuchClientItemPointcut;
import com.shop.ClientServiceRest.Aop.NoSuchClientPointcut;
import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Service.ClientItemService;
import com.shop.ClientServiceRest.Service.ClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/clients/{id}/basket")
public class BasketController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private ClientService clientService;
    private ClientItemService clientItemService;

    private static final String ACCESS_BY_ID_OR_NOT_USER_ROLE = "#authClient.id == #id" +
            " || hasAnyRole('ADMIN', 'MANAGER')";

    @Autowired
    public void setClientService(ClientService clientService) {
        logger.debug("Setting clientService");
        this.clientService = clientService;
    }

    @Autowired
    public void setClientItemService(ClientItemService clientItemService) {
        logger.debug("Setting clientItemService");
        this.clientItemService = clientItemService;
    }

    @ApiOperation(value = "Show basket items by client")
    @NoSuchClientPointcut
    @GetMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<List<ClientItem>> getBasketByClientId(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                                @PathVariable("id") Long id) {
        logger.info("Called getBasketByClientId method");
        List<ClientItem> basket = clientService.findBasketItemsByClientId(id);

        return new ResponseEntity<>(basket, HttpStatus.OK);
    }

    @ApiOperation(value = "Calculate general price of items in the basket")
    @NoSuchClientPointcut
    @GetMapping("/generalPrice")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Double> calcGeneralPriceOfBasket(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                           @PathVariable("id") Long id) {
        logger.info("Called calcGeneralPriceOfBasket method");
        Client client = clientService.findById(id);

        List<ClientItem> basket = getBasketByClientId(client, id).getBody();
        if (basket == null) {
            return new ResponseEntity<>(0D, HttpStatus.OK);
        }

        Double generalPrice = clientItemService.generalPrice(new HashSet<>(basket));
        return new ResponseEntity<>(generalPrice, HttpStatus.OK);
    }

    @ApiOperation(value = "Calculate general weight of items in the basket")
    @NoSuchClientPointcut
    @GetMapping("/generalWeight")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Double> calcGeneralWeightOfBasket(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                            @PathVariable("id") Long id) {
        logger.info("Called calcGeneralWeightOfBasket method");
        Client client = clientService.findById(id);

        List<ClientItem> basket = getBasketByClientId(client, id).getBody();
        if (basket == null) {
            return new ResponseEntity<>(0D, HttpStatus.OK);
        }

        Double generalWeight = clientItemService.generalWeight(new HashSet<>(basket));
        return new ResponseEntity<>(generalWeight, HttpStatus.OK);
    }

    @ApiOperation(value = "Show item in the basket by id")
    @NoSuchClientItemPointcut
    @GetMapping("/{item_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<ClientItem> getItemOfBasketById(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                          @PathVariable("id") Long id,
                                                          @PathVariable("item_id") Long itemId) {
        logger.info("Called getItemOfBasketById method");
        ClientItem clientItem = clientItemService.findById(itemId);

        if (authClient.isManager() || authClient.isAdmin()) {
            return new ResponseEntity<>(clientItem, HttpStatus.OK);
        }

        List<ClientItem> basket = clientService.findBasketItemsByClientId(id);
        for (ClientItem c: basket) {
            if (c.getId().equals(itemId)) {
                return new ResponseEntity<>(c, HttpStatus.OK);
            }
        }

        logger.warn("Client with id - " + id + " not contain item with id - " + itemId + " in the basket");
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Update exists item in the basket")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request (invalid clientItem information)")
    })
    @BadRequestClientItemPointcut
    @NoSuchClientItemPointcut
    @PutMapping("/{item_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<ClientItem> updateItemInBasket(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                         @PathVariable("id") Long id,
                                                         @PathVariable("item_id") Long itemId,
                                                         @RequestBody @Valid ClientItem clientItem,
                                                         BindingResult bindingResult) {
        logger.info("Called updateItemInBasket method");

        ClientItem persistentItem = clientItemService.findById(itemId);
        BeanUtils.copyProperties(clientItem, persistentItem, "id");
        clientItemService.save(persistentItem);

        return new ResponseEntity<>(persistentItem, HttpStatus.OK);
    }

    @ApiOperation(value = "Create new item and place it in the basket")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request (invalid clientItem information)")
    })
    @BadRequestClientItemPointcut
    @NoSuchClientPointcut
    @PostMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<ClientItem> addItemToBasket(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                                      @PathVariable("id") Long id,
                                                      @RequestBody @Valid ClientItem clientItem,
                                                      BindingResult bindingResult) {
        logger.info("Called addItemToBasket method");
        Client client = clientService.findById(id);

        List<ClientItem> basket = clientService.findBasketItemsByClientId(id);
        List<Order> orders = clientService.findOrdersByClientId(id);
        basket.add(clientItem);
        client.setBasket(new HashSet<>(basket));
        client.setOrders(new HashSet<>(orders));
        clientService.save(client);

        return new ResponseEntity<>(clientItem, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete basket item by id")
    @NoSuchClientPointcut
    @DeleteMapping("/{item_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void deleteItemFromBasketById(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                         @PathVariable("id") Long id,
                                         @PathVariable("item_id") Long itemId) {
        logger.info("Called deleteItemFromBasketById method");

        Client client = clientService.findById(id);
        ClientItem itemInBasket = getItemOfBasketById(client, id, itemId).getBody();
        if (itemInBasket == null) {
            return;
        }

        clientService.deleteBasketItems(Collections.singleton(itemInBasket), id);
    }

    @ApiOperation(value = "Clear basket")
    @DeleteMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void clearBasketByClientId(@ApiIgnore @AuthenticationPrincipal Client authClient,
                                      @PathVariable("id") Long id) {
        logger.info("Called clearBasketByClientId method");

        Client client = clientService.findById(id);
        List<ClientItem> basket = getBasketByClientId(client, id).getBody();
        if (basket == null || basket.isEmpty()) {
            return;
        }

        clientService.deleteBasketItems(new HashSet<>(basket), id);
    }
}
