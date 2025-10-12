package org.example.studentsevents.Controller;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @GetMapping("/data")
    public ResponseEntity<?> getInitialData() {
        List<Role> roles = roleRepository.findAll();
        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(Map.of(
                "rolesInDatabase", roles,
                "usersInDatabase", users
        ));
    }
}