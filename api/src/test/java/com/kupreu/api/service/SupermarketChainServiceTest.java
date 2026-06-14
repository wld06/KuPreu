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

import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainRequest;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainWithStoresResponse;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.repository.SupermarketChainRepository;

@ExtendWith(MockitoExtension.class)
class SupermarketChainServiceTest {

    @Mock private SupermarketChainRepository supermarketChainRepository;
    @InjectMocks private SupermarketChainService supermarketChainService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private SupermarketChain entity(String name, Store... stores) {
        return SupermarketChain.builder().Id(ID).name(name).supermarkets(List.of(stores)).build();
    }

    private Store store(String address) {
        return Store.builder().id(UUID.randomUUID()).address(address).build();
    }

    private SupermarketChainRequest request(String name) {
        SupermarketChainRequest req = new SupermarketChainRequest();
        req.setName(name);
        return req;
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_mapsWithStoreCount() {
        when(supermarketChainRepository.findAll()).thenReturn(List.of(
                entity("Mercadona", store("Calle A"), store("Calle B")),
                entity("Lidl")
        ));

        List<SupermarketChainResponse> result = supermarketChainService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Mercadona");
        assertThat(result.get(0).getStoreCount()).isEqualTo(2);
        assertThat(result.get(1).getStoreCount()).isZero();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsWithStores() {
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.of(entity("Mercadona", store("Calle A"))));

        SupermarketChainWithStoresResponse res = supermarketChainService.getById(ID);

        assertThat(res.getId()).isEqualTo(ID);
        assertThat(res.getName()).isEqualTo("Mercadona");
        assertThat(res.getStores()).hasSize(1);
        assertThat(res.getStores().get(0).getAddress()).isEqualTo("Calle A");
    }

    @Test
    void getById_notFound_throws() {
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supermarketChainService.getById(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Supermarket chain not found");
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    void create_saves_andReturnsWithStores() {
        SupermarketChain saved = entity("Mercadona");
        when(supermarketChainRepository.save(any(SupermarketChain.class))).thenReturn(saved);
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.of(saved));

        SupermarketChainWithStoresResponse res = supermarketChainService.create(request("Mercadona"));

        assertThat(res.getName()).isEqualTo("Mercadona");
        verify(supermarketChainRepository).save(any(SupermarketChain.class));
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    void update_found_updatesName() {
        SupermarketChain existing = entity("Mercadona");
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.of(existing));

        SupermarketChainWithStoresResponse res = supermarketChainService.update(ID, request("Mercadona Online"));

        assertThat(res.getName()).isEqualTo("Mercadona Online");
        assertThat(existing.getName()).isEqualTo("Mercadona Online");
        verify(supermarketChainRepository).save(existing);
    }

    @Test
    void update_notFound_throws() {
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supermarketChainService.update(ID, request("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Supermarket chain not found");

        verify(supermarketChainRepository, never()).save(any());
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletes() {
        SupermarketChain existing = entity("Mercadona");
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.of(existing));

        supermarketChainService.delete(ID);

        verify(supermarketChainRepository).delete(existing);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(supermarketChainRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supermarketChainService.delete(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Supermarket chain not found");

        verify(supermarketChainRepository, never()).delete(any());
    }
}
