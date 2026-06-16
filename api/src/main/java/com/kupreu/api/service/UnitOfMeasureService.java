package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import com.kupreu.api.DTOs.UnitOfMeasure.UnitOfMeasureRequest;
import com.kupreu.api.DTOs.UnitOfMeasure.UnitOfMeasureResponse;
import com.kupreu.api.entity.UnitOfMeasure;
import com.kupreu.api.repository.UnitOfMeasureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UnitOfMeasureService {
    private final UnitOfMeasureRepository repository;

    public List<UnitOfMeasureResponse> getAll(){
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UnitOfMeasureResponse getById(UUID id){
        UnitOfMeasure unit = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit of measure not found"));

        return toResponse(unit);
    }

    @Transactional
    public UnitOfMeasureResponse update(UUID id, UnitOfMeasureRequest request){
        UnitOfMeasure unit = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit of measure not found"));

        if (request.getName() != null || !request.getName().isBlank()){
            unit.setName(request.getName());
        }

        if (request.getSymbol() != null || !request.getSymbol().isBlank()){
            unit.setSymbol(request.getSymbol());
        }

        repository.save(unit);

        return getById(unit.getId());
    }

    @Transactional
    public UnitOfMeasureResponse create(UnitOfMeasureRequest request){
        if (request.getName() == null || request.getName().isBlank()){
            throw new BadRequestException("A name is required");
        }

        if (request.getSymbol() == null || request.getSymbol().isBlank()){
            throw new BadRequestException("A symbol is required");
        }

        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name(request.getName())
                .symbol(request.getSymbol())
                .build();

        unit = repository.save(unit);

        return getById(unit.getId());
    }

    @Transactional
    public void delete(UUID id){
        UnitOfMeasure unit = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit of measure not found"));

        repository.delete(unit);
    }

    private UnitOfMeasureResponse toResponse(UnitOfMeasure unit){
        return UnitOfMeasureResponse.builder()
                .id(unit.getId())
                .name(unit.getName())
                .symbol(unit.getSymbol())
                .build();
    }
}
