package com.shop.ClientServiceRest.Service;

import com.shop.ClientServiceRest.Model.ClientItem;

import java.util.Set;

public interface ClientItemService {
    double generalPrice(Set<ClientItem> basket);
    double generalWeight(Set<ClientItem> basket);

    ClientItem findById(Long id);

    void save(ClientItem clientItem);

    //void delete(ClientItem clientItem);
    //void deleteSetItems(Set<ClientItem> clientItemSet);
}
