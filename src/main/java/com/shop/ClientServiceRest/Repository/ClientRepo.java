package com.shop.ClientServiceRest.Repository;

import com.shop.ClientServiceRest.Model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {
    Page<Client> findAll(Pageable pageable);
    Client findByLogin(String login);
    Client findByConfirmationCode(String confirmationCode);
}
