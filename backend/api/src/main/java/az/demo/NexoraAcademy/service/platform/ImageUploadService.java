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
public class ImageUploadService {

    public static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 5 MB

    private static final Map<String, Set<String>> ALLOWED_TYPES = Map.of(
            "image/jpeg", Set.of(".jpg", ".jpeg"),
            "image/png", Set.of(".png"),
            "image/webp", Set.of(".webp")
    );

    private static final String URL_PREFIX = "/uploads/courses/";

    private final Path uploadDir;

    public ImageUploadService(@Value("${app.upload.dir:/app/uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).resolve("courses").toAbsolutePath().normalize();
    }

    /**
     * Şəkli yoxlayır, {uploadDir}/courses/ altında saxlayır və public URL qaytarır.
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Fayl boşdur.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidImageException("Fayl ölçüsü 5MB limitini aşır.");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String extension = original.lastIndexOf('.') >= 0
                ? original.substring(original.lastIndexOf('.'))
                : "";
        Set<String> allowedExtensions = ALLOWED_TYPES.get(contentType);
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new InvalidImageException("Yalnız JPG, PNG və WEBP şəkilləri qəbul olunur.");
        }

        validateSignature(file, contentType);

        try {
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "_" + Instant.now().toEpochMilli() + extension;
            Path target = uploadDir.resolve(filename).normalize();
            // Path traversal qorunması: hədəf həmişə uploadDir daxilində olmalıdır.
            if (!target.startsWith(uploadDir)) {
                throw new InvalidImageException("Yanlış fayl adı.");
            }
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return URL_PREFIX + filename;
        } catch (IOException e) {
            throw new ImageStorageException("Şəkil saxlanıla bilmədi.", e);
        }
    }

    private void validateSignature(MultipartFile file, String contentType) {
        try (var input = file.getInputStream()) {
            byte[] header = input.readNBytes(16);
            boolean valid = switch (contentType) {
                case "image/jpeg" -> startsWith(header,
                        new int[]{0xFF, 0xD8, 0xFF}, 0);
                case "image/png" -> startsWith(header,
                        new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, 0);
                case "image/webp" -> startsWith(header,
                        new int[]{'R', 'I', 'F', 'F'}, 0)
                        && startsWith(header, new int[]{'W', 'E', 'B', 'P'}, 8);
                default -> false;
            };
            if (!valid) {
                throw new InvalidImageException("Faylın şəkil formatı təsdiqlənmədi.");
            }
        } catch (IOException exception) {
            throw new ImageStorageException("Şəkil oxuna bilmədi.", exception);
        }
    }

    private boolean startsWith(byte[] bytes, int[] expected, int offset) {
        if (bytes.length < offset + expected.length) return false;
        for (int index = 0; index < expected.length; index++) {
            if ((bytes[offset + index] & 0xFF) != expected[index]) return false;
        }
        return true;
    }

    /**
     * Public URL (/uploads/courses/xxx) üzrə faylı diskdən silir.
     * Yalnız öz prefiximizə uyğun URL-ləri silir — xarici URL-lər toxunulmazdır.
     */
    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(URL_PREFIX)) {
            return;
        }
        String filename = imageUrl.substring(URL_PREFIX.length());
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return;
        }
        try {
            Files.deleteIfExists(uploadDir.resolve(filename).normalize());
        } catch (IOException e) {
            // Silinməmə kritik deyil — orphan fayldır, log kifayətdir.
        }
    }

    public static class InvalidImageException extends ApiException {
        public InvalidImageException(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }

    public static class ImageStorageException extends ApiException {
        public ImageStorageException(String message, Throwable cause) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, message);
            initCause(cause);
        }
    }
}
