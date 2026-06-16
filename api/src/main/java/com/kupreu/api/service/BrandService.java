package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import com.kupreu.api.DTOs.Brand.BrandRequest;
import com.kupreu.api.DTOs.Brand.BrandResponse;
import com.kupreu.api.entity.Brand;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.repository.BrandRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class BrandService {
    private final BrandRepository brandRepository;

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

    public BrandResponse getById(UUID id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand with id" + id + " not found"));

        return toResponse(brand);
    }

    @Transactional
    public BrandResponse update(@NonNull UUID id, @NonNull BrandRequest request){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("Name is required");
        }

        brand.setName(request.getName());

        brandRepository.save(brand);

        return getById(brand.getId());
    }

    @Transactional
    public void delete(UUID id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        brandRepository.delete(brand);
    }

    @Transactional
    public BrandResponse create(@NonNull BrandRequest request){
        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("Name is required");
        }

        Brand brand = Brand.builder()
                        .name(request.getName())
                        .build();

        Brand savedBrand = brandRepository.save(brand);

        return toResponse(savedBrand);
    }

    private BrandResponse toResponse(Brand brand){
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }
}