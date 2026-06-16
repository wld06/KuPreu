package com.kupreu.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotRequest;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import com.kupreu.api.entity.DateDIM;
import com.kupreu.api.entity.PriceSnapshot;
import com.kupreu.api.entity.PriceSnapshotId;
import com.kupreu.api.entity.Product;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.repository.DateDIMRepository;
import com.kupreu.api.repository.PriceSnapshotRepository;
import com.kupreu.api.repository.ProductRepository;
import com.kupreu.api.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class PriceSnapshotServiceTest {

    @Mock private PriceSnapshotRepository priceSnapshotRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private DateDIMRepository dateDIMRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private PriceSnapshotService priceSnapshotService;

    private static final UUID PRODUCT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID STORE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private Store store() {
        return Store.builder()
                .id(STORE_ID).address("Calle A")
                .supermarketChain(SupermarketChain.builder().Id(UUID.randomUUID()).name("Mercadona").build())
                .build();
    }

    private DateDIM date(LocalDateTime when) {
        return DateDIM.builder().id(UUID.randomUUID()).date(when).build();
    }

    private PriceSnapshot snapshot(BigDecimal price, LocalDateTime start, DateDIM end) {
        return PriceSnapshot.builder()
                .id(PriceSnapshotId.builder().productId(PRODUCT_ID).storeId(STORE_ID).build())
                .store(store())
                .dateStart(date(start))
                .dateEnd(end)
                .price(price)
                .build();
    }

    private PriceSnapshotRequest request(LocalDateTime start, LocalDateTime end) {
        PriceSnapshotRequest req = new PriceSnapshotRequest();
        req.setProductId(PRODUCT_ID);
        req.setStoreId(STORE_ID);
        req.setPrice(new BigDecimal("1.50"));
        req.setDateStart(start);
        req.setDateEnd(end);
        return req;
    }

    // ── getPriceSnapshotByProductId ──────────────────────────────────────────────

    @Test
    void getByProductId_sortsByDateStartDescending() {
        LocalDateTime older = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 6, 1, 0, 0);
        when(priceSnapshotRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of(
                snapshot(new BigDecimal("2.00"), older, date(older)),
                snapshot(new BigDecimal("1.00"), newer, date(newer))
        ));

        List<PriceSnapshotResponse> result = priceSnapshotService.getPriceSnapshotByProductId(PRODUCT_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDateStart().getDate()).isEqualTo(newer);
        assertThat(result.get(1).getDateStart().getDate()).isEqualTo(older);
    }

    @Test
    void getByProductId_empty_throws() {
        when(priceSnapshotRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> priceSnapshotService.getPriceSnapshotByProductId(PRODUCT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Price snapshot not found for product id");
    }

    @Test
    void getByProductId_null_throws() {
        when(priceSnapshotRepository.findByProductId(PRODUCT_ID)).thenReturn(null);

        assertThatThrownBy(() -> priceSnapshotService.getPriceSnapshotByProductId(PRODUCT_ID))
                .isInstanceOf(RuntimeException.class);
    }

    // ── getPriceSnapshotsByProductIdAndStoreId ───────────────────────────────────

    @Test
    void getByProductAndStore_returnsSorted() {
        LocalDateTime when = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(priceSnapshotRepository.findByProductIdAndStoreId(PRODUCT_ID, STORE_ID))
                .thenReturn(List.of(snapshot(new BigDecimal("1.00"), when, date(when))));

        assertThat(priceSnapshotService.getPriceSnapshotsByProductIdAndStoreId(PRODUCT_ID, STORE_ID)).hasSize(1);
    }

    @Test
    void getByProductAndStore_empty_throws() {
        when(priceSnapshotRepository.findByProductIdAndStoreId(PRODUCT_ID, STORE_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> priceSnapshotService.getPriceSnapshotsByProductIdAndStoreId(PRODUCT_ID, STORE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("and store id");
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    void create_existingDates_savesSnapshot() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 1, 0, 0);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store()));
        when(dateDIMRepository.findByDate(start)).thenReturn(Optional.of(date(start)));
        when(dateDIMRepository.findByDate(end)).thenReturn(Optional.of(date(end)));
        when(priceSnapshotRepository.save(any(PriceSnapshot.class)))
                .thenReturn(snapshot(new BigDecimal("1.50"), start, date(end)));

        PriceSnapshotResponse res = priceSnapshotService.create(request(start, end));

        assertThat(res.getPrice()).isEqualByComparingTo("1.50");
        verify(priceSnapshotRepository).save(any(PriceSnapshot.class));
        verify(dateDIMRepository, never()).save(any());
    }

    @Test
    void create_missingDates_createsDateDim() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 1, 0, 0);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store()));
        when(dateDIMRepository.findByDate(start)).thenReturn(Optional.empty());
        when(dateDIMRepository.findByDate(end)).thenReturn(Optional.empty());
        when(dateDIMRepository.save(any(DateDIM.class))).thenAnswer(i -> {
            DateDIM d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
        when(priceSnapshotRepository.save(any(PriceSnapshot.class)))
                .thenReturn(snapshot(new BigDecimal("1.50"), start, date(end)));

        priceSnapshotService.create(request(start, end));

        verify(dateDIMRepository, org.mockito.Mockito.times(2)).save(any(DateDIM.class));
    }

    @Test
    void create_productNotFound_throws() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceSnapshotService.create(request(start, start)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found with id");

        verify(priceSnapshotRepository, never()).save(any());
    }

    @Test
    void create_storeNotFound_throws() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceSnapshotService.create(request(start, start)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Store not found with id");

        verify(priceSnapshotRepository, never()).save(any());
    }

    // ── updateEndDate ────────────────────────────────────────────────────────────

    @Test
    void updateEndDate_found_setsEndDate() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 1, 0, 0);
        PriceSnapshotId id = PriceSnapshotId.builder().productId(PRODUCT_ID).storeId(STORE_ID).build();
        when(priceSnapshotRepository.findById(id)).thenReturn(Optional.of(snapshot(new BigDecimal("1.00"), start, date(start))));
        when(dateDIMRepository.findByDate(end)).thenReturn(Optional.of(date(end)));
        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenAnswer(i -> i.getArgument(0));

        PriceSnapshotResponse res = priceSnapshotService.updateEndDate(end, id);

        assertThat(res.getDateEnd().getDate()).isEqualTo(end);
        verify(priceSnapshotRepository).save(any(PriceSnapshot.class));
    }

    @Test
    void updateEndDate_notFound_throws() {
        PriceSnapshotId id = PriceSnapshotId.builder().productId(PRODUCT_ID).storeId(STORE_ID).build();
        when(priceSnapshotRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceSnapshotService.updateEndDate(LocalDateTime.now(), id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Price snapshot not found with id");

        verify(priceSnapshotRepository, never()).save(any());
    }

    // ── getCheapest ──────────────────────────────────────────────────────────────

    @Test
    void getCheapest_empty_throws() {
        when(priceSnapshotRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> priceSnapshotService.getCheapest(PRODUCT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Price snapshot not found for product id");
    }

    @Test
    void getCheapest_allHaveEndDate_throwsNoActive() {
        // Active = dateEnd == null. Every snapshot here has a dateEnd (inactive) → throws.
        LocalDateTime when = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(priceSnapshotRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of(
                snapshot(new BigDecimal("1.00"), when, date(when))
        ));

        assertThatThrownBy(() -> priceSnapshotService.getCheapest(PRODUCT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No active price snapshots");
    }

    @Test
    void getCheapest_returnsCheapestActive_ignoresInactive() {
        LocalDateTime when = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(priceSnapshotRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of(
                snapshot(new BigDecimal("2.00"), when, null),        // active
                snapshot(new BigDecimal("1.00"), when, null),        // active, cheapest
                snapshot(new BigDecimal("0.50"), when, date(when))   // inactive (has dateEnd), ignored
        ));

        PriceSnapshotResponse res = priceSnapshotService.getCheapest(PRODUCT_ID);

        assertThat(res.getPrice()).isEqualByComparingTo("1.00");
        assertThat(res.getDateEnd()).isNull();
    }
}
