package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Repository.ClientItemRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ClientItemServiceImpl implements ClientItemService {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    private ClientItemRepo clientItemRepo;

    @Autowired
    public void setClientItemRepo(ClientItemRepo clientItemRepo) {
        logger.debug("Setting clientItemRepo");
        this.clientItemRepo = clientItemRepo;
    }

    @Override
    public double generalPrice(Set<ClientItem> basket) {
        return basket.stream()
                .map(item -> item.getItem().getPrice() * item.getQuantity())
                .reduce(Double::sum).orElse(0D);
    }

    @Override
    public double generalWeight(Set<ClientItem> basket) {
        return basket.stream()
                .map(item -> item.getItem().getWeight() * item.getQuantity())
                .reduce(Double::sum).orElse(0D);
    }

    @Override
    @Cacheable(value = "basket")
    public ClientItem findById(Long id) {
        logger.info("Finding client item by id = " + id);
        return clientItemRepo.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void save(ClientItem clientItem) {
        logger.info("Saving client item to database");
        clientItemRepo.save(clientItem);
    }
}
