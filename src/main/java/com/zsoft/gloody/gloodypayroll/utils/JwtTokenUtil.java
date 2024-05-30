package com.zsoft.gloody.gloodypayroll.utils;

import com.zsoft.gloody.gloodypayroll.models.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ID_CLAIM = "GLOODY_USER_ID";
    private static final String GLOODY_COMPANY_ID = "GLOODY_COMPANY_ID";
    @Value("${com.zsoft.gloody.configuration.jwt.secret}")
    private String secret;

    public User getUser(String token) {
           return  User.builder()
                   .id(getClaimFromToken(token, ID_CLAIM, Long.class))
                   .CompanyId(getClaimFromToken(token, GLOODY_COMPANY_ID, Long.class))
                .username(getClaimFromToken(token, Claims::getSubject))
                .build();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public <T> T getClaimFromToken(String token, String claim,  Class<T> aClass) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims.get(claim, aClass );
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
    public String generateToken(HttpServletRequest request, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", user.getUsername());
        claims.put("organization", "gloody-payroll");
        claims.put(ID_CLAIM, user.getId());
        claims.put(GLOODY_COMPANY_ID, user.getCompanyId());
        String issuer = request.getRequestURL().toString();
        return Jwts.builder()
                .setIssuer(issuer)
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

}
