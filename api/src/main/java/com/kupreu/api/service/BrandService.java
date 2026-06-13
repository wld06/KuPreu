package com.kupreu.api.service;

import com.kupreu.api.DTOs.Brand.BrandRequest;
import com.kupreu.api.DTOs.Brand.BrandResponse;
import com.kupreu.api.entity.Brand;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.kupreu.api.repository.BrandRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public List<BrandResponse> getAll(String brandName){

        if (brandName != null && !brandName.isBlank()){
            return brandRepository.findByBrandName(brandName)
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
                .orElseThrow(() -> new RuntimeException("Brand with id" + id + " not found"));

        return toResponse(brand);
    }

    public BrandResponse update(@NonNull UUID id, @NonNull BrandRequest request){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        if (request.getName() == null || request.getName().isBlank()){
            throw new RuntimeException("Name is required");
        }

        brand.setName(request.getName());

        brandRepository.save(brand);

        return getById(brand.getId());
    }

    public void delete(UUID id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        brandRepository.delete(brand);
    }

    public BrandResponse create(@NonNull BrandRequest request){
        if (request.getName() == null || request.getName().isBlank()){
            throw new RuntimeException("Name is required");
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