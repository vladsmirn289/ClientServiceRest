package com.shop.ClientServiceRest.Controller;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private ClientService clientService;

    @Autowired
    public void setClientService(ClientService clientService) {
        logger.debug("Setting clientService");
        this.clientService = clientService;
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<List<Client>> listOfClients(@RequestParam("page") int page,
                                                      @RequestParam("size") int size) {
        logger.info("Called listOfClients method");
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        List<Client> allClients = clientService.findAll(pageable).getContent();

        return new ResponseEntity<>(allClients, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> clientById(@PathVariable("id") Long id) {
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

    @GetMapping("/byLogin/{login}")
    public ResponseEntity<Client> clientByLogin(@PathVariable("login") String login) {
        logger.info("Called clientByLogin method");

        Client client = clientService.findByLogin(login);
        if (client == null) {
            logger.warn("Client with login - " + login + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    @GetMapping("/byConfirmCode/{code}")
    public ResponseEntity<Client> clientByConfirmCode(@PathVariable("code") String code) {
        logger.info("Called clientByConfirmCode method");

        Client client = clientService.findByConfirmationCode(code);
        if (client == null) {
            logger.warn("Client with confirmation code - " + code + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable("id") Long id,
                                               @RequestBody @Valid Client client,
                                               BindingResult bindingResult) {
        logger.info("Called updateClient method");

        if (bindingResult.hasErrors()) {
            logger.info("Bad request on update client information");
            return new ResponseEntity<>(client, HttpStatus.BAD_REQUEST);
        }

        Client persistentClient = clientService.findById(id);
        if (persistentClient == null) {
            logger.warn("Client with id - " + id + " not found");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        BeanUtils.copyProperties(client, persistentClient, "id");
        clientService.save(persistentClient);
        return new ResponseEntity<>(persistentClient, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Client> createNewClient(@RequestBody Client client) {
        logger.info("Called createNewClient method");

        clientService.save(client);
        return new ResponseEntity<>(client, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable("id") Long id) {
        logger.info("Called deleteClient method");

        Client client = clientService.findById(id);
        if (client == null) {
            logger.warn("Client with id - " + id + " not found");
        } else {
            clientService.delete(client);
        }
    }
}
