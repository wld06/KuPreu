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
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Application service for administrative user management: listing users, deleting
 * accounts and changing roles. Every mutating operation is recorded through the
 * {@link AuditService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AuditService auditService;

    /**
     * Returns a paginated list of all users.
     *
     * @param page zero-based page index
     * @param size page size
     * @return a page of user profiles
     */
    public Page<ProfileResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(this::toProfileResponse);
    }

    /**
     * Deletes the user account with the given identifier.
     *
     * @param id the user identifier
     */
    @Transactional
    public void deleteUser(UUID id){
        userRepository.deleteById(id);
        auditService.record("USER_DELETED", "admin",
                "User deleted", "id=" + id, true);
    }

    /**
     * Grants or revokes administrator privileges for a user.
     *
     * @param id      the user identifier
     * @param isAdmin {@code true} to grant admin rights, {@code false} to revoke them
     * @return the updated role information as a response DTO
     * @throws NotFoundException if no user has the given id
     */
    @Transactional
    public AdminResponse updateUserRole(UUID id, boolean isAdmin){
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        user.setAdmin(isAdmin);
        userRepository.save(user);

        auditService.record("ROLE_CHANGED", user.getEmail(),
                "User role updated", "isAdmin=" + isAdmin, true);

        return AdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isAdmin(user.isAdmin())
                .build();
    }

    /** Maps a {@link User} entity to its profile response DTO. */
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
