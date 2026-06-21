package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.DTOs.DateDIMDTO;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotRequest;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.entity.DateDIM;
import com.kupreu.api.entity.PriceSnapshot;
import com.kupreu.api.entity.PriceSnapshotId;
import com.kupreu.api.entity.Product;
import com.kupreu.api.entity.Store;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.repository.DateDIMRepository;
import com.kupreu.api.repository.PriceSnapshotRepository;
import com.kupreu.api.repository.ProductRepository;
import com.kupreu.api.repository.StoreRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PriceSnapshotService {
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final StoreRepository storeRepository;
    private final DateDIMRepository dateDIMRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public List<PriceSnapshotResponse> getPriceSnapshotByProductId(UUID productId){
        List<PriceSnapshot> priceSnapshots = priceSnapshotRepository.findByProductId(productId);
        if (priceSnapshots == null || priceSnapshots.isEmpty()) {
            throw new NotFoundException("Price snapshot not found for product id: " + productId);
        }

        return priceSnapshots.stream()
            .sorted(Comparator.comparing(ps -> ps.getDateStart().getDate(), Comparator.reverseOrder()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PriceSnapshotResponse> getPriceSnapshotsByProductIdAndStoreId(UUID productId, UUID storeId){
        List<PriceSnapshot> priceSnapshots = priceSnapshotRepository.findByProductIdAndStoreId(productId, storeId);
        if (priceSnapshots == null || priceSnapshots.isEmpty()) {
            throw new NotFoundException("Price snapshot not found for product id: " + productId + " and store id: " + storeId);
        }

        return priceSnapshots.stream()
            .sorted(Comparator.comparing(ps -> ps.getDateStart().getDate(), Comparator.reverseOrder()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public PriceSnapshotResponse updateEndDate(LocalDateTime dateEnd, PriceSnapshotId id){
        PriceSnapshot priceSnapshot = priceSnapshotRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Price snapshot not found with id: " + id));
        
        DateDIM dateEndDim = dateDIMRepository.findByDate(dateEnd)
            .orElseGet(() -> dateDIMRepository.save(DateDIM.builder().date(dateEnd).build()));
        
        priceSnapshot.setDateEnd(dateEndDim);
        PriceSnapshot saved = priceSnapshotRepository.save(priceSnapshot);
        auditService.record("PRICE_SNAPSHOT_END_DATE_UPDATED", "Price snapshot end date updated",
                "uuid=" + saved.getUuid() + ", dateEnd=" + dateEnd, true);
        return toResponse(saved);
    }

    @Transactional
    public PriceSnapshotResponse create(PriceSnapshotRequest request){
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new NotFoundException("Product not found with id: " + request.getProductId()));
        
        Store store = storeRepository.findById(request.getStoreId())
            .orElseThrow(() -> new NotFoundException("Store not found with id: " + request.getStoreId()));
        
        DateDIM dateStart = dateDIMRepository.findByDate(request.getDateStart())
            .orElseGet(() -> dateDIMRepository.save(DateDIM.builder().date(request.getDateStart()).build()));
        
        DateDIM dateEnd = request.getDateEnd() == null
            ? null
            : dateDIMRepository.findByDate(request.getDateEnd())
                .orElseGet(() -> dateDIMRepository.save(DateDIM.builder().date(request.getDateEnd()).build()));

        PriceSnapshotId id = PriceSnapshotId.builder()
            .productId(request.getProductId())
            .storeId(request.getStoreId())
            .dateStartId(dateStart.getId())
            .build();
        
        PriceSnapshot priceSnapshot = PriceSnapshot.builder()
            .id(id)
            .product(product)
            .store(store)
            .dateStart(dateStart)
            .dateEnd(dateEnd)
            .price(request.getPrice())
            .build();

        PriceSnapshot saved = priceSnapshotRepository.save(priceSnapshot);
        auditService.record("PRICE_SNAPSHOT_CREATED", "Price snapshot created",
                "uuid=" + saved.getUuid() + ", productId=" + request.getProductId()
                        + ", storeId=" + request.getStoreId() + ", price=" + request.getPrice(), true);
        return toResponse(saved);
    }

    public PriceSnapshotResponse getCheapest(UUID productId){
        List<PriceSnapshot> priceSnapshots = priceSnapshotRepository.findByProductId(productId);
        if (priceSnapshots == null || priceSnapshots.isEmpty()) {
            throw new NotFoundException("Price snapshot not found for product id: " + productId);
        }

        return priceSnapshots.stream()
            .filter(ps -> ps.getDateEnd() == null)
            .min(Comparator.comparing(PriceSnapshot::getPrice))
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("No active price snapshots with product id: " + productId));
    }

    private PriceSnapshotResponse toResponse(PriceSnapshot priceSnapshot){
        return PriceSnapshotResponse.builder()
            .uuid(priceSnapshot.getUuid())
            .store(
                StoreResponse.builder()
                    .id(priceSnapshot.getStore().getId())
                    .address(priceSnapshot.getStore().getAddress())
                    .chain(priceSnapshot.getStore().getSupermarketChain().getName())
                    .build()
            )
            .price(priceSnapshot.getPrice())
            .dateStart(
                DateDIMDTO.builder()
                    .id(priceSnapshot.getDateStart().getId())
                    .date(priceSnapshot.getDateStart().getDate())
                    .build()
            )
            .dateEnd(
                priceSnapshot.getDateEnd() == null ? null :
                        DateDIMDTO.builder()
                            .id(priceSnapshot.getDateEnd().getId())
                            .date(priceSnapshot.getDateEnd().getDate())
                            .build()
            )
            .build();
    }
}
