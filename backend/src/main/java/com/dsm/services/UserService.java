package com.dsm.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dsm.dto.request.RequestDTO.UserRequest;
import com.dsm.dto.response.ResponseDTO.UserResponse;
import com.dsm.entities.User;
import com.dsm.exception.BusinessException;
import com.dsm.exception.DuplicateResourceException;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    public UserResponse createUser(UserRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.checkUsernameValidity(username)) {
            throw new DuplicateResourceException("This Username Is Already In Use.");
        }
        if (userRepository.checkEmailValidity(email)) {
            throw new DuplicateResourceException("This Email Is Already In Use.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("Password must be filled.");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .email(email)
                .role(request.getRole())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return toResponse(userRepository.save(user));
    }
    public UserResponse updateUser(String id, UserRequest request, String currentUsername) {
        User user = findById(id);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        if (!username.equals(user.getUsername()) && userRepository.checkUsernameValidity(username)) {
            throw new DuplicateResourceException("This Username Is Already In Use.");
        }
        if (!email.equals(user.getEmail()) && userRepository.checkEmailValidity(email)) {
            throw new DuplicateResourceException("This Email Is Already In Use.");
        }
        if (user.getUsername().equals(currentUsername) && request.getIsActive() != null && !request.getIsActive()) {
            throw new BusinessException("You cannot lock your own account.");
        }
        user.setUsername(username);
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive() == null || request.getIsActive());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(user));
    }
    public void deleteUser(String id, String currentUsername) {
        User user = findById(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("You cannot delete your own account.");
        }
        userRepository.deleteById(id);
    }
    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User With " + id + " Was Not Found."));
    }
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
