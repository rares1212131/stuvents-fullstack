package org.example.studentsevents.Service;

import org.example.studentsevents.DTORequest.LoginRequest;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.Security.User.UserDetailsImpl;
import org.example.studentsevents.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // --- Mocks for the dependencies ---
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;

    // We don't need to mock JwtUtils, RoleRepository, or PasswordEncoder for this specific test.
    // They are not used in the part of the loginUser method we are testing.

    // --- The class we are testing ---
    @InjectMocks
    private AuthService authService;

    @Test
    void loginUser_ShouldThrowDisabledException_WhenUserIsNotVerified() {
        // --- ARRANGE ---
        // We set up the scenario where a user with a valid password is not yet verified.

        // 1. Create the login request from the frontend.
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("unverified@user.com");
        loginRequest.setPassword("password123");

        // 2. Create a fake User object that represents the user in the database.
        //    CRUCIALLY, this user's 'verified' flag is false.
        User unverifiedUser = new User();
        unverifiedUser.setId(1L);
        unverifiedUser.setEmail("unverified@user.com");
        unverifiedUser.setVerified(false); // This is the key to the test

        // 3. To simulate a successful password check by Spring Security, we need to create
        //    a successful Authentication object.
        UserDetailsImpl userDetails = UserDetailsImpl.build(unverifiedUser);
        Authentication successfulAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

        // 4. Define the behavior of our mocks.
        //    Tell the AuthenticationManager that the password is correct for this user.
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuthentication);

        //    Tell the UserRepository to find our fake unverified user when the service looks them up.
        when(userRepository.findById(1L)).thenReturn(Optional.of(unverifiedUser));


        // --- ACT & ASSERT ---
        // We expect that calling loginUser now will throw a DisabledException
        // because our service logic checks the 'verified' flag AFTER the password check.
        assertThrows(DisabledException.class, () -> {
            authService.loginUser(loginRequest);
        });
    }
}