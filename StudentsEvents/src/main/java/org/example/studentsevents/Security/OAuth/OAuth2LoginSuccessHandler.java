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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final OAuth2UserProvisioningService provisioningService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            log.info("=== OAuth2 Success Handler Started ===");
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            User user = provisioningService.provisionUser(oauth2User);

            String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());

            String redirectUrl = frontendUrl + "/oauth2/redirect?token=" + jwt;


            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            log.info("=== OAuth2 Success Handler Completed Successfully ===");

        } catch (Exception e) {
            log.error("=== OAuth2 Success Handler Error ===", e);

            response.sendRedirect(frontendUrl + "/oauth2/redirect?error=authentication_failed");
        }
    }
}