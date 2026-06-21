package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import com.kupreu.api.DTOs.Attribute.AttributeRequest;
import com.kupreu.api.DTOs.Attribute.AttributeResponse;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.entity.Attribute;
import com.kupreu.api.repository.AttributeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AttributeService {
    private final AttributeRepository repository;
    private final AuditService auditService;

    public List<AttributeResponse> getAll(){
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AttributeResponse getById(UUID id){
        Attribute att = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found"));

        return toResponse(att);
    }

    @Transactional
    public AttributeResponse create(AttributeRequest request){
        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("A name is required");
        }

        Attribute att = Attribute.builder()
                .name(request.getName())
                .build();

        att = repository.save(att);

        auditService.record("ATTRIBUTE_CREATED", "Attribute created",
                "id=" + att.getId() + ", name=" + att.getName(), true);

        return toResponse(att);
    }

    @Transactional
    public AttributeResponse update (UUID id, AttributeRequest request){
        Attribute att = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found"));

        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("A name is required");
        }

        att.setName(request.getName());
        repository.save(att);

        auditService.record("ATTRIBUTE_UPDATED", "Attribute updated",
                "id=" + id + ", name=" + att.getName(), true);

        return toResponse(att);
    }

    @Transactional
    public void delete(UUID id){
        Attribute att = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found"));

        repository.delete(att);

        auditService.record("ATTRIBUTE_DELETED", "Attribute deleted", "id=" + id, true);
    }

    private AttributeResponse toResponse(Attribute attribute){
        return AttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .build();
    }
}
