package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import com.kupreu.api.DTOs.UnitOfMeasure.UnitOfMeasureRequest;
import com.kupreu.api.DTOs.UnitOfMeasure.UnitOfMeasureResponse;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.entity.UnitOfMeasure;
import com.kupreu.api.repository.UnitOfMeasureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service holding the business logic for {@link UnitOfMeasure} management.
 * Every mutating operation is recorded through the {@link AuditService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UnitOfMeasureService {
    private final UnitOfMeasureRepository repository;
    private final AuditService auditService;

    /**
     * Returns all units of measure.
     *
     * @return every unit as a response DTO
     */
    public List<UnitOfMeasureResponse> getAll(){
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a single unit of measure by its identifier.
     *
     * @param id the unit identifier
     * @return the matching unit as a response DTO
     * @throws NotFoundException if no unit has the given id
     */
    public UnitOfMeasureResponse getById(UUID id){
        UnitOfMeasure unit = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit of measure not found"));

        return toResponse(unit);
    }

    /**
     * Updates the name and/or symbol of an existing unit of measure.
     *
     * @param id      the unit identifier
     * @param request the new unit data
     * @return the updated unit as a response DTO
     * @throws NotFoundException if no unit has the given id
     */
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

        auditService.record("UNIT_OF_MEASURE_UPDATED", "Unit of measure updated",
                "id=" + id + ", name=" + unit.getName(), true);

        return getById(unit.getId());
    }

    /**
     * Creates a new unit of measure.
     *
     * @param request the unit data; both name and symbol are required
     * @return the created unit as a response DTO
     * @throws BadRequestException if the name or symbol is missing or blank
     */
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

        auditService.record("UNIT_OF_MEASURE_CREATED", "Unit of measure created",
                "id=" + unit.getId() + ", name=" + unit.getName(), true);

        return getById(unit.getId());
    }

    /**
     * Deletes the unit of measure with the given identifier.
     *
     * @param id the unit identifier
     * @throws NotFoundException if no unit has the given id
     */
    @Transactional
    public void delete(UUID id){
        UnitOfMeasure unit = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit of measure not found"));

        repository.delete(unit);

        auditService.record("UNIT_OF_MEASURE_DELETED", "Unit of measure deleted", "id=" + id, true);
    }

    /** Maps a {@link UnitOfMeasure} entity to its response DTO. */
    private UnitOfMeasureResponse toResponse(UnitOfMeasure unit){
        return UnitOfMeasureResponse.builder()
                .id(unit.getId())
                .name(unit.getName())
                .symbol(unit.getSymbol())
                .build();
    }
}
