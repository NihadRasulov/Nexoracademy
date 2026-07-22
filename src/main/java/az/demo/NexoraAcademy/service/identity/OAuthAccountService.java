package az.demo.NexoraAcademy.service.identity;

import az.demo.NexoraAcademy.dto.identity.OAuthAccountRequest;
import az.demo.NexoraAcademy.dto.identity.OAuthAccountResponse;
import az.demo.NexoraAcademy.entity.identity.OAuthAccount;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.identity.OAuthAccountRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthAccountService {

    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<OAuthAccountResponse> findAll() {
        return oAuthAccountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OAuthAccountResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public OAuthAccountResponse create(OAuthAccountRequest request) {
        assertAvailable(request, null);

        OAuthAccount account = new OAuthAccount();
        account.setUser(resolveUser(request.userId()));
        account.setProvider(request.provider());
        account.setProviderUserId(request.providerUserId());
        account.setAccessTokenEnc(request.accessTokenEnc());
        account.setRefreshTokenEnc(request.refreshTokenEnc());

        return toResponse(oAuthAccountRepository.saveAndFlush(account));
    }

    public OAuthAccountResponse update(Long id, OAuthAccountRequest request) {
        OAuthAccount account = getOrThrow(id);
        assertAvailable(request, id);

        account.setUser(resolveUser(request.userId()));
        account.setProvider(request.provider());
        account.setProviderUserId(request.providerUserId());
        account.setAccessTokenEnc(request.accessTokenEnc());
        account.setRefreshTokenEnc(request.refreshTokenEnc());

        return toResponse(oAuthAccountRepository.saveAndFlush(account));
    }

    public OAuthAccountResponse patch(Long id, OAuthAccountRequest request) {
        OAuthAccount account = getOrThrow(id);

        if (request.userId() != null) account.setUser(resolveUser(request.userId()));
        if (request.provider() != null || request.providerUserId() != null) {
            assertAvailable(request, id);
            if (request.provider() != null) account.setProvider(request.provider());
            if (request.providerUserId() != null) account.setProviderUserId(request.providerUserId());
        }
        if (request.accessTokenEnc() != null) account.setAccessTokenEnc(request.accessTokenEnc());
        if (request.refreshTokenEnc() != null) account.setRefreshTokenEnc(request.refreshTokenEnc());

        return toResponse(oAuthAccountRepository.saveAndFlush(account));
    }

    public void delete(Long id) {
        if (!oAuthAccountRepository.existsById(id)) {
            throw ResourceNotFoundException.of("OAuthAccount", id);
        }
        oAuthAccountRepository.deleteById(id);
    }

    private void assertAvailable(OAuthAccountRequest request, Long currentId) {
        oAuthAccountRepository.findByProviderAndProviderUserId(request.provider(), request.providerUserId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentId)) {
                        throw DuplicateResourceException.of("OAuthAccount", "provider+providerUserId",
                                request.provider() + ":" + request.providerUserId());
                    }
                });
    }

    private User resolveUser(java.util.UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private OAuthAccount getOrThrow(Long id) {
        return oAuthAccountRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("OAuthAccount", id));
    }

    private OAuthAccountResponse toResponse(OAuthAccount account) {
        return new OAuthAccountResponse(
                account.getId(),
                account.getUser().getId(),
                account.getProvider(),
                account.getProviderUserId(),
                account.getLinkedAt()
        );
    }
}
