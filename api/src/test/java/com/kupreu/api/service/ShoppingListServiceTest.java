package com.kupreu.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kupreu.api.DTOs.ShoppingList.ShoppingListItemRequest;
import com.kupreu.api.DTOs.ShoppingList.ShoppingListItemUpdateQtyRequest;
import com.kupreu.api.DTOs.ShoppingList.ShoppingListRequest;
import com.kupreu.api.DTOs.ShoppingList.ShoppingListResponse;
import com.kupreu.api.entity.DateDIM;
import com.kupreu.api.entity.PriceSnapshot;
import com.kupreu.api.entity.Product;
import com.kupreu.api.entity.ShoppingList;
import com.kupreu.api.entity.ShoppingListItem;
import com.kupreu.api.entity.Store;
import com.kupreu.api.entity.SupermarketChain;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.PriceSnapshotRepository;
import com.kupreu.api.repository.ShoppingListItemRepository;
import com.kupreu.api.repository.ShoppingListRepository;
import com.kupreu.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock private ShoppingListRepository repository;
    @Mock private UserRepository userRepository;
    @Mock private PriceSnapshotRepository psRepository;
    @Mock private ShoppingListItemRepository sliRepository;
    @InjectMocks private ShoppingListService service;

    private static final String EMAIL = "user@kupreu.com";
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LIST_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ITEM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PRODUCT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PS_UUID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    // ── builders ─────────────────────────────────────────────────────────────────

    private User user() {
        return User.builder().id(USER_ID).email(EMAIL).build();
    }

    private Store store() {
        return Store.builder()
                .id(UUID.randomUUID()).address("Calle A")
                .supermarketChain(SupermarketChain.builder().Id(UUID.randomUUID()).name("Mercadona").build())
                .build();
    }

    private PriceSnapshot priceSnapshot(UUID uuid, BigDecimal price, DateDIM dateEnd) {
        return PriceSnapshot.builder()
                .uuid(uuid)
                .product(Product.builder().id(PRODUCT_ID).name("Leche").build())
                .store(store())
                .dateStart(DateDIM.builder().id(UUID.randomUUID()).date(LocalDateTime.of(2026, 1, 1, 0, 0)).build())
                .dateEnd(dateEnd)
                .price(price)
                .build();
    }

    private ShoppingListItem item(UUID id, int qty, PriceSnapshot ps) {
        return ShoppingListItem.builder().id(id).quantity(qty).priceSnapshot(ps).build();
    }

    private ShoppingList list(User owner, ShoppingListItem... items) {
        ShoppingList sl = ShoppingList.builder()
                .id(LIST_ID).name("Compra semanal").createdAt(LocalDateTime.of(2026, 6, 1, 0, 0))
                .user(owner)
                .items(new ArrayList<>(List.of(items)))
                .build();
        for (ShoppingListItem it : items) {
            it.setShoppingList(sl);
        }
        return sl;
    }

    // ── getAll ───────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsUserLists() {
        User u = user();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(repository.findByUser(USER_ID)).thenReturn(List.of(
                list(u, item(ITEM_ID, 2, priceSnapshot(PS_UUID, new BigDecimal("1.50"), null)))
        ));

        List<ShoppingListResponse> result = service.getAll(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Compra semanal");
        assertThat(result.get(0).getShoppingListItems()).hasSize(1);
    }

    @Test
    void getAll_userNotFound_throws() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAll(EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── getById / auth ─────────────────────────────────────────────────────────────

    @Test
    void getById_ownedByUser_returns() {
        User u = user();
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(list(u)));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));

        assertThat(service.getById(LIST_ID, EMAIL).getId()).isEqualTo(LIST_ID);
    }

    @Test
    void getById_notOwner_throws() {
        User owner = user();
        User other = User.builder().id(UUID.randomUUID()).email(EMAIL).build();
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(list(owner)));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.getById(LIST_ID, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Shopping list not found");
    }

    @Test
    void getById_listNotFound_throws() {
        when(repository.findById(LIST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(LIST_ID, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Shopping list not found");
    }

    // ── create ─────────────────────────────────────────────────────────────────────

    @Test
    void create_newName_saves() {
        ShoppingListRequest req = new ShoppingListRequest("Nueva");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(repository.existsByNameAndUser(eq("Nueva"), any(User.class))).thenReturn(false);
        when(repository.save(any(ShoppingList.class))).thenAnswer(i -> {
            ShoppingList sl = i.getArgument(0);
            sl.setId(LIST_ID);
            sl.setItems(new ArrayList<>());
            return sl;
        });

        ShoppingListResponse res = service.create(req, EMAIL);

        assertThat(res.getName()).isEqualTo("Nueva");
        verify(repository).save(any(ShoppingList.class));
    }

    @Test
    void create_duplicateName_throws() {
        ShoppingListRequest req = new ShoppingListRequest("Compra semanal");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(repository.existsByNameAndUser(eq("Compra semanal"), any(User.class))).thenReturn(true);

        assertThatThrownBy(() -> service.create(req, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("same name exists");

        verify(repository, never()).save(any());
    }

    // ── update / delete ──────────────────────────────────────────────────────────────

    @Test
    void update_changesName() {
        User u = user();
        ShoppingList sl = list(u);
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(sl));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(repository.save(any(ShoppingList.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingListRequest req = new ShoppingListRequest("Renombrada");

        assertThat(service.update(LIST_ID, req, EMAIL).getName()).isEqualTo("Renombrada");
    }

    @Test
    void delete_ownedList_deletes() {
        User u = user();
        ShoppingList sl = list(u);
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(sl));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));

        service.delete(LIST_ID, EMAIL);

        verify(repository).delete(sl);
    }

    // ── getCheapestList ────────────────────────────────────────────────────────────

    @Test
    void getCheapestList_picksActiveCheapest() {
        User u = user();
        PriceSnapshot current = priceSnapshot(PS_UUID, new BigDecimal("2.00"), null);
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(list(u, item(ITEM_ID, 1, current))));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        // DB returns the cheapest active snapshot directly (filtering/ordering pushed down)
        when(psRepository.findFirstByProductIdAndDateEndIsNullOrderByPriceAsc(PRODUCT_ID))
                .thenReturn(Optional.of(priceSnapshot(UUID.randomUUID(), new BigDecimal("1.00"), null)));

        ShoppingListResponse res = service.getCheapestList(LIST_ID, EMAIL);

        assertThat(res.getShoppingListItems()).hasSize(1);
        assertThat(res.getShoppingListItems().get(0).getPriceSnapshot().getPrice()).isEqualByComparingTo("1.00");
    }

    // ── addItem ────────────────────────────────────────────────────────────────────

    @Test
    void addItem_newProduct_addsItem() {
        User u = user();
        ShoppingList sl = list(u); // empty
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(sl));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(psRepository.findByUuid(PS_UUID)).thenReturn(priceSnapshot(PS_UUID, new BigDecimal("1.50"), null));
        when(repository.save(any(ShoppingList.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingListItemRequest req = new ShoppingListItemRequest(3, PS_UUID);

        ShoppingListResponse res = service.addItem(LIST_ID, req, EMAIL);

        assertThat(res.getShoppingListItems()).hasSize(1);
        assertThat(res.getShoppingListItems().get(0).getQuantity()).isEqualTo(3);
        verify(psRepository).findByUuid(PS_UUID);
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() {
        User u = user();
        PriceSnapshot ps = priceSnapshot(PS_UUID, new BigDecimal("1.50"), null);
        ShoppingList sl = list(u, item(ITEM_ID, 2, ps));
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(sl));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(repository.save(any(ShoppingList.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingListItemRequest req = new ShoppingListItemRequest(5, PS_UUID);

        ShoppingListResponse res = service.addItem(LIST_ID, req, EMAIL);

        assertThat(res.getShoppingListItems()).hasSize(1);
        // Existing item: quantity is increased by the requested amount (2 + 5)
        assertThat(res.getShoppingListItems().get(0).getQuantity()).isEqualTo(7);
        verify(psRepository, never()).findByUuid(any());
    }

    // ── updateQuantityItem / deleteItem ──────────────────────────────────────────────

    @Test
    void updateQuantityItem_setsQuantity() {
        User u = user();
        PriceSnapshot ps = priceSnapshot(PS_UUID, new BigDecimal("1.50"), null);
        ShoppingListItem it = item(ITEM_ID, 2, ps);
        ShoppingList sl = list(u, it);
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(sl));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(sliRepository.findById(ITEM_ID)).thenReturn(Optional.of(it));
        when(sliRepository.save(any(ShoppingListItem.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingListItemUpdateQtyRequest req = new ShoppingListItemUpdateQtyRequest(9);

        service.updateQuantityItem(LIST_ID, ITEM_ID, req, EMAIL);

        assertThat(it.getQuantity()).isEqualTo(9);
        verify(sliRepository).save(it);
    }

    @Test
    void deleteItem_removesItem() {
        User u = user();
        PriceSnapshot ps = priceSnapshot(PS_UUID, new BigDecimal("1.50"), null);
        ShoppingListItem it = item(ITEM_ID, 2, ps);
        ShoppingList sl = list(u, it);
        when(repository.findById(LIST_ID)).thenReturn(Optional.of(sl));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
        when(sliRepository.findById(ITEM_ID)).thenReturn(Optional.of(it));

        service.deleteItem(LIST_ID, ITEM_ID, EMAIL);

        verify(sliRepository).delete(it);
    }
}
