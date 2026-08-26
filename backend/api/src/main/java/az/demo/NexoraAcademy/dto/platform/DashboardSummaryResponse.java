package az.demo.NexoraAcademy.dto.platform;

import java.time.Instant;
import java.util.List;

public record DashboardSummaryResponse(
        long publishedCourses,
        long publishedNews,
        long pendingApplications,
        long pendingContacts,
        long activeSubscribers,
        List<ActivityItem> recentActivity) {

    public record ActivityItem(String type, String title, String detail, Instant createdAt) {
    }
}
