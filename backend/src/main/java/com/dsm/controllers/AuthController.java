package com.dsm.controllers;

import com.dsm.dto.request.RequestDTO.SignInRequest;
import com.dsm.dto.request.RequestDTO.SignUpRequest;
import com.dsm.dto.request.RequestDTO.ForgotPasswordRequest;
import com.dsm.dto.request.RequestDTO.ProfileRequest;
import com.dsm.dto.response.ResponseDTO.AuthResponse;
import com.dsm.dto.response.ResponseDTO.MessageResponse;
import com.dsm.entities.*;
import com.dsm.exception.*;
import com.dsm.repositories.*;
import com.dsm.security.JwtUtils;
import com.dsm.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/signin")
    @Operation(summary = "SignIn")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody SignInRequest request) {
        String username = request.getUsername().trim();
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);
        User user = userRepository.getUserByUsername(username).orElseThrow();
        return ResponseEntity.ok(authResponse(user, token));
    }
    @PostMapping("/signup")
    @Operation(summary = "SignUp")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignUpRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String name = request.getName().trim();
        if (userRepository.checkUsernameValidity(username)) {
            throw new DuplicateResourceException("This Username Is Already In Use.");
        }
        if (userRepository.checkEmailValidity(email)) {
            throw new DuplicateResourceException("This Email Is Already In Use.");
        }
        
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .name(name)
                .email(email)
                .role(User.Role.user).isActive(true).build();
        userRepository.save(user);
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        String token = jwtUtils.generateToken((UserDetails) auth.getPrincipal());
        return ResponseEntity.ok(authResponse(user, token));
    }
    @GetMapping("/me")
    @Operation(summary = "Current User")
    public ResponseEntity<AuthResponse> currentUser(Authentication authentication) {
        User user = userRepository.getUserByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("No User Found."));
        String token = jwtUtils.generateToken(userDetailsService.loadUserByUsername(user.getUsername()));
        return ResponseEntity.ok(authResponse(user, token));
    }
    @PutMapping("/me")
    @Operation(summary = "Update Current User")
    public ResponseEntity<AuthResponse> updateCurrentUser(@Valid @RequestBody ProfileRequest request, Authentication authentication) {
        User user = userRepository.getUserByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("No User Found."));
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String name = request.getName().trim();
        if (!username.equals(user.getUsername()) && userRepository.checkUsernameValidity(username)) {
            throw new DuplicateResourceException("This Username Is Already In Use.");
        }
        if (!email.equals(user.getEmail()) && userRepository.checkEmailValidity(email)) {
            throw new DuplicateResourceException("This Email Is Already In Use.");
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setName(name);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        String token = jwtUtils.generateToken(userDetailsService.loadUserByUsername(user.getUsername()));
        return ResponseEntity.ok(authResponse(user, token));
    }
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        User user = userRepository.getUserByEmail(request.getEmail().trim().toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("No User Found With This Email."));
        String temporaryPassword = "temp" + new SecureRandom().nextInt(100000, 999999);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);
        return ResponseEntity.ok(MessageResponse.builder().message("Temporary password: " + temporaryPassword).build());
    }
    private AuthResponse authResponse(User user, String token) {
        return AuthResponse.builder().token(token).username(user.getUsername()).name(user.getName()).email(user.getEmail()).role(user.getRole().name()).build();
    }

}
