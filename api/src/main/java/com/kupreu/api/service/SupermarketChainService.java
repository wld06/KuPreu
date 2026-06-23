package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;

import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainRequest;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainWithStoresResponse;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.repository.SupermarketChainRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service holding the business logic for {@link SupermarketChain} management,
 * including resolving each chain's stores. Every mutating operation is recorded
 * through the {@link AuditService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SupermarketChainService {
    private final SupermarketChainRepository supermarketChainRepository;
    private final AuditService auditService;

    /**
     * Returns all supermarket chains together with their store counts.
     *
     * @return every chain as a response DTO
     */
    public List<SupermarketChainResponse> getAll(){
        return supermarketChainRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a single chain, with its stores, by identifier.
     *
     * @param id the chain identifier
     * @return the matching chain including its stores
     * @throws NotFoundException if no chain has the given id
     */
    public SupermarketChainWithStoresResponse getById(UUID id){
        SupermarketChain smChain = supermarketChainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supermarket chain not found"));

        return toResponseWithStore(smChain);
    }

    /**
     * Creates a new supermarket chain.
     *
     * @param request the chain data
     * @return the created chain including its (initially empty) stores
     */
    @Transactional
    public SupermarketChainWithStoresResponse create(SupermarketChainRequest request){
        SupermarketChain smChain = SupermarketChain.builder()
                .name(request.getName())
                .build();

        smChain = supermarketChainRepository.save(smChain);

        auditService.record("SUPERMARKET_CHAIN_CREATED", "Supermarket chain created",
                "id=" + smChain.getId() + ", name=" + smChain.getName(), true);

        return getById(smChain.getId());
    }

    /**
     * Updates the name of an existing supermarket chain.
     *
     * @param id      the chain identifier
     * @param request the new chain data
     * @return the updated chain including its stores
     * @throws NotFoundException if no chain has the given id
     */
    @Transactional
    public SupermarketChainWithStoresResponse update(UUID id, SupermarketChainRequest request){
        SupermarketChain smChain = supermarketChainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supermarket chain not found"));

        smChain.setName(request.getName());
        supermarketChainRepository.save(smChain);

        auditService.record("SUPERMARKET_CHAIN_UPDATED", "Supermarket chain updated",
                "id=" + id + ", name=" + smChain.getName(), true);

        return getById(smChain.getId());
    }

    /**
     * Deletes the supermarket chain with the given identifier.
     *
     * @param id the chain identifier
     * @throws NotFoundException if no chain has the given id
     */
    @Transactional
    public void delete(UUID id){
        SupermarketChain smChain = supermarketChainRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supermarket chain not found"));

        supermarketChainRepository.delete(smChain);

        auditService.record("SUPERMARKET_CHAIN_DELETED", "Supermarket chain deleted", "id=" + id, true);
    }

    /** Maps a {@link SupermarketChain} entity to a response DTO including its stores. */
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

    /** Maps a {@link Store} entity to its response DTO (without chain name). */
    private StoreResponse toResponseStore(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .address(store.getAddress())
                .build();
    }

    /** Maps a {@link SupermarketChain} entity to a summary response DTO with its store count. */
    private SupermarketChainResponse toResponse(SupermarketChain supermarketChain){
        return SupermarketChainResponse.builder()
                .id(supermarketChain.getId())
                .name(supermarketChain.getName())
                .storeCount(supermarketChain.getSupermarkets().size())
                .build();
    }
}
