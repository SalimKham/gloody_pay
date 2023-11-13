package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.models.entities.User;
import com.zsoft.gloody.gloodypayroll.services.JwtUserDetailsService;
import com.zsoft.gloody.gloodypayroll.utils.JwtTokenUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@RequiredArgsConstructor
@RestController
public class JwtAuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;

    @PostMapping(value = "/api/authenticate")
    public ResponseEntity<?> createAuthenticationToken(
            HttpServletRequest request,
            @RequestBody JwtAuthenticationDto authDto
    ) throws Exception {
        authenticate(authDto.getUsername(), authDto.getPassword());
        final User userDetails = (User) userDetailsService.loadUserByUsername(authDto.getUsername());
        final String token = jwtTokenUtil.generateToken(request, userDetails);
        return ResponseEntity.ok(token);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @Data
    public static class JwtAuthenticationDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private String username;
        private String password;
    }
}
