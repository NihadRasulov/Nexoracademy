package az.demo.NexoraAcademy.controller.identity;

import az.demo.NexoraAcademy.dto.identity.OAuthAccountRequest;
import az.demo.NexoraAcademy.dto.identity.OAuthAccountResponse;
import az.demo.NexoraAcademy.service.identity.OAuthAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/oauth-accounts")
@RequiredArgsConstructor
@Tag(name = "OAuth Accounts")
public class OAuthAccountController {

    private final OAuthAccountService oAuthAccountService;

    @PostMapping
    public ResponseEntity<OAuthAccountResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody OAuthAccountRequest request) {
        OAuthAccountResponse response = oAuthAccountService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OAuthAccountResponse>> findAll() {
        return ResponseEntity.ok(oAuthAccountService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OAuthAccountResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(oAuthAccountService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OAuthAccountResponse> update(@PathVariable Long id, @Validated(ValidationGroups.OnCreate.class) @RequestBody OAuthAccountRequest request) {
        return ResponseEntity.ok(oAuthAccountService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OAuthAccountResponse> patch(@PathVariable Long id, @Valid @RequestBody OAuthAccountRequest request) {
        return ResponseEntity.ok(oAuthAccountService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        oAuthAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
