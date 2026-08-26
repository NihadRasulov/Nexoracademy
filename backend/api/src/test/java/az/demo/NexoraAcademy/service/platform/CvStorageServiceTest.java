package az.demo.NexoraAcademy.service.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CvStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesPdfOutsidePublicMediaDirectoryAndLoadsIt() throws Exception {
        CvStorageService service = new CvStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "cv", "candidate.pdf", "application/pdf", "%PDF-1.7 test".getBytes());

        CvStorageService.StoredCv stored = service.store(file);

        assertEquals("candidate.pdf", stored.originalName());
        assertTrue(tempDir.resolve("cvs").resolve(stored.storedName()).toFile().isFile());
        assertTrue(service.load(stored.storedName()).isReadable());
    }

    @Test
    void rejectsSpoofedPdf() {
        CvStorageService service = new CvStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "cv", "candidate.pdf", "application/pdf", "not a pdf".getBytes());

        assertThrows(CvStorageService.InvalidCvException.class, () -> service.store(file));
    }

    @Test
    void rejectsUnsupportedType() {
        CvStorageService service = new CvStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "cv", "candidate.exe", "application/octet-stream", new byte[]{1, 2, 3});

        var error = assertThrows(CvStorageService.InvalidCvException.class, () -> service.store(file));
        assertEquals("Yalnız PDF, DOC və DOCX faylları qəbul olunur.", error.getMessage());
    }
}
