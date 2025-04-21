package org.example.financetracker.service;

import jakarta.transaction.Transactional;
import org.example.financetracker.dto.UserProfileDTO;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Логин или email уже существуют");
        }
        user.setFirstName(user.getFirstName());
        user.setLastName(user.getLastName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }


    public UserDetailsService loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        System.out.println("Найденный пользователь: " + user.getUsername() + ", пароль: " + user.getPassword());
        return (UserDetailsService) org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    @Transactional
    public UserProfileDTO getUserProfile(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    @Transactional
    public boolean updateUserProfile(String username, UserProfileDTO profileDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        if (!passwordEncoder.matches(profileDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        if (!profileDTO.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(profileDTO.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }

        if (profileDTO.getPassword() != null && !profileDTO.getPassword().isEmpty()) {
            if (!profileDTO.getPassword().equals(profileDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            user.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
        }

        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        user.setEmail(profileDTO.getEmail());

        userRepository.save(user);
        return true;
    }
}