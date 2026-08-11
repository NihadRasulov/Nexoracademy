package az.demo.NexoraAcademy.logging;

import az.demo.NexoraAcademy.security.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Automatically logs every create/read/update/delete call across the
 * service layer — one line per call, routed to its own log file (see
 * logback-spring.xml: logs/create, logs/read, logs/update, logs/delete),
 * plus every failure to logs/error regardless of which bucket it came from.
 *
 * Request DTO field values are never logged (they can carry passwords/
 * tokens) — only the DTO's type name. Simple identifiers (UUID, String,
 * numbers, Pageable) are safe and are logged as-is.
 */
@Aspect
@Component
public class CrudLoggingAspect {

    private static final Logger CREATE_LOG = LoggerFactory.getLogger("AUDIT_CREATE");
    private static final Logger READ_LOG = LoggerFactory.getLogger("AUDIT_READ");
    private static final Logger UPDATE_LOG = LoggerFactory.getLogger("AUDIT_UPDATE");
    private static final Logger DELETE_LOG = LoggerFactory.getLogger("AUDIT_DELETE");
    private static final Logger ERROR_LOG = LoggerFactory.getLogger(CrudLoggingAspect.class);

    private enum Operation { CREATE, READ, UPDATE, DELETE }

    @Around("execution(public * az.demo.NexoraAcademy.service..*.*(..)) "
            + "&& !target(az.demo.NexoraAcademy.service.JwtService) "
            + "&& !target(az.demo.NexoraAcademy.service.notify.EmailService)")
    public Object logCrud(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Operation operation = classify(methodName);
        if (operation == null) {
            return joinPoint.proceed();
        }

        String service = joinPoint.getTarget().getClass().getSimpleName();
        String actor = currentActor();
        String args = summarizeArgs(joinPoint.getArgs());
        long startNanos = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            loggerFor(operation).info(
                    "{} | service={} | method={} | actor={} | args=[{}] | result={} | durationMs={} | status=SUCCESS",
                    operation, service, methodName, actor, args, summarizeResult(result), durationMs);
            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            ERROR_LOG.error(
                    "{} | service={} | method={} | actor={} | args=[{}] | durationMs={} | status=FAILED | exception={}: {}",
                    operation, service, methodName, actor, args, durationMs, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private Logger loggerFor(Operation operation) {
        return switch (operation) {
            case CREATE -> CREATE_LOG;
            case READ -> READ_LOG;
            case UPDATE -> UPDATE_LOG;
            case DELETE -> DELETE_LOG;
        };
    }

    private Operation classify(String methodName) {
        String name = methodName.toLowerCase();
        if (name.startsWith("create") || name.startsWith("register")) return Operation.CREATE;
        if (name.startsWith("find") || name.startsWith("get") || name.startsWith("search")) return Operation.READ;
        if (name.startsWith("update") || name.startsWith("patch")) return Operation.UPDATE;
        if (name.startsWith("delete")) return Operation.DELETE;
        return null;
    }

    private String currentActor() {
        UUID id = SecurityUtils.currentUserId();
        return id != null ? id.toString() : "anonymous/system";
    }

    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args).map(this::summarizeArg).collect(Collectors.joining(", "));
    }

    private String summarizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof UUID || arg instanceof String || arg instanceof Number || arg instanceof Boolean
                || arg instanceof Pageable || arg.getClass().isEnum()) {
            return arg.toString();
        }
        // Request DTOs and anything else: log only the type, never field values
        // (a request record's toString() would include passwords/tokens verbatim).
        return arg.getClass().getSimpleName();
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "void";
        }
        if (result instanceof Page<?> page) {
            return "page(size=" + page.getNumberOfElements() + ", totalElements=" + page.getTotalElements() + ")";
        }
        try {
            Method idAccessor = result.getClass().getMethod("id");
            return result.getClass().getSimpleName() + "(id=" + idAccessor.invoke(result) + ")";
        } catch (ReflectiveOperationException e) {
            if (result instanceof Iterable<?> iterable) {
                int count = 0;
                for (Object ignored : iterable) {
                    count++;
                }
                return "list(size=" + count + ")";
            }
            return result.getClass().getSimpleName();
        }
    }
}
