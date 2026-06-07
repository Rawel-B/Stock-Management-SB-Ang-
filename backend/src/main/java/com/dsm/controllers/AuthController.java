package com.dsm.controllers;

import com.dsm.dto.request.RequestDTO.SignInRequest;
import com.dsm.dto.request.RequestDTO.SignUpRequest;
import com.dsm.dto.response.ResponseDTO.AuthResponse;
import com.dsm.entities.*;
import com.dsm.exception.*;
import com.dsm.repositories.*;
import com.dsm.security.JwtUtils;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    @Operation(summary = "SignIn")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody SignInRequest request) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);
        User user = userRepository.getUserByUsername(request.getUsername()).orElseThrow();
        return ResponseEntity.ok(AuthResponse.builder().token(token).username(user.getUsername()).name(user.getName()).role(user.getRole().name()).build());
    }
    @PostMapping("/signup")
    @Operation(summary = "SignUp")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignUpRequest request) {
        if (userRepository.checkUsernameValidity(request.getUsername())) {
            throw new DuplicateResourceException("This Username Is Already In Use.");
        }
        if (userRepository.checkEmailValidity(request.getEmail())) {
            throw new DuplicateResourceException("This Email Is Already In Use.");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .role(User.Role.user).isActive(true).build();
        userRepository.save(user);
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String token = jwtUtils.generateToken((UserDetails) auth.getPrincipal());
        return ResponseEntity.ok(AuthResponse.builder().token(token).username(user.getUsername()).name(user.getName()).role(user.getRole().name()).build());
    }

}
