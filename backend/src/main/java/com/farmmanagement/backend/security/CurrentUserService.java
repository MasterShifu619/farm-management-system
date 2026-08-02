package com.farmmanagement.backend.security;

import com.farmmanagement.backend.entity.User;
import com.farmmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

// Bridges the token's username (all JwtAuthFilter puts in SecurityContext) back
// to the full User row, so controllers can filter by owner id / state code.
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + authentication.getName()));
    }
}
