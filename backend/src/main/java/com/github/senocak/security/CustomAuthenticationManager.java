package com.github.senocak.security;

import com.github.senocak.domain.User;
import com.github.senocak.service.UserService;
import com.github.senocak.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationManager.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationManager(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        final User user = userService.findByEmail(authentication.getName());
        if (authentication.getCredentials() != null) {
            final boolean matches = passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword());
            if (!matches) {
                final String errorMessage = "Username or password invalid. AuthenticationCredentialsNotFoundException occurred for " + user.getName();
                log.error(errorMessage);
                throw new AuthenticationCredentialsNotFoundException(errorMessage);
            }
        }

        final Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AppConstants.RoleName.ROLE_USER.getRole()));
        if (user.getRoles().stream().anyMatch(role -> role.equals(AppConstants.RoleName.ROLE_ADMIN.getRole()))) {
            authorities.add(new SimpleGrantedAuthority(AppConstants.RoleName.ROLE_ADMIN.getRole()));
        }

        final org.springframework.security.core.userdetails.User loadUserByUsername = userService.loadUserByUsername(authentication.getName());
        final Authentication auth = new UsernamePasswordAuthenticationToken(loadUserByUsername, user.getPassword(), authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("Authentication is set to SecurityContext for {}", user.getName());
        return auth;
    }
}
