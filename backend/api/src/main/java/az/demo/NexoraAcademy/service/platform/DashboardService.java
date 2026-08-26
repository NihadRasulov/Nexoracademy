package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.dto.platform.DashboardSummaryResponse;
import az.demo.NexoraAcademy.entity.enums.CmsContentType;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.cms.CmsContentRepository;
import az.demo.NexoraAcademy.repository.crm.ContactSubmissionRepository;
import az.demo.NexoraAcademy.repository.crm.NewsletterSubscriptionRepository;
import az.demo.NexoraAcademy.repository.platform.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final CourseRepository courseRepository;
    private final CmsContentRepository cmsContentRepository;
    private final ApplicationRepository applicationRepository;
    private final ContactSubmissionRepository contactSubmissionRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        var applicationItems = applicationRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(item -> new DashboardSummaryResponse.ActivityItem(
                        "application", item.getFullname(), applicationType(item.getApplicationType()), item.getCreatedAt()));
        var contactItems = contactSubmissionRepository.findTop5ByOrderBySubmittedAtDesc().stream()
                .map(item -> new DashboardSummaryResponse.ActivityItem(
                        "contact", item.getFullName(), item.getEmail(), item.getSubmittedAt()));
        var recent = Stream.concat(applicationItems, contactItems)
                .sorted(Comparator.comparing(DashboardSummaryResponse.ActivityItem::createdAt).reversed())
                .limit(6)
                .toList();

        return new DashboardSummaryResponse(
                courseRepository.countByPublishedTrueAndActiveTrueAndDeletedAtIsNull(),
                cmsContentRepository.countByTypeAndPublishedTrue(CmsContentType.NEWS),
                applicationRepository.countByStatus("PENDING"),
                contactSubmissionRepository.countByStatus("pending"),
                newsletterSubscriptionRepository.countByActiveTrue(),
                recent);
    }

    private String applicationType(Short type) {
        if (type == null) return "Müraciət";
        return switch (type) {
            case 1 -> "Kurs müraciəti";
            case 2 -> "Demo dərs sorğusu";
            case 3 -> "Karyera və mentor dəstəyi";
            case 4 -> "Ümumi əlaqə";
            default -> "Müraciət";
        };
    }
}
