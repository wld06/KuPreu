package com.kupreu.api.service.Users;

import com.kupreu.api.exception.NotFoundException;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.DTOs.PostalCodeDTO;
import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.DTOs.Roles.AdminResponse;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public Page<ProfileResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(this::toProfileResponse);
    }

    @Transactional
    public void deleteUser(UUID id){
        userRepository.deleteById(id);
    }

    @Transactional
    public AdminResponse updateUserRole(UUID id, boolean isAdmin){
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        user.setAdmin(isAdmin);
        userRepository.save(user);
        return AdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isAdmin(user.isAdmin())
                .build();
    }

    private ProfileResponse toProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .postalCode(PostalCodeDTO.builder()
                        .code(user.getPostalCode().getCode())
                        .city(user.getPostalCode().getCity())
                        .build())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
