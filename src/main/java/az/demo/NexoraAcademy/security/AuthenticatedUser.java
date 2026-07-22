package az.demo.NexoraAcademy.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

/**
 * UserDetails that also carries the domain user's UUID, so controllers and
 * services can resolve "who's calling" (e.g. /me endpoints, audit actor id)
 * without an extra findByEmail lookup.
 */
public class AuthenticatedUser extends User {

    private final UUID id;

    public AuthenticatedUser(UUID id, String email, String passwordHash, boolean enabled, boolean accountNonLocked,
                              Collection<? extends GrantedAuthority> authorities) {
        super(email, passwordHash == null ? "" : passwordHash, enabled, true, true, accountNonLocked, authorities);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
