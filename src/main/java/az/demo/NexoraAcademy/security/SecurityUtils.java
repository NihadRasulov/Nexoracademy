package az.demo.NexoraAcademy.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Returns the currently authenticated user's id, or null (system/anonymous action, or no security context — e.g. tests). */
    public static UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser principal) {
            return principal.getId();
        }
        return null;
    }

    /**
     * True if the caller holds any of the given roles (without the "ROLE_" prefix, e.g. "ADMIN").
     * Used by services that need finer-grained, ownership-aware access control than the
     * path-based rules in SecurityConfig can express (e.g. "staff can act on anyone's
     * enrollment, everyone else only on their own").
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            for (String role : roles) {
                if (authority.getAuthority().equals("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
