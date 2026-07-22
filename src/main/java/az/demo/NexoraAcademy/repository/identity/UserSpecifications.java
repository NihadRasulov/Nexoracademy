package az.demo.NexoraAcademy.repository.identity;

import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> search(String query, UserRole role, AccountStatus status) {
        List<Specification<User>> specs = Stream.of(
                emailOrNameContains(query),
                roleEquals(role),
                statusEquals(status)
        ).filter(Objects::nonNull).toList();

        return specs.isEmpty() ? Specification.unrestricted() : Specification.allOf(specs);
    }

    private static Specification<User> emailOrNameContains(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String like = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), like),
                cb.like(cb.lower(root.get("fullName")), like)
        );
    }

    private static Specification<User> roleEquals(UserRole role) {
        return role == null ? null : (root, cq, cb) -> cb.equal(root.get("role"), role);
    }

    private static Specification<User> statusEquals(AccountStatus status) {
        return status == null ? null : (root, cq, cb) -> cb.equal(root.get("status"), status);
    }
}
