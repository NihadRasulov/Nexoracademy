package az.demo.NexoraAcademy.controller.identity;

import az.demo.NexoraAcademy.dto.identity.ChangePasswordRequest;
import az.demo.NexoraAcademy.dto.identity.UpdateProfileRequest;
import az.demo.NexoraAcademy.dto.identity.UserRequest;
import az.demo.NexoraAcademy.dto.identity.UserResponse;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.security.AuthenticatedUser;
import az.demo.NexoraAcademy.service.identity.UserService;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    // --- self-service (any authenticated user) ---------------------------------

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userService.findById(principal.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@AuthenticationPrincipal AuthenticatedUser principal,
                                                          @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    // --- admin management (see SecurityConfig: /api/v1/users/** requires ADMIN) ------

    @PostMapping
    public ResponseEntity<UserResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    /** Supports free-text search (email/full name) plus filters, paginated and sortable. */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(userService.search(q, role, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patch(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
