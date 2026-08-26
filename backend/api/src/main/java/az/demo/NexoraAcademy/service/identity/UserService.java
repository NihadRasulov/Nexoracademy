package az.demo.NexoraAcademy.service.identity;

import az.demo.NexoraAcademy.dto.identity.ChangePasswordRequest;
import az.demo.NexoraAcademy.dto.identity.UpdateProfileRequest;
import az.demo.NexoraAcademy.dto.identity.UserResponse;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidCredentialsException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = getOrThrow(id);
        if (request.email() != null) {
            userRepository.findByEmail(request.email()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw DuplicateResourceException.of("Admin", "email", request.email());
                }
            });
            user.setEmail(request.email());
        }
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.locale() != null) user.setLocale(request.locale());
        if (request.profile() != null) user.setProfile(request.profile());
        return toResponse(userRepository.save(user));
    }

    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = getOrThrow(id);
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User getOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Admin", id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getPhone(), user.getFirstName(), user.getLastName(),
                user.getDisplayName(), user.getRole(), user.getStatus(), user.getLocale(), user.getProfile(),
                user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
