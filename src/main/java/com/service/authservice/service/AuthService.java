package com.service.authservice.service;

import com.service.authservice.dto.AuthResponse;
import com.service.authservice.dto.LoginRequest;
import com.service.authservice.dto.RegisterRequest;
import com.service.authservice.entity.User;
import com.service.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с таким Email уже существует");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_USER)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Ищем пользователя по email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Сравниваем введённый пароль с хэшем в БД
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }

        // Всё верно — генерируем токен
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
