package com.kupreu.api.service;

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

import com.kupreu.api.DTOs.Brand.BrandRequest;
import com.kupreu.api.DTOs.Brand.BrandResponse;
import com.kupreu.api.entity.Brand;
import com.kupreu.api.repository.BrandRepository;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock private BrandRepository brandRepository;
    @InjectMocks private BrandService brandService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private Brand entity(String name) {
        return Brand.builder().Id(ID).name(name).build();
    }

    private BrandRequest request(String name) {
        BrandRequest req = new BrandRequest();
        req.setName(name);
        return req;
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_noFilter_returnsAll() {
        when(brandRepository.findAll()).thenReturn(List.of(entity("Hacendado"), entity("Pascual")));

        List<BrandResponse> result = brandService.getAll(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Hacendado");
    }

    @Test
    void getAll_blankFilter_returnsAll() {
        when(brandRepository.findAll()).thenReturn(List.of(entity("Hacendado")));

        assertThat(brandService.getAll("   ")).hasSize(1);
        verify(brandRepository, never()).findByName(any());
    }

    @Test
    void getAll_withFilter_delegatesToFindByName() {
        when(brandRepository.findByName("Pascual")).thenReturn(List.of(entity("Pascual")));

        List<BrandResponse> result = brandService.getAll("Pascual");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Pascual");
        verify(brandRepository, never()).findAll();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        when(brandRepository.findById(ID)).thenReturn(Optional.of(entity("Hacendado")));

        BrandResponse res = brandService.getById(ID);

        assertThat(res.getId()).isEqualTo(ID);
        assertThat(res.getName()).isEqualTo("Hacendado");
    }

    @Test
    void getById_notFound_throws() {
        when(brandRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.getById(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    void create_validName_saves() {
        when(brandRepository.save(any(Brand.class))).thenReturn(entity("Hacendado"));

        BrandResponse res = brandService.create(request("Hacendado"));

        assertThat(res.getName()).isEqualTo("Hacendado");
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void create_nullName_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> brandService.create(request(null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Name is required");

        verify(brandRepository, never()).save(any());
    }

    @Test
    void create_blankName_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> brandService.create(request("  ")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Name is required");

        verify(brandRepository, never()).save(any());
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    void update_found_updatesName() {
        Brand existing = entity("Hacendado");
        when(brandRepository.findById(ID)).thenReturn(Optional.of(existing));

        BrandResponse res = brandService.update(ID, request("Hacendado Premium"));

        assertThat(res.getName()).isEqualTo("Hacendado Premium");
        assertThat(existing.getName()).isEqualTo("Hacendado Premium");
        verify(brandRepository).save(existing);
    }

    @Test
    void update_notFound_throws() {
        when(brandRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.update(ID, request("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Brand not found");

        verify(brandRepository, never()).save(any());
    }

    @Test
    void update_blankName_throwsAndDoesNotSave() {
        when(brandRepository.findById(ID)).thenReturn(Optional.of(entity("Hacendado")));

        assertThatThrownBy(() -> brandService.update(ID, request("  ")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Name is required");

        verify(brandRepository, never()).save(any());
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletes() {
        Brand existing = entity("Hacendado");
        when(brandRepository.findById(ID)).thenReturn(Optional.of(existing));

        brandService.delete(ID);

        verify(brandRepository).delete(existing);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(brandRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.delete(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Brand not found");

        verify(brandRepository, never()).delete(any(Brand.class));
    }
}
