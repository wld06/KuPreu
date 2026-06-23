package com.kupreu.api.service.Users;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security {@link UserDetailsService} that loads application users by e-mail
 * and maps the {@code isAdmin} flag to either the {@code ADMIN} or {@code USER} role.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService{
    private final UserRepository userRepository;

    /**
     * Loads a user by e-mail (used as the Spring Security username) and builds the
     * corresponding {@link UserDetails} with the appropriate role.
     *
     * @param email the user's e-mail
     * @return the Spring Security user details
     * @throws UsernameNotFoundException if no user matches the e-mail
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.isAdmin() ? "ADMIN" : "USER")
                .build();
    }
}
