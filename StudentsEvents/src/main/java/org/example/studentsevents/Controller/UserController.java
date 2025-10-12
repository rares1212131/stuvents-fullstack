package org.example.studentsevents.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.UserUpdateRequest;
import org.example.studentsevents.DTOResponse.UserResponse;
import org.example.studentsevents.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("userUpdate") UserUpdateRequest userUpdateRequest, // <-- Use @RequestPart for the DTO
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        // The service method now receives the DTO directly
        UserResponse updatedUser = userService.updateUser(id, userUpdateRequest, imageFile);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User with id " + id + " was deleted successfully.");
    }


}