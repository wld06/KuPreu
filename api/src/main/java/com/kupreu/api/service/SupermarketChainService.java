package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;

import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainRequest;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainWithStoresResponse;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.repository.SupermarketChainRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SupermarketChainService {
    private final SupermarketChainRepository supermarketChainRepository;

    public List<SupermarketChainResponse> getAll(){
        return supermarketChainRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SupermarketChainWithStoresResponse getById(UUID id){
        SupermarketChain smChain = supermarketChainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supermarket chain not found"));

        return toResponseWithStore(smChain);
    }

    public SupermarketChainWithStoresResponse create(SupermarketChainRequest request){
        SupermarketChain smChain = SupermarketChain.builder()
                .name(request.getName())
                .build();

        smChain = supermarketChainRepository.save(smChain);

        return getById(smChain.getId());
    }

    public SupermarketChainWithStoresResponse update(UUID id, SupermarketChainRequest request){
        SupermarketChain smChain = supermarketChainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supermarket chain not found"));

        smChain.setName(request.getName());
        supermarketChainRepository.save(smChain);

        return getById(smChain.getId());
    }

    public void delete(UUID id){
        SupermarketChain smChain = supermarketChainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supermarket chain not found"));

        supermarketChainRepository.delete(smChain);
    }

    private SupermarketChainWithStoresResponse toResponseWithStore(SupermarketChain supermarketChain){

        return SupermarketChainWithStoresResponse.builder()
                .id(supermarketChain.getId())
                .name(supermarketChain.getName())
                .stores(supermarketChain
                        .getSupermarkets()
                        .stream()
                        .map(this::toResponseStore)
                        .collect(Collectors.toList())
                )
                .build();
    }

    private StoreResponse toResponseStore(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .address(store.getAddress())
                .build();
    }

    private SupermarketChainResponse toResponse(SupermarketChain supermarketChain){
        return SupermarketChainResponse.builder()
                .id(supermarketChain.getId())
                .name(supermarketChain.getName())
                .storeCount(supermarketChain.getSupermarkets().size())
                .build();
    }
}
