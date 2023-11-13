package com.zsoft.gloody.gloodypayroll.configs;

import com.zsoft.gloody.gloodypayroll.rest.filters.JwtTokenFilter;
import com.zsoft.gloody.gloodypayroll.services.JwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    static final String[] PERMITTED_URLS = {
            "/index.html", "/", "/*.js", "/*.css", "/*.ico", "/*.webmanifest", "/assets/**",
            "/api/authenticate"
    };
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtUserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        // Disable header frame options
        http.headers().frameOptions().disable();

        // Set permissions to endpoints
        http.authorizeRequests()
                // Public endpoints
                .antMatchers(PERMITTED_URLS).permitAll()
                // private endpoints
                .antMatchers("/v2/api-docs").hasAnyRole("ADMIN", "DEVELOPER")
                .anyRequest().authenticated();

        // Set unauthorized requests exception handler
        http = http.exceptionHandling()
                .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                )
                .and();

        // Set session management to stateless
        http = http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();

        // Add JWT token filter
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Api Cors Configuration
        CorsConfiguration conf = new CorsConfiguration();
        conf.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://*.gloody.com"));
        conf.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        conf.setAllowedHeaders(Arrays.asList("Content-Type", "Range", "Authorization", "Requestor-Type"));
        conf.setExposedHeaders(Arrays.asList("Authorization", "Link", "X-Total-Count", "X-Forwarded-For", "X-Get-Header"));
        conf.setMaxAge(3600L);
        source.registerCorsConfiguration("/api/**", conf);
        source.registerCorsConfiguration("/v2/api-docs", conf);
        return source;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
