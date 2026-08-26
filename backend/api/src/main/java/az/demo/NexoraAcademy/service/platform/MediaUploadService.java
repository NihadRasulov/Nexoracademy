package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaUploadService {

    public static final long MAX_MEDIA_SIZE = 25L * 1024 * 1024;

    private static final Map<String, Set<String>> ALLOWED_TYPES = Map.of(
            "video/mp4", Set.of(".mp4"),
            "video/webm", Set.of(".webm")
    );

    private static final String URL_PREFIX = "/uploads/media/";

    private final Path uploadDir;

    public MediaUploadService(@Value("${app.upload.dir:/app/uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).resolve("media").toAbsolutePath().normalize();
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidMediaException("Fayl boşdur.");
        }
        if (file.getSize() > MAX_MEDIA_SIZE) {
            throw new InvalidMediaException("Video ölçüsü 25MB limitini aşır.");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String extension = original.lastIndexOf('.') >= 0
                ? original.substring(original.lastIndexOf('.'))
                : "";

        Set<String> allowedExtensions = ALLOWED_TYPES.get(contentType);
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new InvalidMediaException("Yalnız MP4 və WebM video faylları qəbul olunur.");
        }

        validateSignature(file, contentType);

        try {
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "_" + Instant.now().toEpochMilli() + extension;
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new InvalidMediaException("Yanlış fayl adı.");
            }
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return URL_PREFIX + filename;
        } catch (IOException exception) {
            throw new MediaStorageException("Video saxlanıla bilmədi.", exception);
        }
    }

    private void validateSignature(MultipartFile file, String contentType) {
        try (var input = file.getInputStream()) {
            byte[] header = input.readNBytes(64);
            boolean valid = switch (contentType) {
                case "video/mp4" -> containsAscii(header, "ftyp", 4);
                case "video/webm" -> header.length >= 4
                        && (header[0] & 0xFF) == 0x1A
                        && (header[1] & 0xFF) == 0x45
                        && (header[2] & 0xFF) == 0xDF
                        && (header[3] & 0xFF) == 0xA3;
                default -> false;
            };
            if (!valid) {
                throw new InvalidMediaException("Faylın video formatı təsdiqlənmədi.");
            }
        } catch (IOException exception) {
            throw new MediaStorageException("Video oxuna bilmədi.", exception);
        }
    }

    private boolean containsAscii(byte[] bytes, String marker, int startIndex) {
        byte[] expected = marker.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        for (int index = Math.max(0, startIndex); index <= bytes.length - expected.length; index++) {
            boolean matches = true;
            for (int offset = 0; offset < expected.length; offset++) {
                if (bytes[index + offset] != expected[offset]) {
                    matches = false;
                    break;
                }
            }
            if (matches) return true;
        }
        return false;
    }

    public static class InvalidMediaException extends ApiException {
        public InvalidMediaException(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }

    public static class MediaStorageException extends ApiException {
        public MediaStorageException(String message, Throwable cause) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, message);
            initCause(cause);
        }
    }
}
