package az.demo.NexoraAcademy.controller.identity;

import az.demo.NexoraAcademy.dto.identity.ChangePasswordRequest;
import az.demo.NexoraAcademy.dto.identity.UpdateProfileRequest;
import az.demo.NexoraAcademy.dto.identity.UserResponse;
import az.demo.NexoraAcademy.security.AuthenticatedUser;
import az.demo.NexoraAcademy.service.identity.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userService.findById(principal.getId()));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateMyProfile(@AuthenticationPrincipal AuthenticatedUser principal,
                                                         @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changeMyPassword(@AuthenticationPrincipal AuthenticatedUser principal,
                                                  @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
