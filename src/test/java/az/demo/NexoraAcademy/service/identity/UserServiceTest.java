package az.demo.NexoraAcademy.service.identity;

import az.demo.NexoraAcademy.dto.identity.ChangePasswordRequest;
import az.demo.NexoraAcademy.dto.identity.UserRequest;
import az.demo.NexoraAcademy.dto.identity.UserResponse;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidCredentialsException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail("existing@example.com");
        existingUser.setFullName("Existing User");
        existingUser.setPasswordHash("hashed-old-password");
        existingUser.setRole(UserRole.STUDENT);
        existingUser.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void createThrowsDuplicateResourceExceptionWhenEmailAlreadyExists() {
        UserRequest request = new UserRequest("existing@example.com", null, "New User", "pass1234",
                null, null, null, null);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void createDefaultsRoleAndStatusWhenNotProvided() {
        UserRequest request = new UserRequest("new@example.com", null, "New User", "pass1234",
                null, null, null, null);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request);

        assertThat(response.role()).isEqualTo(UserRole.STUDENT);
        assertThat(response.status()).isEqualTo(AccountStatus.PENDING_VERIFICATION);
    }

    @Test
    void findByIdThrowsResourceNotFoundExceptionWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patchOnlyAppliesNonNullFields() {
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserRequest patch = new UserRequest(null, null, "Updated Name Only", null, null, null, null, null);
        UserResponse response = userService.patch(existingUser.getId(), patch);

        assertThat(response.fullName()).isEqualTo("Updated Name Only");
        assertThat(response.email()).isEqualTo("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePasswordThrowsInvalidCredentialsWhenCurrentPasswordWrong() {
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "hashed-old-password")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "newpass123");

        assertThatThrownBy(() -> userService.changePassword(existingUser.getId(), request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void changePasswordUpdatesHashWhenCurrentPasswordCorrect() {
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("old-password", "hashed-old-password")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("hashed-new-password");

        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "newpass123");
        userService.changePassword(existingUser.getId(), request);

        assertThat(existingUser.getPasswordHash()).isEqualTo("hashed-new-password");
        verify(userRepository).saveAndFlush(existingUser);
    }

    @Test
    void deleteThrowsResourceNotFoundExceptionWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(missingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
