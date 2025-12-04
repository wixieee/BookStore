package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnClientDetails() {
        String email = "client@test.com";
        Client client = new Client();
        client.setEmail(email);
        client.setPassword("encoded_pass");
        client.setBlocked(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertEquals(email, result.getUsername());
        assertEquals("encoded_pass", result.getPassword());
        assertTrue(result.isAccountNonLocked());

        String role = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        assertEquals("ROLE_CLIENT", role);
    }

    @Test
    void loadUserByUsername_shouldReturnEmployeeDetails() {
        String email = "emp@test.com";
        Employee employee = new Employee();
        employee.setEmail(email);
        employee.setPassword("pass");
        employee.setBlocked(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        String role = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        assertEquals("ROLE_EMPLOYEE", role);
    }

    @Test
    void loadUserByUsername_shouldReturnGenericUserDetails() {
        String email = "user@test.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("pass");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        String role = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        assertEquals("ROLE_USER", role);
    }

    @Test
    void loadUserByUsername_shouldHandleBlockedUser() {
        String email = "blocked@test.com";
        Client client = new Client();
        client.setEmail(email);
        client.setPassword("pass");
        client.setBlocked(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(client));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertFalse(result.isAccountNonLocked());
        assertTrue(result.isEnabled());
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        String email = "unknown@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(email)
        );
    }
}