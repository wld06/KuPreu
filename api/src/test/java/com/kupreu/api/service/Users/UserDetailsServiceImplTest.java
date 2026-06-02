package com.kupreu.api.service.Users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserDetailsServiceImpl service;

    private User user(boolean admin) {
        return User.builder()
                .id(UUID.randomUUID()).username("ana").name("Ana").surname("García")
                .email("ana@test.com").password("hashed").isAdmin(admin).build();
    }

    @Test
    void loadUserByUsername_regularUser_hasUserRole() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(false)));

        UserDetails details = service.loadUserByUsername("ana@test.com");

        assertThat(details.getUsername()).isEqualTo("ana@test.com");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_adminUser_hasAdminRole() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(true)));

        UserDetails details = service.loadUserByUsername("ana@test.com");

        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: ghost@test.com");
    }
}
