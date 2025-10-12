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
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // This is the crucial part: specify the exact URL of your frontend
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Allow all headers, which is fine for development
        configuration.setAllowedHeaders(List.of("*"));
        // Allow credentials (like cookies or auth tokens) to be sent
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all paths in your application
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // +++ END OF NEW CODE                                         +++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
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
                .authorizeHttpRequests(auth -> auth.
                        requestMatchers("/api/debug/**").permitAll() // <-- ADD THIS LINE
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/verify-email").permitAll()
                        // ★★★ ADD THESE TWO LINES ★★★
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()

                        // --- 2. ADMIN-ONLY ENDPOINTS ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories", "/api/cities").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/cities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/cities/**").hasRole("ADMIN")

                        // --- 3. ORGANIZER-ONLY ENDPOINTS (Admins can also access) ---
                        .requestMatchers("/api/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")

                        // --- 4. ANY OTHER REQUEST MUST BE AUTHENTICATED ---
                        // This is a catch-all for any endpoint not listed above.
                        // Examples: /api/auth/me, /api/bookings, /api/users/{id} etc.
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

