package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.models.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SecurityUtils {
    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<User> getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof User) {
                        return (User) authentication.getPrincipal();
                    }
                    return null;
                });
    }

    public static Long getCurrentCompanyId() {
        return getCurrentUser()
                .map(User::getCompanyId)
                .orElse(null);
    }

    public static List<String> getGrantedAuthorities() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null)
            return Collections.emptyList();
        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            return Collections.emptyList();
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public static boolean hasAnyAuthority(String... authorities) {
        List<String> grantedAuthorities = getGrantedAuthorities();
        return Arrays.stream(authorities)
                .anyMatch(grantedAuthorities::contains);
    }

    public static boolean hasNoneAuthority(String... authorities) {
        List<String> grantedAuthorities = getGrantedAuthorities();
        return Arrays.stream(authorities)
                .noneMatch(grantedAuthorities::contains);
    }
}
