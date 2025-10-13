package org.example.studentsevents.Security.Config;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.EventRequest;
import org.example.studentsevents.DTOResponse.BookingResponse;
import org.example.studentsevents.Security.JWT.AuthTokenFilter;
import org.example.studentsevents.Security.JWT.JwtAuthEntryPoint;
import org.example.studentsevents.Security.OAuth.CustomOAuth2UserService;
import org.example.studentsevents.Security.OAuth.OAuth2LoginSuccessHandler;
import org.example.studentsevents.Security.User.UserDetailsServiceImpl;
import org.example.studentsevents.model.Booking;
import org.example.studentsevents.model.Event;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // This enables @PreAuthorize annotations
@RequiredArgsConstructor
public class EventConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthEntryPoint unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final CustomOAuth2UserService customOAuth2UserService; // <-- NEW INJECTION
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // <-- NEW INJECTION

    // Read allowed origins from environment variable (comma-separated). Provide sensible default for local dev + prod.
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,https://stuvents-fullstack.vercel.app}")
    private String corsAllowedOrigins;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.createTypeMap(EventRequest.class, Event.class)
                .addMappings(mapper -> {
                    mapper.skip(Event::setId);
                    mapper.skip(Event::setTicketTypes);
                });
        TypeMap<Booking, BookingResponse> bookingMap = modelMapper.createTypeMap(Booking.class, BookingResponse.class);
        bookingMap.addMappings(mapper -> {
            // For the 'event' field in the destination (BookingResponse),
            // get the source's ticketType, then get that ticketType's event.
            mapper.map(src -> src.getTicketType().getEvent(), BookingResponse::setEvent);
        });

        return modelMapper;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Global CORS configuration.
     *
     * - Reads CORS_ALLOWED_ORIGINS env var (comma separated).
     * - If any pattern contains '*' you will use allowedOriginPatterns for flexibility.
     * - Allows credentials; do not use '*' origin when allowCredentials=true in production.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // parse env var into list
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // If you want wildcard support (e.g. "https://*.vercel.app") use allowedOriginPatterns
        boolean containsPattern = origins.stream().anyMatch(o -> o.contains("*"));

        if (containsPattern) {
            // Spring will match patterns when using allowedOriginPatterns
            configuration.setAllowedOriginPatterns(origins);
        } else {
            configuration.setAllowedOrigins(origins);
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // wire the CorsConfigurationSource into the security filter chain
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Use our custom service
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // Use our custom success handler
                )
                .authorizeHttpRequests(auth -> auth
                        // Always allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public auth paths (both root and /api prefix) - adjust if your controllers are different
                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()

                        // Public GET endpoints (both root and /api prefix)
                        .requestMatchers(HttpMethod.GET, "/events/**", "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cities/**", "/api/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**", "/api/images/**").permitAll()

                        // Additional individual permits (optional redundancy)
                        .requestMatchers("/auth/register", "/api/auth/register").permitAll()
                        .requestMatchers("/auth/login", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/verify-email", "/api/auth/verify-email").permitAll()
                        .requestMatchers("/auth/forgot-password", "/api/auth/forgot-password").permitAll()
                        .requestMatchers("/auth/reset-password", "/api/auth/reset-password").permitAll()

                        // Debug / health endpoints (optional)
                        .requestMatchers("/api/debug/**", "/debug/**").permitAll()

                        // ADMIN endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories", "/api/cities").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/cities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/cities/**").hasRole("ADMIN")

                        // ORGANIZER endpoints
                        .requestMatchers("/api/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
