package az.demo.NexoraAcademy.service.crm;

import az.demo.NexoraAcademy.dto.crm.ChatSessionRequest;
import az.demo.NexoraAcademy.dto.crm.ChatSessionResponse;
import az.demo.NexoraAcademy.entity.crm.ChatSession;
import az.demo.NexoraAcademy.entity.crm.Lead;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.crm.ChatSessionRepository;
import az.demo.NexoraAcademy.repository.crm.LeadRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> findAll() {
        return chatSessionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ChatSessionResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public ChatSessionResponse create(ChatSessionRequest request) {
        ChatSession session = new ChatSession();
        session.setUser(resolveUser(request.userId()));
        session.setLead(resolveLead(request.leadId()));
        session.setChannel(request.channel() != null ? request.channel() : "web_widget");
        session.setMessages(request.messages() != null ? request.messages() : new ArrayList<>());

        return toResponse(chatSessionRepository.saveAndFlush(session));
    }

    public ChatSessionResponse update(UUID id, ChatSessionRequest request) {
        ChatSession session = getOrThrow(id);

        session.setUser(resolveUser(request.userId()));
        session.setLead(resolveLead(request.leadId()));
        session.setChannel(request.channel() != null ? request.channel() : session.getChannel());
        session.setMessages(request.messages() != null ? request.messages() : session.getMessages());

        return toResponse(chatSessionRepository.saveAndFlush(session));
    }

    public ChatSessionResponse patch(UUID id, ChatSessionRequest request) {
        ChatSession session = getOrThrow(id);

        if (request.userId() != null) session.setUser(resolveUser(request.userId()));
        if (request.leadId() != null) session.setLead(resolveLead(request.leadId()));
        if (request.channel() != null) session.setChannel(request.channel());
        if (request.messages() != null) session.setMessages(request.messages());

        return toResponse(chatSessionRepository.saveAndFlush(session));
    }

    /** Closes the chat session (sets ended_at). */
    public ChatSessionResponse end(UUID id) {
        ChatSession session = getOrThrow(id);
        session.setEndedAt(java.time.Instant.now());
        return toResponse(chatSessionRepository.saveAndFlush(session));
    }

    public void delete(UUID id) {
        if (!chatSessionRepository.existsById(id)) {
            throw ResourceNotFoundException.of("ChatSession", id);
        }
        chatSessionRepository.deleteById(id);
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Lead resolveLead(UUID leadId) {
        if (leadId == null) {
            return null;
        }
        return leadRepository.findById(leadId).orElseThrow(() -> ResourceNotFoundException.of("Lead", leadId));
    }

    private ChatSession getOrThrow(UUID id) {
        return chatSessionRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ChatSession", id));
    }

    private ChatSessionResponse toResponse(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getUser() != null ? session.getUser().getId() : null,
                session.getLead() != null ? session.getLead().getId() : null,
                session.getChannel(),
                session.getMessages(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
