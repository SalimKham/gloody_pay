package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.models.entities.User;
import com.zsoft.gloody.gloodypayroll.models.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@AllArgsConstructor
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Configuration
public class PersistenceConfig {
    private final UserRepository userRepository;

    @Bean
    AuditorAware<User> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            if (authentication.getPrincipal() instanceof String) {
                return Optional.empty();
            }
            if (authentication.getPrincipal() instanceof User) {
                return Optional.of((User)(authentication.getPrincipal()));
            }
            String username = authentication.getName();
            return userRepository.findById(username);
        };
    }
}
