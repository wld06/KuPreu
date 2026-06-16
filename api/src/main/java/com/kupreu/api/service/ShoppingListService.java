package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.ConflictException;

import com.kupreu.api.DTOs.DateDIMDTO;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import com.kupreu.api.DTOs.ShoppingList.*;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.entity.*;
import com.kupreu.api.repository.PriceSnapshotRepository;
import com.kupreu.api.repository.ShoppingListItemRepository;
import com.kupreu.api.repository.ShoppingListRepository;
import com.kupreu.api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {
    private final ShoppingListRepository repository;
    private final UserRepository userRepository;
    private final PriceSnapshotRepository psRepository;
    private final ShoppingListItemRepository sliRepository;

    public List<ShoppingListResponse> getAll(String username){
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return repository.findByUser(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ShoppingListResponse getById(UUID id, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        return toResponse(sl);
    }

    @Transactional
    public ShoppingListResponse create(ShoppingListRequest request, String username){
        if (repository.existsByName(request.getName())){
            throw new ConflictException("A shopping list with the same name exists");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ShoppingList sl = ShoppingList.builder()
                .name(request.getName())
                .user(user)
                .build();

        sl = repository.save(sl);

        return toResponse(sl);
    }

    @Transactional
    public ShoppingListResponse update(UUID id, ShoppingListRequest request, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        sl.setName(request.getName());

        sl = repository.save(sl);

        return toResponse(sl);
    }

    @Transactional
    public void delete(UUID id, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        repository.delete(sl);
    }

    public ShoppingListResponse getCheapestList(UUID id, String username){

        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        return ShoppingListResponse.builder()
                .id(sl.getId())
                .name(sl.getName())
                .createdAt(sl.getCreatedAt())
                .shoppingListItems(
                        sl.getItems().stream()
                                .map(this::toCheapestItem)
                                .collect(Collectors.toList())
                )
                .build();
    }

    // ITEMS

    @Transactional
    public ShoppingListResponse addItem(UUID shoppingListId, ShoppingListItemRequest request, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(shoppingListId, username);

        ShoppingListItem item = sl.getItems().stream()
                .filter(i -> i.getPriceSnapshot().getUuid().equals(request.getPriceSnapshotUuid()))
                .findFirst()
                .orElse(null);

        if (item == null){

            PriceSnapshot ps = psRepository.findByUuid(request.getPriceSnapshotUuid());

            if (ps == null){
                throw new NotFoundException("Price snapshot not found");
            }

            sl.getItems().add(
                    ShoppingListItem.builder()
                            .quantity(request.getQuantity())
                            .priceSnapshot(ps)
                            .shoppingList(sl)
                            .build()
            );
        }else{
            item.setQuantity(item.getQuantity() + request.getQuantity());
        }

        repository.save(sl);

        return toResponse(sl);
    }

    @Transactional
    public ShoppingListResponse updateQuantityItem(UUID shoppingListId, UUID shoppingListItemId, ShoppingListItemUpdateQtyRequest request, String username){

        // For security we check the shopping list is for the user
        ShoppingList sl = getShoppingListIfAuthenticated(shoppingListId, username);

        ShoppingListItem item = sliRepository.findById(shoppingListItemId)
                .orElseThrow(() -> new NotFoundException("A shopping list item id is required"));

        if (!item.getShoppingList().getId().equals(sl.getId())){
            throw new NotFoundException("The shopping list item is not found");
        }

        item.setQuantity(request.getQuantity());

        sliRepository.save(item);

        return toResponse(sl);
    }

    @Transactional
    public ShoppingListResponse deleteItem(UUID shoppingListId, UUID shoppingListItemId, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(shoppingListId, username);

        ShoppingListItem item = sliRepository.findById(shoppingListItemId)
                .orElseThrow(() -> new NotFoundException("A shopping list item id is required"));

        if (!item.getShoppingList().getId().equals(sl.getId())){
            throw new NotFoundException("The shopping list item is not found");
        }

        sliRepository.delete(item);

        return toResponse(sl);
    }

    // HELPERS AND ASSIST FUNCTIONS

    private ShoppingList getShoppingListIfAuthenticated(UUID slId, String username){
        ShoppingList sl = repository.findById(slId)
                .orElseThrow(() -> new NotFoundException("Shopping list not found"));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!sl.getUser().getId().equals(user.getId())){
            throw new NotFoundException("Shopping list not found");
        }

        //if the user is the owner of the Shopping list, it will return it, if not error

        return sl;
    }

    private ShoppingListItemResponse toCheapestItem(ShoppingListItem item){
        return ShoppingListItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .priceSnapshot(
                        findCheapest(item.getPriceSnapshot().getProduct().getId())
                )
                .build();
    }

    private PriceSnapshotResponse findCheapest(UUID productId){
        return psRepository.findFirstByProductIdAndDateEndIsNullOrderByPriceAsc(productId)
                .map(this::toPriceSnapshotResponse)
                .orElse(null);
    }

    private PriceSnapshotResponse toPriceSnapshotResponse(PriceSnapshot ps){
        return PriceSnapshotResponse.builder()
                .uuid(ps.getUuid())
                .store(
                        StoreResponse.builder()
                                .id(ps.getStore().getId())
                                .address(ps.getStore().getAddress())
                                .chain(ps.getStore().getSupermarketChain().getName())
                                .build()
                )
                .price(ps.getPrice())
                .dateStart(
                        DateDIMDTO.builder()
                                .id(ps.getDateStart().getId())
                                .date(ps.getDateStart().getDate())
                                .build()
                )
                .build();
    }

    private ShoppingListResponse toResponse(ShoppingList shoppingList){
        return ShoppingListResponse.builder()
                .id(shoppingList.getId())
                .name(shoppingList.getName())
                .createdAt(shoppingList.getCreatedAt())
                .shoppingListItems(
                    shoppingList.getItems()
                            .stream()
                            .map(this::toItemsResponse)
                            .collect(Collectors.toList())
                )
                .build();
    }

    private ShoppingListItemResponse toItemsResponse(ShoppingListItem item){

        PriceSnapshot ps = item.getPriceSnapshot();

        return ShoppingListItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .priceSnapshot(
                        PriceSnapshotResponse.builder()
                                .uuid(ps.getUuid())
                                .store(
                                        StoreResponse.builder()
                                                .id(ps.getStore().getId())
                                                .address(ps.getStore().getAddress())
                                                .chain(ps.getStore().getSupermarketChain().getName())
                                                .build()
                                )
                                .price(ps.getPrice())
                                .build()
                )
                .build();

    }
}
