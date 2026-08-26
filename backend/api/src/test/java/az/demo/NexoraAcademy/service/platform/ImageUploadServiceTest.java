package az.demo.NexoraAcademy.service.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesValidatedPngUnderCoursesDirectory() throws Exception {
        var service = new ImageUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file",
                "course.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01});

        String url = service.upload(file);

        assertTrue(url.startsWith("/uploads/courses/"));
        String filename = url.substring("/uploads/courses/".length());
        assertTrue(Files.exists(tempDir.resolve("courses").resolve(filename)));
    }

    @Test
    void rejectsSvgUploads() {
        var service = new ImageUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file", "course.svg", "image/svg+xml", "<svg/>".getBytes());

        var error = assertThrows(ImageUploadService.InvalidImageException.class,
                () -> service.upload(file));

        assertEquals("Yalnız JPG, PNG və WEBP şəkilləri qəbul olunur.", error.getMessage());
    }

    @Test
    void rejectsSpoofedPngWithoutSignature() {
        var service = new ImageUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file", "course.png", "image/png", "not-an-image".getBytes());

        var error = assertThrows(ImageUploadService.InvalidImageException.class,
                () -> service.upload(file));

        assertEquals("Faylın şəkil formatı təsdiqlənmədi.", error.getMessage());
    }

    @Test
    void deletesOnlyManagedCourseImage() throws Exception {
        var service = new ImageUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file",
                "course.webp",
                "image/webp",
                new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'});
        String url = service.upload(file);
        Path stored = tempDir.resolve("courses").resolve(url.substring("/uploads/courses/".length()));

        service.delete(url);

        assertFalse(Files.exists(stored));
    }
}
