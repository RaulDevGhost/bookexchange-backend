package com.example.bookexchange.service;

import com.example.bookexchange.model.dto.request.UserRequest;
import com.example.bookexchange.model.dto.response.UserResponse;
import com.example.bookexchange.model.entity.User;
import com.example.bookexchange.repository.UserRepository;
import com.example.bookexchange.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse provisionUser(Authentication authentication) {
        // Extract user information from Keycloak JWT token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        // Check if user already exists in our database
        return userRepository.findByUsername(username)
                .map(this::mapToUserResponse)
                .orElseGet(() -> createNewUser(keycloakId, username, email));
    }

    private UserResponse createNewUser(String keycloakId, String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setKeycloakId(keycloakId); // Add this field to User entity

        // Set default values
        user.setCountry("Not specified");
        user.setCity("Not specified");
        user.setProfilePicture("/default-avatar.png");
        user.setDescription("New user");

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update only profile information, not username or email
        user.setCountry(request.getCountry());
        user.setCity(request.getCity());
        user.setProfilePicture(request.getProfilePicture());
        user.setDescription(request.getDescription());

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("preferred_username");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setCountry(user.getCountry());
        response.setCity(user.getCity());
        response.setProfilePicture(user.getProfilePicture());
        response.setDescription(user.getDescription());
        response.setRank(user.getRank());
        response.setExchangeCount(user.getExchangeCount());
        response.setAverageRating(user.getAverageRating());
        return response;
    }
}