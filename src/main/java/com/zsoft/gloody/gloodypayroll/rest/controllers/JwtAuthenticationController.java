package com.zsoft.gloody.gloodypayroll.rest.controllers;

import com.zsoft.gloody.gloodypayroll.models.entities.User;
import com.zsoft.gloody.gloodypayroll.services.JwtUserDetailsService;
import com.zsoft.gloody.gloodypayroll.utils.JwtTokenUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

@RequiredArgsConstructor
@RestController
public class JwtAuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${com.zsoft.gloody.configuration.auth.secret-key}")
    private  String authSecret;

    @PostMapping(value = "/api/authenticate")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestHeader(value = "Authorization") String secretCode,
            HttpServletRequest request,
            @RequestBody User user
            ) throws Exception {
        if (secretCode == null || !secretCode.equals(authSecret)) {
            throw new BadCredentialsException("Invalid secret code");
        }
        //authenticate();
        final String token = jwtTokenUtil.generateToken(request, user);
        return ResponseEntity.ok(token);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @Data
    public static class JwtAuthenticationDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private String username;
    }
}
