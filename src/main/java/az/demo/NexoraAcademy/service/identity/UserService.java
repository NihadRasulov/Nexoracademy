package az.demo.NexoraAcademy.service.identity;

import az.demo.NexoraAcademy.dto.identity.ChangePasswordRequest;
import az.demo.NexoraAcademy.dto.identity.UpdateProfileRequest;
import az.demo.NexoraAcademy.dto.identity.UserRequest;
import az.demo.NexoraAcademy.dto.identity.UserResponse;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.event.UserRoleChangedEvent;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidCredentialsException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.repository.identity.UserSpecifications;
import az.demo.NexoraAcademy.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> search(String query, UserRole role, AccountStatus status, Pageable pageable) {
        return userRepository.findAll(UserSpecifications.search(query, role, status), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw DuplicateResourceException.of("User", "email", request.email());
        }
        if (request.phone() != null && userRepository.existsByPhone(request.phone())) {
            throw DuplicateResourceException.of("User", "phone", request.phone());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : UserRole.STUDENT);
        user.setStatus(request.status() != null ? request.status() : AccountStatus.PENDING_VERIFICATION);
        user.setLocale(request.locale() != null ? request.locale() : "az-AZ");
        user.setProfile(request.profile() != null ? request.profile() : new HashMap<>());

        return toResponse(userRepository.saveAndFlush(user));
    }

    public UserResponse update(UUID id, UserRequest request) {
        User user = getOrThrow(id);
        assertEmailAvailable(request.email(), id);
        if (request.phone() != null) {
            assertPhoneAvailable(request.phone(), id);
        }

        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        applyRoleChange(user, request.role());
        user.setStatus(request.status() != null ? request.status() : user.getStatus());
        user.setLocale(request.locale() != null ? request.locale() : user.getLocale());
        user.setProfile(request.profile() != null ? request.profile() : user.getProfile());

        return toResponse(userRepository.saveAndFlush(user));
    }

    public UserResponse patch(UUID id, UserRequest request) {
        User user = getOrThrow(id);

        if (request.email() != null) {
            assertEmailAvailable(request.email(), id);
            user.setEmail(request.email());
        }
        if (request.phone() != null) {
            assertPhoneAvailable(request.phone(), id);
            user.setPhone(request.phone());
        }
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.password() != null) user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.role() != null) applyRoleChange(user, request.role());
        if (request.status() != null) user.setStatus(request.status());
        if (request.locale() != null) user.setLocale(request.locale());
        if (request.profile() != null) user.setProfile(request.profile());

        return toResponse(userRepository.saveAndFlush(user));
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException.of("User", id);
        }
        userRepository.deleteById(id);
    }

    // --- self-service (/me) -------------------------------------------------

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return toResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email)));
    }

    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = getOrThrow(id);

        if (request.email() != null) {
            assertEmailAvailable(request.email(), id);
            user.setEmail(request.email());
        }
        if (request.phone() != null) {
            assertPhoneAvailable(request.phone(), id);
            user.setPhone(request.phone());
        }
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.locale() != null) user.setLocale(request.locale());
        if (request.profile() != null) user.setProfile(request.profile());

        return toResponse(userRepository.saveAndFlush(user));
    }

    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = getOrThrow(id);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.saveAndFlush(user);
    }

    // --- internal -------------------------------------------------------------

    private void applyRoleChange(User user, UserRole newRole) {
        if (newRole == null || newRole == user.getRole()) {
            return;
        }
        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        eventPublisher.publishEvent(new UserRoleChangedEvent(user.getId(), oldRole, newRole, SecurityUtils.currentUserId()));
    }

    private void assertEmailAvailable(String email, UUID currentUserId) {
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(currentUserId)) {
                throw DuplicateResourceException.of("User", "email", email);
            }
        });
    }

    private void assertPhoneAvailable(String phone, UUID currentUserId) {
        userRepository.findByPhone(phone).ifPresent(existing -> {
            if (!existing.getId().equals(currentUserId)) {
                throw DuplicateResourceException.of("User", "phone", phone);
            }
        });
    }

    private User getOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getLocale(),
                user.getProfile(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
