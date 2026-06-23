package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import com.kupreu.api.DTOs.Store.StoreRequest;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.entity.PostalCode;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.repository.PostalCodeRepository;
import com.kupreu.api.repository.StoreRepository;
import com.kupreu.api.repository.SupermarketChainRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service holding the business logic for {@link Store} management.
 * On write it resolves the referenced supermarket chain and postal code, and
 * every mutating operation is recorded through the {@link AuditService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    private final StoreRepository storeRepository;
    private final SupermarketChainRepository smChainRepository;
    private final PostalCodeRepository postalCodeRepository;
    private final AuditService auditService;

    /**
     * Returns all stores.
     *
     * @return every store as a response DTO
     */
    public List<StoreResponse> getAll(){
        return storeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a single store by its identifier.
     *
     * @param id the store identifier
     * @return the matching store as a response DTO
     * @throws NotFoundException if no store has the given id
     */
    public StoreResponse getById(UUID id){
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        return toResponse(store);
    }

    /**
     * Creates a new store linked to a supermarket chain and a postal code.
     *
     * @param request the store data (address, chain id and postal code)
     * @return the created store as a response DTO
     * @throws NotFoundException   if the chain or postal code does not exist
     * @throws BadRequestException if the address is missing or blank
     */
    @Transactional
    public StoreResponse create(StoreRequest request){
        SupermarketChain smChain = smChainRepository.findById(request.getSupermarketChainId())
                .orElseThrow(() -> new NotFoundException("A supermarket cchain is required"));

        PostalCode postalCode = postalCodeRepository.findByCode(request.getPostalCode())
                .orElseThrow(() -> new NotFoundException("A postal code is required"));

        if (request.getAddress() == null || request.getAddress().isBlank()){
            throw new BadRequestException("An address is required");
        }

        Store store = Store.builder()
                .address(request.getAddress())
                .supermarketChain(smChain)
                .postalCode(postalCode)
                .build();

        store = storeRepository.save(store);

        auditService.record("STORE_CREATED", "Store created",
                "id=" + store.getId() + ", address=" + store.getAddress(), true);

        return getById(store.getId());
    }

    /**
     * Updates an existing store. The chain and postal code are only changed when
     * provided in the request; the address is always required.
     *
     * @param id      the store identifier
     * @param request the new store data
     * @return the updated store as a response DTO
     * @throws NotFoundException   if the store, chain or postal code does not exist
     * @throws BadRequestException if the address is missing or blank
     */
    @Transactional
    public StoreResponse update(UUID id, StoreRequest request){

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        if (request.getSupermarketChainId() != null){
            SupermarketChain smChain = smChainRepository.findById(request.getSupermarketChainId())
                    .orElseThrow(() -> new NotFoundException("A supermarket cchain is required"));

            store.setSupermarketChain(smChain);
        }

        if (request.getPostalCode() != null && !request.getPostalCode().isBlank()){
            PostalCode postalCode = postalCodeRepository.findByCode(request.getPostalCode())
                    .orElseThrow(() -> new NotFoundException("A postal code is required"));

            store.setPostalCode(postalCode);
        }

        if (request.getAddress() == null || request.getAddress().isBlank()){
            throw new BadRequestException("An address is required");
        }

        store.setAddress(request.getAddress());
        storeRepository.save(store);

        auditService.record("STORE_UPDATED", "Store updated",
                "id=" + id + ", address=" + store.getAddress(), true);

        return getById(store.getId());
    }

    /**
     * Deletes the store with the given identifier.
     *
     * @param id the store identifier
     * @throws NotFoundException if no store has the given id
     */
    @Transactional
    public void delete(UUID id){
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        storeRepository.delete(store);

        auditService.record("STORE_DELETED", "Store deleted", "id=" + id, true);
    }

    /** Maps a {@link Store} entity to its response DTO. */
    private StoreResponse toResponse(Store store){
        return StoreResponse.builder()
                .id(store.getId())
                .address(store.getAddress())
                .chain(store.getSupermarketChain().getName())
                .build();
    }
}
