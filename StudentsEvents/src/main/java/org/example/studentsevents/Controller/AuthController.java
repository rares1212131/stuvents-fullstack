// In file: src/main/java/org/example/studentsevents/Controller/AuthController.java
package org.example.studentsevents.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.LoginRequest;
import org.example.studentsevents.DTORequest.PasswordResetRequest;
import org.example.studentsevents.DTORequest.RegistrationRequest;
import org.example.studentsevents.DTOResponse.JWTResponse;
import org.example.studentsevents.DTOResponse.UserResponse;
import org.example.studentsevents.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        UserResponse newUser = authService.registerUser(registrationRequest);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JWTResponse jwtResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse user = authService.getAuthenticatedUser();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            authService.verifyUser(token);
            return ResponseEntity.ok("<h1>Email Verified Successfully!</h1><p>You can now close this tab and log in.</p>");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("<h1>Error</h1><p>" + e.getMessage() + "</p>");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        try {
            authService.handleForgotPassword(email);
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        } catch (Exception e) {
            System.err.println("Error in forgotPassword: " + e.getMessage());
            return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}