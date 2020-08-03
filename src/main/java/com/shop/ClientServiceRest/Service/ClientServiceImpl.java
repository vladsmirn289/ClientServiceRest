package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Order;
import com.shop.ClientServiceRest.Model.Role;
import com.shop.ClientServiceRest.Repository.ClientRepo;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    private ClientRepo clientRepo;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setClientRepo(ClientRepo clientRepo) {
        logger.debug("Setting clientRepo");
        this.clientRepo = clientRepo;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        logger.debug("Setting passwordEncoder");
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Client findById(Long id) {
        logger.info("Find client by id - " + id);
        return clientRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> findAll(Pageable pageable) {
        logger.info("Find all clients with pagination");
        return clientRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Client findByLogin(String login) {
        logger.info("Find client by login - " + login);
        return clientRepo.findByLogin(login);
    }

    @Override
    @Transactional(readOnly = true)
    public Client findByConfirmationCode(String confirmationCode) {
        logger.info("Find client by confirmation code - " + confirmationCode);
        return clientRepo.findByConfirmationCode(confirmationCode);
    }

    @Override
    public List<ClientItem> findBasketItemsByClientId(Long id) {
        logger.info("Find basket items by client id - " + id);
        Client client = clientRepo.findById(id).orElseThrow(NoSuchElementException::new);
        Hibernate.initialize(client.getBasket());

        return new ArrayList<>(client.getBasket());
    }

    @Override
    public List<Order> findOrdersByClientId(Long id) {
        logger.info("Find orders by client id - " + id);
        Client client = clientRepo.findById(id).orElseThrow(NoSuchElementException::new);
        Hibernate.initialize(client.getOrders());

        return new ArrayList<>(client.getOrders());
    }

    @Override
    public void save(Client client) {
        if (findByLogin(client.getLogin()) == null) {
            logger.info("Saving client to database without confirmation");
            if (client.getRoles().isEmpty()) {
                client.setRoles(Collections.singleton(Role.USER));
            }
        } else {
            logger.info("Update client");
            if (client.getConfirmationCode() != null) {
                String password = client.getPassword();
                if (!password.startsWith("$2a$")) {
                    client.setConfirmationCode(null);
                    password = passwordEncoder.encode(password);
                    client.setPassword(password);
                }
            }
        }

        clientRepo.save(client);
    }

    @Override
    public void delete(Client client) {
        logger.info("Deleting client with id = " + client.getId() + " from database");
        clientRepo.delete(client);
    }

    @Override
    public void deleteBasketItems(Set<ClientItem> itemSet, Long id) {
        logger.info("Called deleteBasketItems method");
        Client client = clientRepo.findById(id).orElseThrow(NoSuchElementException::new);

        Set<ClientItem> basketItems = client.getBasket()
                .stream().map(clientItem -> {
            if (!itemSet.contains(clientItem)) {
                return clientItem;
            } else {
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        client.setBasket(basketItems);
        save(client);
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        logger.info("LoadingUserByUsername called");
        Client client = findByLogin(login);

        if (client == null) {
            logger.warn("Client with login - " + login + " not found");
            throw new UsernameNotFoundException("Client not found");
        }

        return client;
    }
}
