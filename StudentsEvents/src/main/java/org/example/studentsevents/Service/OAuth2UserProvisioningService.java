package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OAuth2UserProvisioningService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static final Logger log = LoggerFactory.getLogger(OAuth2UserProvisioningService.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User provisionUser(OAuth2User oAuth2User) {
        try {
            String email = oAuth2User.getAttribute("email");
            log.info("OAuth2 [TX_NEW] - Attempting to provision user with email: {}", email);

            // Find the user using an Optiodal
            Optional<User> userOptional = userRepository.findByEmail(email);

            User user;
            if (userOptional.isPresent()) {
                // --- UPDATE PATH ---
                log.info("OAuth2 [TX_NEW]: Found existing user. Updating details.");
                user = userOptional.get();
                user.setFirstName(oAuth2User.getAttribute("given_name"));
                user.setLastName(oAuth2User.getAttribute("family_name"));
                user.setVerified(true);
            } else {
                // --- CREATE PATH ---
                log.info("OAuth2 [TX_NEW]: New user. Creating entity.");
                user = new User();
                user.setEmail(email);
                user.setFirstName(oAuth2User.getAttribute("given_name"));
                user.setLastName(oAuth2User.getAttribute("family_name"));
                user.setPassword("OAUTH2_DUMMY_PASSWORD");
                user.setVerified(true);
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("CRITICAL: ROLE_USER does not exist in roles table!"));
                user.setRoles(Set.of(userRole));
            }

            log.info("OAuth2 [TX_NEW] - Calling userRepository.save() for email: {}", user.getEmail());
            User savedUser = userRepository.save(user);
            log.info("OAuth2 [TX_NEW] - save() method completed. User ID (pre-commit): {}", savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            log.error("!!!!!!!!!! EXCEPTION CAUGHT INSIDE TRANSACTION, WILL CAUSE ROLLBACK !!!!!!!!!!");
            log.error("Hidden Error Type: {}", e.getClass().getName());
            log.error("Hidden Error Message: {}", e.getMessage());
            log.error("Full stack trace of hidden error:", e);
            throw e;
        }
    }
}