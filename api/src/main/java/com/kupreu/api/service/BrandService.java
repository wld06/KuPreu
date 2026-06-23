package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import com.kupreu.api.DTOs.Brand.BrandRequest;
import com.kupreu.api.DTOs.Brand.BrandResponse;
import com.kupreu.api.entity.Brand;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.audit.AuditService;
import com.kupreu.api.repository.BrandRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service holding the business logic for {@link Brand} management:
 * listing, lookup, creation, update and deletion. Every mutating operation is
 * recorded through the {@link AuditService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BrandService {
    private final BrandRepository brandRepository;
    private final AuditService auditService;

    /**
     * Returns all brands, optionally filtered by an exact name.
     *
     * @param brandName exact name to filter by; when {@code null} or blank, all brands are returned
     * @return the matching brands as response DTOs
     */
    public List<BrandResponse> getAll(String brandName){

        if (brandName != null && !brandName.isBlank()){
            return brandRepository.findByName(brandName)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return brandRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a single brand by its identifier.
     *
     * @param id the brand identifier
     * @return the matching brand as a response DTO
     * @throws NotFoundException if no brand has the given id
     */
    public BrandResponse getById(UUID id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand with id" + id + " not found"));

        return toResponse(brand);
    }

    /**
     * Updates the name of an existing brand.
     *
     * @param id      the brand identifier
     * @param request the new brand data; its name must not be blank
     * @return the updated brand as a response DTO
     * @throws NotFoundException   if no brand has the given id
     * @throws BadRequestException if the requested name is missing or blank
     */
    @Transactional
    public BrandResponse update(@NonNull UUID id, @NonNull BrandRequest request){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("Name is required");
        }

        brand.setName(request.getName());

        brandRepository.save(brand);

        auditService.record("BRAND_UPDATED", "Brand updated",
                "id=" + id + ", name=" + brand.getName(), true);

        return getById(brand.getId());
    }

    /**
     * Deletes the brand with the given identifier.
     *
     * @param id the brand identifier
     * @throws NotFoundException if no brand has the given id
     */
    @Transactional
    public void delete(UUID id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        brandRepository.delete(brand);

        auditService.record("BRAND_DELETED", "Brand deleted", "id=" + id, true);
    }

    /**
     * Creates a new brand.
     *
     * @param request the brand data; its name must not be blank
     * @return the created brand as a response DTO
     * @throws BadRequestException if the requested name is missing or blank
     */
    @Transactional
    public BrandResponse create(@NonNull BrandRequest request){
        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("Name is required");
        }

        Brand brand = Brand.builder()
                        .name(request.getName())
                        .build();

        Brand savedBrand = brandRepository.save(brand);

        auditService.record("BRAND_CREATED", "Brand created",
                "id=" + savedBrand.getId() + ", name=" + savedBrand.getName(), true);

        return toResponse(savedBrand);
    }

    /** Maps a {@link Brand} entity to its response DTO. */
    private BrandResponse toResponse(Brand brand){
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }
}