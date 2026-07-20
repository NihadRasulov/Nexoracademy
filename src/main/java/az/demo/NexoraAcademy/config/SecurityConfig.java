package az.demo.NexoraAcademy.config;

import az.demo.NexoraAcademy.security.CustomAccessDeniedHandler;
import az.demo.NexoraAcademy.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // REST API olduğu üçün CSRF istifadə etmirik
                .csrf(csrf -> csrf.disable())

                // CORS ayrıca CorsConfig classında idarə olunacaq
                .cors(Customizer.withDefaults())

                // Session saxlanılmayacaq (JWT üçün lazımdır)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Exception Handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth

                        // Authentication
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        // Public APIs
                        .requestMatchers(
                                "/api/v1/courses/**",
                                "/api/v1/categories/**"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Health Check
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // Admin APIs
                        .requestMatchers(
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        // Content Manager
                        .requestMatchers(
                                "/api/v1/content/**"
                        ).hasAnyRole("ADMIN", "CONTENT_MANAGER")

                        // Sales Manager
                        .requestMatchers(
                                "/api/v1/sales/**"
                        ).hasAnyRole("ADMIN", "SALES_MANAGER")

                        // Digər bütün endpointlər authentication tələb edir
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

}