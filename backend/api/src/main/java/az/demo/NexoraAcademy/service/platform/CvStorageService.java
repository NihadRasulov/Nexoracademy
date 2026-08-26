package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CvStorageService {

    public static final long MAX_CV_SIZE = 10L * 1024 * 1024;

    private static final Map<String, Set<String>> ALLOWED_TYPES = Map.of(
            "application/pdf", Set.of(".pdf"),
            "application/msword", Set.of(".doc"),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of(".docx")
    );

    private final Path uploadDir;

    public CvStorageService(@Value("${app.upload.dir:/app/uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).resolve("cvs").toAbsolutePath().normalize();
    }

    public StoredCv store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCvException("CV faylı tələb olunur.");
        }
        if (file.getSize() > MAX_CV_SIZE) {
            throw new InvalidCvException("CV ölçüsü 10MB limitini aşır.");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String originalName = file.getOriginalFilename() == null
                ? "cv"
                : Path.of(file.getOriginalFilename()).getFileName().toString();
        String lowerName = originalName.toLowerCase(Locale.ROOT);
        String extension = lowerName.lastIndexOf('.') >= 0
                ? lowerName.substring(lowerName.lastIndexOf('.'))
                : "";

        Set<String> extensions = ALLOWED_TYPES.get(contentType);
        if (extensions == null || !extensions.contains(extension)) {
            throw new InvalidCvException("Yalnız PDF, DOC və DOCX faylları qəbul olunur.");
        }

        validateSignature(file, extension);

        String storedName = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new InvalidCvException("Yanlış fayl adı.");
        }

        try {
            Files.createDirectories(uploadDir);
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredCv(originalName, storedName);
        } catch (IOException exception) {
            throw new CvStorageException("CV saxlanıla bilmədi.", exception);
        }
    }

    public Resource load(String storedName) {
        Path target = resolve(storedName);
        try {
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new CvNotFoundException();
            }
            return resource;
        } catch (java.net.MalformedURLException exception) {
            throw new CvNotFoundException();
        }
    }

    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) return;
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException exception) {
            throw new CvStorageException("CV silinə bilmədi.", exception);
        }
    }

    private Path resolve(String storedName) {
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new CvNotFoundException();
        }
        return target;
    }

    private void validateSignature(MultipartFile file, String extension) {
        try (var input = file.getInputStream()) {
            byte[] header = input.readNBytes(8);
            boolean valid = switch (extension) {
                case ".pdf" -> startsWith(header, new byte[]{'%', 'P', 'D', 'F', '-'});
                case ".doc" -> startsWith(header, new byte[]{
                        (byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0,
                        (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1});
                case ".docx" -> startsWith(header, new byte[]{'P', 'K'});
                default -> false;
            };
            if (!valid) {
                throw new InvalidCvException("CV faylının formatı təsdiqlənmədi.");
            }
        } catch (IOException exception) {
            throw new CvStorageException("CV oxuna bilmədi.", exception);
        }
    }

    private boolean startsWith(byte[] actual, byte[] expected) {
        if (actual.length < expected.length) return false;
        for (int index = 0; index < expected.length; index++) {
            if (actual[index] != expected[index]) return false;
        }
        return true;
    }

    public record StoredCv(String originalName, String storedName) {
    }

    public static class InvalidCvException extends ApiException {
        public InvalidCvException(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }

    public static class CvNotFoundException extends ApiException {
        public CvNotFoundException() {
            super(HttpStatus.NOT_FOUND, "CV tapılmadı.");
        }
    }

    public static class CvStorageException extends ApiException {
        public CvStorageException(String message, Throwable cause) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, message);
            initCause(cause);
        }
    }
}
