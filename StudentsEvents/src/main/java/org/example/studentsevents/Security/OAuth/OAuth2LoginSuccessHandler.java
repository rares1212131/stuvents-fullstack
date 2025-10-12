package org.example.studentsevents.Security.OAuth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor; // <-- ADD THIS
import org.example.studentsevents.Security.JWT.JwtUtils;
import org.example.studentsevents.Service.OAuth2UserProvisioningService; // <-- ADD THIS
import org.example.studentsevents.model.User; // <-- ADD THIS
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor // <-- Use Lombok's constructor injection
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    // Inject the provisioning service directly here
    private final OAuth2UserProvisioningService provisioningService;

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            log.info("=== OAuth2 Success Handler Started ===");
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            log.info("OAuth2User attributes: {}", oauth2User.getAttributes());

            // =========================================================================
            // == THE CRITICAL FIX: Find or create the user HERE
            // =========================================================================
            // This call will find an existing user or create a new one. Because we
            // are calling it directly and it's annotated with @Transactional,
            // its transaction will complete before the next line of code runs.
            User user = provisioningService.provisionUser(oauth2User);
            log.info("User provisioned successfully. Email: {}", user.getEmail());
            // =========================================================================

            // Now that we are GUARANTEED the user exists in the database,
            // we can safely generate a token for them.
            String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());

            log.info("JWT generated successfully for email: {}", user.getEmail());
            String redirectUrl = "http://localhost:5173/oauth2/redirect?token=" + jwt;
            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            log.info("=== OAuth2 Success Handler Completed Successfully ===");

        } catch (Exception e) {
            log.error("=== OAuth2 Success Handler Error ===", e);
            response.sendRedirect("http://localhost:5173/oauth2/redirect?error=authentication_failed");
        }
    }
}