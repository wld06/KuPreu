package com.kupreu.api.service;

import com.kupreu.api.DTOs.Attribute.AttributeRequest;
import com.kupreu.api.DTOs.Attribute.AttributeResponse;
import com.kupreu.api.entity.Attribute;
import com.kupreu.api.repository.AttributeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AttributeService {
    private final AttributeRepository repository;

    public List<AttributeResponse> getAll(){
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AttributeResponse getById(UUID id){
        Attribute att = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));

        return toResponse(att);
    }

    public AttributeResponse create(AttributeRequest request){
        if (request.getName() == null || request.getName().isBlank()){
            throw new RuntimeException("A name is required");
        }

        Attribute att = Attribute.builder()
                .name(request.getName())
                .build();

        att = repository.save(att);

        return toResponse(att);
    }

    public AttributeResponse update (UUID id, AttributeRequest request){
        Attribute att = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));

        if (request.getName() == null || request.getName().isBlank()){
            throw new RuntimeException("A name is required");
        }

        att.setName(request.getName());
        repository.save(att);

        return toResponse(att);
    }

    public void delete(UUID id){
        Attribute att = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute not found"));

        repository.delete(att);
    }

    private AttributeResponse toResponse(Attribute attribute){
        return AttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .build();
    }
}
