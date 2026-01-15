

package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.UserUpdateRequest;
import org.example.studentsevents.DTOResponse.UserResponse;
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

        if (imageFile != null && !imageFile.isEmpty()) {

            String imageUrl = imageService.storeFile(imageFile);
            existingUser.setProfilePictureUrl(imageUrl);
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
                .map(this::mapUserToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse mapUserToUserResponse(User user) {
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        userResponse.setProfilePictureUrl(user.getProfilePictureUrl());

        return userResponse;
    }
}