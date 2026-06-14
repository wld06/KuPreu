package com.kupreu.api.service;

import com.kupreu.api.DTOs.Store.StoreRequest;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.entity.PostalCode;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.repository.PostalCodeRepository;
import com.kupreu.api.repository.StoreRepository;
import com.kupreu.api.repository.SupermarketChainRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final SupermarketChainRepository smChainRepository;
    private final PostalCodeRepository postalCodeRepository;

    public List<StoreResponse> getAll(){
        return storeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public StoreResponse getById(UUID id){
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return toResponse(store);
    }

    public StoreResponse create(StoreRequest request){
        SupermarketChain smChain = smChainRepository.findById(request.getSupermarketChainId())
                .orElseThrow(() -> new RuntimeException("A supermarket cchain is required"));

        PostalCode postalCode = postalCodeRepository.findByCode(request.postalCode)
                .orElseThrow(() -> new RuntimeException("A postal code is required"));

        if (request.address == null || request.address.isBlank()){
            throw new RuntimeException("An address is required");
        }

        Store store = Store.builder()
                .address(request.getAddress())
                .supermarketChain(smChain)
                .postalCode(postalCode)
                .build();

        store = storeRepository.save(store);

        return getById(store.getId());
    }

    public StoreResponse update(UUID id, StoreRequest request){

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (request.getSupermarketChainId() != null){
            SupermarketChain smChain = smChainRepository.findById(request.getSupermarketChainId())
                    .orElseThrow(() -> new RuntimeException("A supermarket cchain is required"));

            store.setSupermarketChain(smChain);
        }

        if (request.getPostalCode() != null && !request.getPostalCode().isBlank()){
            PostalCode postalCode = postalCodeRepository.findByCode(request.postalCode)
                    .orElseThrow(() -> new RuntimeException("A postal code is required"));

            store.setPostalCode(postalCode);
        }

        if (request.address == null || request.address.isBlank()){
            throw new RuntimeException("An address is required");
        }

        store.setAddress(request.getAddress());
        storeRepository.save(store);

        return getById(store.getId());
    }

    public void delete(UUID id){
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        storeRepository.delete(store);
    }

    private StoreResponse toResponse(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .address(store.getAddress())
                .chain(store.getSupermarketChain().getName())
                .build();
    }
}
