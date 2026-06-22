package com.kupreu.api.service.Users;

import com.kupreu.api.audit.AuditService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kupreu.api.DTOs.Profile.PasswordRequest;
import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.entity.PostalCode;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;
    @InjectMocks private ProfileService profileService;

    private static final UUID ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private User user(PostalCode pc) {
        return User.builder()
                .id(ID).username("ana").name("Ana").surname("García")
                .email("ana@test.com").password("hashed").postalCode(pc).build();
    }

    @Test
    void getMyProfile_found_noPostalCode_returnsNullPostalCode() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(null)));

        ProfileResponse res = profileService.getMyProfile("ana@test.com");

        assertThat(res.getEmail()).isEqualTo("ana@test.com");
        assertThat(res.getUsername()).isEqualTo("ana");
        assertThat(res.getPostalCode()).isNull();
    }

    @Test
    void getMyProfile_found_withPostalCode_mapsIt() {
        PostalCode pc = PostalCode.builder().code("28001").city("Madrid").build();
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(pc)));

        ProfileResponse res = profileService.getMyProfile("ana@test.com");

        assertThat(res.getPostalCode()).isNotNull();
        assertThat(res.getPostalCode().getCode()).isEqualTo("28001");
        assertThat(res.getPostalCode().getCity()).isEqualTo("Madrid");
    }

    @Test
    void getMyProfile_notFound_throws() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile("ghost@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void getProfileById_found_returnsResponse() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user(null)));

        ProfileResponse res = profileService.getProfileById(ID);

        assertThat(res.getId()).isEqualTo(ID);
        assertThat(res.getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    void getProfileById_notFound_throws() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileById(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void getProfileByEmail_found_returnsResponse() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user(null)));

        ProfileResponse res = profileService.getProfileByEmail("ana@test.com");

        assertThat(res.getEmail()).isEqualTo("ana@test.com");
        assertThat(res.getUsername()).isEqualTo("ana");
    }

    @Test
    void getProfileByEmail_notFound_throws() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileByEmail("ghost@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void updatePassword_found_encodesAndSaves() {
        User u = user(null);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("OldPass1!", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("new-hash");

        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username("ana@test.com").password("hashed").roles("USER").build();
        PasswordRequest req = new PasswordRequest();
        req.setActualPassword("OldPass1!");
        req.setNewPassword("NewPass1!");

        profileService.updatePassword(principal, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("new-hash");
    }

    @Test
    void updatePassword_wrongCurrentPassword_throws() {
        User u = user(null);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("WrongPass1!", "hashed")).thenReturn(false);

        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username("ana@test.com").password("hashed").roles("USER").build();
        PasswordRequest req = new PasswordRequest();
        req.setActualPassword("WrongPass1!");
        req.setNewPassword("NewPass1!");

        assertThatThrownBy(() -> profileService.updatePassword(principal, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Current password incorrect");
        verify(userRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updatePassword_userNotFound_throws() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username("ghost@test.com").password("x").roles("USER").build();
        PasswordRequest req = new PasswordRequest();
        req.setNewPassword("NewPass1!");

        assertThatThrownBy(() -> profileService.updatePassword(principal, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
