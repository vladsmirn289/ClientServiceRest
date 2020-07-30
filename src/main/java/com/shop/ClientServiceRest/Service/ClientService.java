package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import com.shop.ClientServiceRest.Model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

public interface ClientService extends UserDetailsService {
    Client findById(Long id);
    Page<Client> findAll(Pageable pageable);
    Client findByLogin(String login);
    Client findByConfirmationCode(String confirmationCode);
    List<ClientItem> findBasketItemsByClientId(Long id);
    List<Order> findOrdersByClientId(Long id);

    void save(Client client);

    void delete(Client client);
    void deleteBasketItems(Set<ClientItem> itemSet, Long id);
}
