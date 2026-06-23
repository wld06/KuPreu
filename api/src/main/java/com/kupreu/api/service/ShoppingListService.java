package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.ConflictException;

import com.kupreu.api.DTOs.DateDIMDTO;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import com.kupreu.api.DTOs.ShoppingList.*;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.audit.AuditService;
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

/**
 * Application service holding the business logic for {@link ShoppingList} and its items.
 * <p>
 * All operations are scoped to the authenticated user (identified by e-mail); a list
 * that belongs to another user is reported as "not found" rather than forbidden, so
 * existence is not leaked. Every mutating operation is recorded through the {@link AuditService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {
    private final ShoppingListRepository repository;
    private final UserRepository userRepository;
    private final PriceSnapshotRepository psRepository;
    private final ShoppingListItemRepository sliRepository;
    private final AuditService auditService;

    /**
     * Returns every shopping list owned by the given user.
     *
     * @param username the authenticated user's e-mail
     * @return the user's shopping lists as response DTOs
     * @throws NotFoundException if no user matches the e-mail
     */
    public List<ShoppingListResponse> getAll(String username){
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return repository.findByUser(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a single shopping list owned by the given user.
     *
     * @param id       the shopping list identifier
     * @param username the authenticated user's e-mail
     * @return the matching shopping list as a response DTO
     * @throws NotFoundException if the list does not exist or is not owned by the user
     */
    public ShoppingListResponse getById(UUID id, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        return toResponse(sl);
    }

    /**
     * Creates a new shopping list for the given user.
     *
     * @param request  the list data (name)
     * @param username the authenticated user's e-mail
     * @return the created shopping list as a response DTO
     * @throws NotFoundException if no user matches the e-mail
     * @throws ConflictException if the user already has a list with that name
     */
    @Transactional
    public ShoppingListResponse create(ShoppingListRequest request, String username){
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (repository.existsByNameAndUser(request.getName(), user)){
            throw new ConflictException("A shopping list with the same name exists");
        }

        ShoppingList sl = ShoppingList.builder()
                .name(request.getName())
                .user(user)
                .build();

        sl = repository.save(sl);

        auditService.record("SHOPPING_LIST_CREATED", username, "Shopping list created",
                "id=" + sl.getId() + ", name=" + sl.getName(), true);

        return toResponse(sl);
    }

    /**
     * Renames a shopping list owned by the given user.
     *
     * @param id       the shopping list identifier
     * @param request  the new list data (name)
     * @param username the authenticated user's e-mail
     * @return the updated shopping list as a response DTO
     * @throws NotFoundException if the list does not exist or is not owned by the user
     */
    @Transactional
    public ShoppingListResponse update(UUID id, ShoppingListRequest request, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        sl.setName(request.getName());

        sl = repository.save(sl);

        auditService.record("SHOPPING_LIST_UPDATED", username, "Shopping list updated",
                "id=" + id + ", name=" + sl.getName(), true);

        return toResponse(sl);
    }

    /**
     * Deletes a shopping list owned by the given user.
     *
     * @param id       the shopping list identifier
     * @param username the authenticated user's e-mail
     * @throws NotFoundException if the list does not exist or is not owned by the user
     */
    @Transactional
    public void delete(UUID id, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(id, username);

        repository.delete(sl);

        auditService.record("SHOPPING_LIST_DELETED", username, "Shopping list deleted", "id=" + id, true);
    }

    /**
     * Returns the shopping list with, for each item, the cheapest currently-valid
     * price found across all stores for that product.
     *
     * @param id       the shopping list identifier
     * @param username the authenticated user's e-mail
     * @return the shopping list with cheapest-price items
     * @throws NotFoundException if the list does not exist or is not owned by the user
     */
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

    /**
     * Adds an item to a shopping list. If the same price snapshot is already in the
     * list, its quantity is increased instead of inserting a duplicate line.
     *
     * @param shoppingListId the shopping list identifier
     * @param request        the item data (price snapshot uuid and quantity)
     * @param username       the authenticated user's e-mail
     * @return the updated shopping list as a response DTO
     * @throws NotFoundException if the list (for this user) or the price snapshot does not exist
     */
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

        auditService.record("SHOPPING_LIST_ITEM_ADDED", username, "Item added to shopping list",
                "shoppingListId=" + shoppingListId + ", priceSnapshotUuid=" + request.getPriceSnapshotUuid(), true);

        return toResponse(sl);
    }

    /**
     * Sets the quantity of an existing item within a shopping list.
     *
     * @param shoppingListId     the shopping list identifier
     * @param shoppingListItemId the item identifier
     * @param request            the new quantity
     * @param username           the authenticated user's e-mail
     * @return the updated shopping list as a response DTO
     * @throws NotFoundException if the list (for this user) or the item does not exist,
     *                           or the item does not belong to the list
     */
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

        auditService.record("SHOPPING_LIST_ITEM_QTY_UPDATED", username, "Shopping list item quantity updated",
                "shoppingListId=" + shoppingListId + ", itemId=" + shoppingListItemId + ", quantity=" + request.getQuantity(), true);

        return toResponse(sl);
    }

    /**
     * Removes an item from a shopping list.
     *
     * @param shoppingListId     the shopping list identifier
     * @param shoppingListItemId the item identifier
     * @param username           the authenticated user's e-mail
     * @return the updated shopping list as a response DTO
     * @throws NotFoundException if the list (for this user) or the item does not exist,
     *                           or the item does not belong to the list
     */
    @Transactional
    public ShoppingListResponse deleteItem(UUID shoppingListId, UUID shoppingListItemId, String username){
        ShoppingList sl = getShoppingListIfAuthenticated(shoppingListId, username);

        ShoppingListItem item = sliRepository.findById(shoppingListItemId)
                .orElseThrow(() -> new NotFoundException("A shopping list item id is required"));

        if (!item.getShoppingList().getId().equals(sl.getId())){
            throw new NotFoundException("The shopping list item is not found");
        }

        sliRepository.delete(item);

        auditService.record("SHOPPING_LIST_ITEM_DELETED", username, "Item removed from shopping list",
                "shoppingListId=" + shoppingListId + ", itemId=" + shoppingListItemId, true);

        return toResponse(sl);
    }

    // HELPERS AND ASSIST FUNCTIONS

    /**
     * Loads a shopping list and verifies it belongs to the given user. To avoid
     * leaking the existence of other users' lists, a list owned by someone else is
     * reported the same way as a missing list.
     *
     * @param slId     the shopping list identifier
     * @param username the authenticated user's e-mail
     * @return the shopping list, guaranteed to belong to the user
     * @throws NotFoundException if the list or user does not exist, or the list is not owned by the user
     */
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

    /** Maps an item to a response carrying the cheapest active price for its product. */
    private ShoppingListItemResponse toCheapestItem(ShoppingListItem item){
        return ShoppingListItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .priceSnapshot(
                        findCheapest(item.getPriceSnapshot().getProduct().getId())
                )
                .build();
    }

    /** Finds the cheapest currently-valid price for a product, or {@code null} if none. */
    private PriceSnapshotResponse findCheapest(UUID productId){
        return psRepository.findFirstByProductIdAndDateEndIsNullOrderByPriceAsc(productId)
                .map(this::toPriceSnapshotResponse)
                .orElse(null);
    }

    /** Maps a {@link PriceSnapshot} entity to its response DTO (store, price and start date). */
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

    /** Maps a {@link ShoppingList} entity to its response DTO, including its items. */
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

    /** Maps a {@link ShoppingListItem} entity to its response DTO using its stored price snapshot. */
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
