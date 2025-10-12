package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.UserUpdateRequest;
import org.example.studentsevents.DTOResponse.UserResponse;
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Image;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ImageService imageService;
    private final RoleRepository roleRepository;


    @Transactional
    public void setUserRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Find all the Role objects that match the names sent from the frontend
        Set<Role> newRoles = roleRepository.findAll().stream()
                .filter(role -> roleNames.contains(role.getName()))
                .collect(Collectors.toSet());
        user.setRoles(newRoles);

        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapUserToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest, MultipartFile imageFile) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setFirstName(userUpdateRequest.getFirstName());
        existingUser.setLastName(userUpdateRequest.getLastName());

        // Check if a new image file was provided
        if (imageFile != null && !imageFile.isEmpty()) {
            // Store the new file and get its unique name
            String fileName = imageService.storeFile(imageFile);
            // Set the new image on the user.
            // If an old image exists, orphanRemoval=true will handle deleting it from the DB
            // and we can later add logic to delete the old file from disk if needed.
            existingUser.setProfilePicture(new Image(fileName));
        }

        User updatedUser = userRepository.save(existingUser);
        return mapUserToUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapUserToUserResponse) // Reuse your existing mapping logic
                .collect(Collectors.toList());
    }

    public UserResponse mapUserToUserResponse(User user) {
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        // Check if the user has a profile picture and construct the full URL
        if (user.getProfilePicture() != null) {
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/images/") // This matches your ImageController's mapping
                    .path(user.getProfilePicture().getFileName())
                    .toUriString();
            userResponse.setProfilePictureUrl(fileDownloadUri);
        }

        return userResponse;
    }
}