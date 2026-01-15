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

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginUser_ShouldThrowDisabledException_WhenUserIsNotVerified() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("unverified@user.com");
        loginRequest.setPassword("password123");

        User unverifiedUser = new User();
        unverifiedUser.setId(1L);
        unverifiedUser.setEmail("unverified@user.com");
        unverifiedUser.setVerified(false);

        UserDetailsImpl userDetails = UserDetailsImpl.build(unverifiedUser);
        Authentication successfulAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuthentication);

        when(userRepository.findById(1L)).thenReturn(Optional.of(unverifiedUser));


        assertThrows(DisabledException.class, () -> {
            authService.loginUser(loginRequest);
        });
    }
}