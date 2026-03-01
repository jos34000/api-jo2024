package dev.jos.back.service;

import dev.jos.back.dto.user.CreateUserDTO;
import dev.jos.back.dto.user.UserResponseDTO;
import dev.jos.back.exceptions.user.InvalidPasswordException;
import dev.jos.back.exceptions.user.UserAlreadyExistsException;
import dev.jos.back.mapper.UserMapper;
import dev.jos.back.model.PasswordResetToken;
import dev.jos.back.model.User;
import dev.jos.back.repository.PasswordResetTokenRepository;
import dev.jos.back.repository.UserRepository;
import dev.jos.back.util.Role;
import dev.jos.back.util.TokenValidationResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public UserResponseDTO createUser(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("Cet email est déjà utilisé");
        }

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(String authEmail, String newEmail, String newFirstName, String newLastName) {
        if (newEmail.equals(authEmail) && userRepository.existsByEmail(authEmail)) {
            throw new UserAlreadyExistsException("Cet email est déjà utilisé");
        }

        User user = userRepository.findByEmail(authEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authEmail));
        user.setEmail(newEmail);
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);
        User modifiedUser = userRepository.saveAndFlush(user);
        return userMapper.toResponseDTO(modifiedUser);
    }

    public UserResponseDTO getUserResponseDto(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("Mot de passe actuel incorrect");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.saveAndFlush(user);
    }

    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        String token = UUID.randomUUID().toString();
        String hashedToken = BCrypt.hashpw(token, BCrypt.gensalt());
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = new PasswordResetToken(user, hashedToken, expiry);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
    }

    public TokenValidationResult validateResetToken(String token) {
        List<PasswordResetToken> allTokens = passwordResetTokenRepository.findAll();
        Optional<PasswordResetToken> matchingToken = allTokens.stream()
                .filter(t -> BCrypt.checkpw(token, t.getHashedToken()))
                .findFirst();

        if (matchingToken.isEmpty()) {
            return TokenValidationResult.NOT_FOUND;
        }

        if (matchingToken.get().getExpiry().isBefore(LocalDateTime.now())) {
            return TokenValidationResult.EXPIRED;
        }

        return TokenValidationResult.VALID;
    }
}