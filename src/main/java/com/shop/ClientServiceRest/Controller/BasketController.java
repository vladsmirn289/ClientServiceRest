package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Service.ClientItemService;
import com.shop.ClientServiceRest.Service.ClientService;
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

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

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

    @GetMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<List<ClientItem>> getBasketByClientId(@AuthenticationPrincipal Client authClient,
                                                                @PathVariable("id") Long id) {
        logger.info("Called getBasketByClientId method");

        try {
            List<ClientItem> basket = clientService.findBasketItemsByClientId(id);
            return new ResponseEntity<>(basket, HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            logger.warn("Client with id - " + id + " not found");
            logger.error(ex.toString());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/generalPrice")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Double> calcGeneralPriceOfBasket(@AuthenticationPrincipal Client authClient,
                                                           @PathVariable("id") Long id) {
        logger.info("Called calcGeneralPriceOfBasket method");
        List<ClientItem> basket = getBasketByClientId(authClient, id).getBody();
        if (basket == null) {
            return new ResponseEntity<>(0D, HttpStatus.OK);
        }

        Double generalPrice = clientItemService.generalPrice(new HashSet<>(basket));
        return new ResponseEntity<>(generalPrice, HttpStatus.OK);
    }

    @GetMapping("/generalWeight")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<Double> calcGeneralWeightOfBasket(@AuthenticationPrincipal Client authClient,
                                                            @PathVariable("id") Long id) {
        logger.info("Called calcGeneralWeightOfBasket method");
        List<ClientItem> basket = getBasketByClientId(authClient, id).getBody();
        if (basket == null) {
            return new ResponseEntity<>(0D, HttpStatus.OK);
        }

        Double generalWeight = clientItemService.generalWeight(new HashSet<>(basket));
        return new ResponseEntity<>(generalWeight, HttpStatus.OK);
    }

    @GetMapping("/{item_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<ClientItem> getItemOfBasketById(@AuthenticationPrincipal Client authClient,
                                                          @PathVariable("id") Long id,
                                                          @PathVariable("item_id") Long itemId) {
        logger.info("Called getItemOfBasketById method");

        try {
            ClientItem itemInBasket = clientItemService.findById(itemId);
            return new ResponseEntity<>(itemInBasket, HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            logger.warn("ClientItem with id - " + itemId + " not found");
            logger.error(ex.toString());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{item_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<ClientItem> updateItemInBasket(@AuthenticationPrincipal Client authClient,
                                                         @PathVariable("id") Long id,
                                                         @PathVariable("item_id") Long itemId,
                                                         @RequestBody @Valid ClientItem clientItem,
                                                         BindingResult bindingResult) {
        logger.info("Called updateItemInBasket method");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on update clientItem information");
            return new ResponseEntity<>(clientItem, HttpStatus.BAD_REQUEST);
        }

        try {
            ClientItem persistentItem = clientItemService.findById(itemId);

            BeanUtils.copyProperties(clientItem, persistentItem, "id");
            clientItemService.save(persistentItem);
            return new ResponseEntity<>(persistentItem, HttpStatus.OK);
        } catch (NoSuchElementException ex) {
            logger.warn("ClientItem with id - " + itemId + " not found");
            logger.error(ex.toString());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public ResponseEntity<ClientItem> addItemToBasket(@AuthenticationPrincipal Client authClient,
                                                      @PathVariable("id") Long id,
                                                      @RequestBody @Valid ClientItem clientItem,
                                                      BindingResult bindingResult) {
        logger.info("Called addItemToBasket method");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on clientItem information");
            return new ResponseEntity<>(clientItem, HttpStatus.BAD_REQUEST);
        }

        List<ClientItem> basket = clientService.findBasketItemsByClientId(id);
        basket.add(clientItem);
        authClient.setBasket(new HashSet<>(basket));
        clientService.save(authClient);

        return new ResponseEntity<>(clientItem, HttpStatus.OK);
    }

    @DeleteMapping("/{item_id}")
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void deleteItemFromBasketById(@AuthenticationPrincipal Client authClient,
                                         @PathVariable("id") Long id,
                                         @PathVariable("item_id") Long itemId) {
        logger.info("Called deleteItemFromBasketById method");

        ClientItem itemInBasket = getItemOfBasketById(authClient, authClient.getId(), itemId).getBody();
        if (itemInBasket == null) {
            return;
        }

        clientService.deleteBasketItems(Collections.singleton(itemInBasket), id);
    }

    @DeleteMapping
    @PreAuthorize(ACCESS_BY_ID_OR_NOT_USER_ROLE)
    public void clearBasketByClientId(@AuthenticationPrincipal Client authClient,
                                      @PathVariable("id") Long id) {
        logger.info("Called clearBasketByClientId method");

        List<ClientItem> basket = getBasketByClientId(authClient, id).getBody();
        if (basket == null || basket.isEmpty()) {
            return;
        }

        clientService.deleteBasketItems(new HashSet<>(basket), id);
    }
}
