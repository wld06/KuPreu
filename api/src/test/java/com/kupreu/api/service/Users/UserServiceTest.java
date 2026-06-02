package com.kupreu.api.service.Users;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.DTOs.Roles.AdminResponse;
import com.kupreu.api.entity.PostalCode;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private static final UUID ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private User user() {
        return User.builder()
                .id(ID).username("ana").name("Ana").surname("García")
                .email("ana@test.com").isAdmin(false)
                .postalCode(PostalCode.builder().code("28001").city("Madrid").build())
                .build();
    }

    @Test
    void getAllUsers_mapsPage() {
        Page<User> page = new PageImpl<>(List.of(user()), PageRequest.of(0, 20), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProfileResponse> result = userService.getAllUsers(0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).username).isEqualTo("ana");
        assertThat(result.getContent().get(0).postalCode.city).isEqualTo("Madrid");
    }

    @Test
    void getAllUsers_passesPageRequest() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

        userService.getAllUsers(2, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void deleteUser_delegatesToRepository() {
        userService.deleteUser(ID);
        verify(userRepository).deleteById(ID);
    }

    @Test
    void updateUserRole_grantAdmin_savesAndReturns() {
        User u = user();
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));

        AdminResponse res = userService.updateUserRole(ID, true);

        assertThat(res.isAdmin).isTrue();
        assertThat(res.username).isEqualTo("ana");
        assertThat(u.isAdmin()).isTrue();
        verify(userRepository).save(u);
    }

    @Test
    void updateUserRole_revokeAdmin_savesAndReturns() {
        User u = user();
        u.setAdmin(true);
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));

        AdminResponse res = userService.updateUserRole(ID, false);

        assertThat(res.isAdmin).isFalse();
        assertThat(u.isAdmin()).isFalse();
    }

    @Test
    void updateUserRole_notFound_throwsAndDoesNotSave() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(ID, true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).save(any());
    }
}
