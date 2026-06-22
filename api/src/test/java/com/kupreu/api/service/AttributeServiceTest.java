package com.kupreu.api.service;

import com.kupreu.api.audit.AuditService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kupreu.api.DTOs.Attribute.AttributeRequest;
import com.kupreu.api.DTOs.Attribute.AttributeResponse;
import com.kupreu.api.entity.Attribute;
import com.kupreu.api.repository.AttributeRepository;

@ExtendWith(MockitoExtension.class)
class AttributeServiceTest {

    @Mock private AttributeRepository repository;
    @Mock private AuditService auditService;
    @InjectMocks private AttributeService service;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private Attribute entity(String name) {
        return Attribute.builder().Id(ID).name(name).build();
    }

    private AttributeRequest request(String name) {
        AttributeRequest req = new AttributeRequest();
        req.setName(name);
        return req;
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(entity("Color"), entity("Size")));

        List<AttributeResponse> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Color");
    }

    @Test
    void getAll_empty_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        when(repository.findById(ID)).thenReturn(Optional.of(entity("Color")));

        AttributeResponse res = service.getById(ID);

        assertThat(res.getId()).isEqualTo(ID);
        assertThat(res.getName()).isEqualTo("Color");
    }

    @Test
    void getById_notFound_throws() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attribute not found");
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    void create_validName_saves() {
        when(repository.save(any(Attribute.class))).thenReturn(entity("Color"));

        AttributeResponse res = service.create(request("Color"));

        assertThat(res.getName()).isEqualTo("Color");
        verify(repository).save(any(Attribute.class));
    }

    @Test
    void create_nullName_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> service.create(request(null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("A name is required");

        verify(repository, never()).save(any());
    }

    @Test
    void create_blankName_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> service.create(request("  ")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("A name is required");

        verify(repository, never()).save(any());
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    void update_found_updatesName() {
        Attribute existing = entity("Color");
        when(repository.findById(ID)).thenReturn(Optional.of(existing));

        AttributeResponse res = service.update(ID, request("Material"));

        assertThat(res.getName()).isEqualTo("Material");
        assertThat(existing.getName()).isEqualTo("Material");
        verify(repository).save(existing);
    }

    @Test
    void update_notFound_throws() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ID, request("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attribute not found");

        verify(repository, never()).save(any());
    }

    @Test
    void update_nullName_throwsAndDoesNotSave() {
        when(repository.findById(ID)).thenReturn(Optional.of(entity("Color")));

        assertThatThrownBy(() -> service.update(ID, request(null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("A name is required");

        verify(repository, never()).save(any());
    }

    @Test
    void update_blankName_throwsAndDoesNotSave() {
        when(repository.findById(ID)).thenReturn(Optional.of(entity("Color")));

        assertThatThrownBy(() -> service.update(ID, request("  ")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("A name is required");

        verify(repository, never()).save(any());
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletes() {
        Attribute existing = entity("Color");
        when(repository.findById(ID)).thenReturn(Optional.of(existing));

        service.delete(ID);

        verify(repository).delete(existing);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attribute not found");

        verify(repository, never()).delete(any(Attribute.class));
    }
}
