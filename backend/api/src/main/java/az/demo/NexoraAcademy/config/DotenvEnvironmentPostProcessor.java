package az.demo.NexoraAcademy.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads a ".env" file (if present in the working directory) into a low-priority
 * property source, so DB_HOST/DB_PASSWORD/JWT_SECRET/etc. placeholders in
 * application.yml resolve without every developer having to export real shell
 * env vars by hand. A real OS environment variable of the same name always
 * wins (this is added with the lowest precedence).
 *
 * This is hand-rolled rather than using a third-party "spring-dotenv" style
 * library: the popular one on Maven Central only implements the older
 * org.springframework.boot.env.EnvironmentPostProcessor interface. Spring
 * Boot 4.x introduced a new org.springframework.boot.EnvironmentPostProcessor
 * (same method signature, different package) that its own bootstrap actually
 * looks for via spring.factories — the old interface is no longer picked up,
 * so that library silently never runs (no error, .env is just never read).
 * See the registration file: META-INF/spring.factories.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_FILENAME = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = Path.of(DOTENV_FILENAME);
        if (!Files.isRegularFile(envFile)) {
            return;
        }

        Map<String, Object> values = parse(envFile);
        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
        }
    }

    private Map<String, Object> parse(Path envFile) {
        Map<String, Object> values = new LinkedHashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(envFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + envFile.toAbsolutePath(), e);
        }

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = unquote(line.substring(separator + 1).trim());
            values.put(key, value);
        }
        return values;
    }

    private String unquote(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
