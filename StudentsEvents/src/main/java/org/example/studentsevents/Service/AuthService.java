// Complete AuthService.java file with all changes
package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.LoginRequest;
import org.example.studentsevents.DTORequest.PasswordResetRequest;
import org.example.studentsevents.DTORequest.RegistrationRequest;
import org.example.studentsevents.DTOResponse.JWTResponse;
import org.example.studentsevents.DTOResponse.UserResponse;
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.Security.JWT.JwtUtils;
import org.example.studentsevents.Security.User.UserDetailsImpl;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Transactional
    public UserResponse registerUser(RegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalStateException("Error: Email is already in use!");
        }

        User user = new User();
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
        user.setRoles(Set.of(userRole));

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiryDate(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        String verificationLink = "http://localhost:5173/verify-email?token=" + token;
        String emailBody = "Welcome to STUvents!\n\nPlease click the link below to verify your email address and activate your account:\n" + verificationLink + "\n\nThis link will expire in 24 hours.";
        System.out.println("--- 1. ATTEMPTING TO SEND VERIFICATION EMAIL to: " + savedUser.getEmail() + " ---");
        emailService.sendSimpleEmail(savedUser.getEmail(), "STUvents Email Verification", emailBody);
        System.out.println("--- 2. EMAIL HANDED OFF TO EMAIL SERVICE ---");
        return userService.mapUserToUserResponse(savedUser);
    }

    public JWTResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));

        if (!user.isVerified()) {
            throw new DisabledException("Account not verified. Please check your email for a verification link.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        return new JWTResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);
    }

    @Transactional
    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or already used verification token."));

        if (user.getVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired. Please register again.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiryDate(null);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        return userService.mapUserToUserResponse(user);
    }

    @Transactional
    public void handleForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setResetTokenExpiryDate(LocalDateTime.now().plusHours(1));

            userRepository.save(user);

            String resetLink = "http://localhost:5173/reset-password?token=" + token;
            String emailBody = "You have requested to reset your password.\n\n"
                    + "Please click the link below to set a new password:\n" + resetLink + "\n\n"
                    + "If you did not request this, please ignore this email. This link will expire in 1 hour.";
            System.out.println("--- 1. ATTEMPTING TO RESET ---");
            emailService.sendSimpleEmail(user.getEmail(), "STUvents Password Reset Request", emailBody);
            System.out.println("--- 2. EMAIL HANDED OFF ---");
        }
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or already used password reset token."));

        if (user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setResetTokenExpiryDate(null);

        userRepository.save(user);
    }


}