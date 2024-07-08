package com.github.senocak.service;

import com.github.senocak.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for AuthenticationService")
class AuthenticationServiceTest {
    @InjectMocks AuthenticationServiceImpl authenticationService;

    private final Authentication auth = Mockito.mock(Authentication.class);
    private final org.springframework.security.core.userdetails.User secUser = Mockito.mock(org.springframework.security.core.userdetails.User.class);

    @BeforeEach
    void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
        Mockito.lenient().when(auth.getPrincipal()).thenReturn(secUser);
    }

    @Test
    void givenNullAuthenticationWhenIsAuthorizedThenThrowAccessDeniedException() {
        // Given
        Mockito.lenient().when(auth.getPrincipal()).thenReturn(null);
        // When
        Executable closureToTest = () -> authenticationService.isAuthorized(new String[]{});
        // Then
        assertThrows(AccessDeniedException.class, closureToTest);
    }

    @Test
    void givenValidRoleWhenIsAuthorizedThenAssertResult() throws AccessDeniedException {
        // Given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Mockito.lenient().when(secUser.getAuthorities()).thenReturn(authorities);
        // When
        boolean preHandle = authenticationService.isAuthorized("ROLE_ADMIN");
        // Then
        assertTrue(preHandle);
    }

    @Test
    void givenNotValidRoleWhenIsAuthorizedThenAssertResult() {
        // When
        boolean preHandle = authenticationService.isAuthorized("ROLE_USER");
        // Then
        assertFalse(preHandle);
    }
}
