package az.demo.NexoraAcademy.service.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaUploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesValidatedMp4UnderMediaDirectory() throws Exception {
        var service = new MediaUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file",
                "hero.mp4",
                "video/mp4",
                new byte[]{0x00, 0x00, 0x00, 0x18, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'});

        String url = service.upload(file);

        assertTrue(url.startsWith("/uploads/media/"));
        String filename = url.substring("/uploads/media/".length());
        assertTrue(Files.exists(tempDir.resolve("media").resolve(filename)));
    }

    @Test
    void storesValidatedWebmUnderMediaDirectory() {
        var service = new MediaUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file",
                "hero.webm",
                "video/webm",
                new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0x01});

        String url = service.upload(file);

        assertTrue(url.startsWith("/uploads/media/"));
    }

    @Test
    void rejectsMismatchedOrUnsupportedMediaType() {
        var service = new MediaUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file",
                "hero.mp4",
                "image/png",
                new byte[]{0x01});

        var error = assertThrows(MediaUploadService.InvalidMediaException.class,
                () -> service.upload(file));

        assertEquals("Yalnız MP4 və WebM video faylları qəbul olunur.", error.getMessage());
    }

    @Test
    void rejectsSpoofedMp4WithoutContainerSignature() {
        var service = new MediaUploadService(tempDir.toString());
        var file = new MockMultipartFile(
                "file",
                "hero.mp4",
                "video/mp4",
                "not-a-video".getBytes());

        var error = assertThrows(MediaUploadService.InvalidMediaException.class,
                () -> service.upload(file));

        assertEquals("Faylın video formatı təsdiqlənmədi.", error.getMessage());
    }
}
