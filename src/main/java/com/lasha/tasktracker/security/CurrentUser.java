package com.lasha.tasktracker.security;

import com.lasha.tasktracker.entity.UserEntity;
import com.lasha.tasktracker.exception.ApiException;
import com.lasha.tasktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    public UserEntity get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        }

        Object principal = auth.getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected principal type: " + principal.getClass());
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Current user was not found: " + email));
    }
}
