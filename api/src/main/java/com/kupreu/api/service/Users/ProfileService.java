package com.kupreu.api.service.Users;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kupreu.api.DTOs.PostalCodeDTO;
import com.kupreu.api.DTOs.Profile.PasswordRequest;
import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getMyProfile(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .postalCode(user.getPostalCode() == null ? null : PostalCodeDTO.builder()
                        .code(user.getPostalCode().getCode())
                        .city(user.getPostalCode().getCity())
                        .build())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ProfileResponse getProfileById(UUID id) {
        User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ProfileResponse.builder()
                .id(id)
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .postalCode(user.getPostalCode() == null ? null : PostalCodeDTO.builder()
                            .code(user.getPostalCode().getCode())
                            .city(user.getPostalCode().getCity())
                            .build()
                )
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ProfileResponse getProfileByEmail(String email){
        User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(email)
                .postalCode(user.getPostalCode() == null ? null : PostalCodeDTO.builder()
                            .code(user.getPostalCode().getCode())
                            .city(user.getPostalCode().getCity())
                            .build()
                )
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void updatePassword(UserDetails userDetails, PasswordRequest request){
        User user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
