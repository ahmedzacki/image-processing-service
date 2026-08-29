package com.ahmed.image_processing_service.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ahmed.image_processing_service.auth.dto.AuthResponse;
import com.ahmed.image_processing_service.auth.dto.LoginRequest;
import com.ahmed.image_processing_service.auth.dto.RegisterRequest;
import com.ahmed.image_processing_service.exception.InvalidCredentialsException;
import com.ahmed.image_processing_service.security.JwtService;
import com.ahmed.image_processing_service.user.User;
import com.ahmed.image_processing_service.user.UserRepository;
import com.ahmed.image_processing_service.user.UserService;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = userService.register(request);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
