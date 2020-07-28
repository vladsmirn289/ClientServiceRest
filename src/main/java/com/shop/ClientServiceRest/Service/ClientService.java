package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.Client;
import com.shop.ClientServiceRest.Model.ClientItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface ClientService extends UserDetailsService {
    Client findById(Long id);
    Page<Client> findAll(Pageable pageable);
    Client findByLogin(String login);
    Client findByConfirmationCode(String confirmationCode);

    void save(Client client);

    void delete(Client client);
//  void deleteBasketItems(Set<ClientItem> itemSet, String login);
}
