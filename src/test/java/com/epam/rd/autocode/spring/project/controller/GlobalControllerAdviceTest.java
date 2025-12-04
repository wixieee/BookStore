package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalControllerAdviceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GlobalControllerAdvice globalControllerAdvice;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticatedAndExists() {
        String email = "test@email.com";
        User user = new User();
        user.setEmail(email);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("somePrincipalObject");
        when(authentication.getName()).thenReturn(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = globalControllerAdvice.getCurrentUser();

        assertEquals(user, result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        User result = globalControllerAdvice.getCurrentUser();

        assertNull(result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(false);

        User result = globalControllerAdvice.getCurrentUser();

        assertNull(result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenAnonymousUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        User result = globalControllerAdvice.getCurrentUser();

        assertNull(result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenUserNotFoundInRepo() {
        String email = "unknown@email.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user");
        when(authentication.getName()).thenReturn(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User result = globalControllerAdvice.getCurrentUser();

        assertNull(result);
        verify(userRepository).findByEmail(email);
    }
}