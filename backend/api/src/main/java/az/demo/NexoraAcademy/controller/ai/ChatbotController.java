package az.demo.NexoraAcademy.controller.ai;

import az.demo.NexoraAcademy.dto.ai.ChatbotMessageRequest;
import az.demo.NexoraAcademy.dto.ai.ChatbotMessageResponse;
import az.demo.NexoraAcademy.security.AuthenticatedUser;
import az.demo.NexoraAcademy.service.ai.ChatbotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kənar chat-bota mesaj göndərmək üçün endpoint.
 * Təhlükəsizlik qaydası üçün bax SecurityConfig ("/api/v1/chatbot/**").
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatbotMessageResponse> sendMessage(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ChatbotMessageRequest request) {

        String userId = principal != null ? principal.getId().toString() : null;
        return ResponseEntity.ok(chatbotService.sendMessage(request, userId));
    }
}
