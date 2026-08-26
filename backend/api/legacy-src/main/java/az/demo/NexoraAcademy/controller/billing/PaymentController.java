package az.demo.NexoraAcademy.controller.billing;

import az.demo.NexoraAcademy.dto.billing.PaymentCallbackRequest;
import az.demo.NexoraAcademy.dto.billing.PaymentRequest;
import az.demo.NexoraAcademy.dto.billing.PaymentResponse;
import az.demo.NexoraAcademy.service.billing.PaymentService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentResponse> patch(@PathVariable UUID id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.capture(id));
    }

    /**
     * Gateway webhook target — see SecurityConfig, this path is intentionally
     * public (a gateway can't carry our JWT). PaymentCallbackSignatureFilter
     * verifies the gateway's HMAC signature before this method is ever reached
     * (bax PaymentGatewaySignatureVerifier) — but only once PAYMENT_GATEWAY_WEBHOOK_SECRET
     * is actually configured. Until a real gateway is chosen and that secret is set,
     * verification is bypassed (with a WARN log on every callback).
     */
    @PostMapping("/callback")
    public ResponseEntity<PaymentResponse> callback(@Valid @RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.ok(paymentService.handleCallback(request));
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
