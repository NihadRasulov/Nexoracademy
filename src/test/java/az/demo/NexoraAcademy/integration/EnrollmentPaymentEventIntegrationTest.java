package az.demo.NexoraAcademy.integration;

import az.demo.NexoraAcademy.entity.catalog.Category;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.academics.CourseGroup;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import az.demo.NexoraAcademy.entity.enums.GroupStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.entity.notify.Notification;
import az.demo.NexoraAcademy.entity.platform.AuditLog;
import az.demo.NexoraAcademy.repository.academics.CourseGroupRepository;
import az.demo.NexoraAcademy.repository.catalog.CategoryRepository;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.repository.notify.NotificationRepository;
import az.demo.NexoraAcademy.repository.platform.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the event-driven side effects wired up for Tasks 17/18: confirming
 * an enrollment creates a Notification, and capturing a payment creates both
 * a Notification and an AuditLog row — all through real HTTP against the
 * live database, so AFTER_COMMIT listeners actually fire.
 *
 * Enrollment status and payments/** are staff-only operations (see EnrollmentService's
 * STAFF_ROLES / SecurityConfig): a STUDENT can only self-enroll at PENDING_PAYMENT and
 * can't create or capture payment records directly. So this test drives those two
 * calls as an ADMIN acting on the student's behalf, which is how that part of the
 * flow is meant to happen now — the student is only the enrollment/notification subject.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentPaymentEventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseGroupRepository courseGroupRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void confirmingEnrollmentAndCapturingPaymentTriggerNotificationsAndAuditLog() throws Exception {
        // --- fixtures, created directly via repositories for speed ---
        User student = new User();
        student.setEmail("event-flow-" + UUID.randomUUID() + "@example.com");
        student.setFullName("Event Flow Student");
        student.setPasswordHash(passwordEncoder.encode("studentpass123"));
        student.setRole(UserRole.STUDENT);
        student.setStatus(AccountStatus.ACTIVE);
        student.setProfile(new HashMap<>());
        student = userRepository.save(student);

        User admin = new User();
        admin.setEmail("event-flow-admin-" + UUID.randomUUID() + "@example.com");
        admin.setFullName("Event Flow Admin");
        admin.setPasswordHash(passwordEncoder.encode("adminpass123"));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setProfile(new HashMap<>());
        admin = userRepository.save(admin);

        Category category = new Category();
        category.setSlug("event-flow-cat-" + UUID.randomUUID().toString().substring(0, 8));
        category.setName("Event Flow Category");
        category.setSortOrder(0);
        category.setActive(true);
        category = categoryRepository.save(category);

        Course course = new Course();
        course.setSlug("event-flow-course-" + UUID.randomUUID().toString().substring(0, 8));
        course.setCategory(category);
        course.setTitle("Event Flow Course");
        course.setDifficulty(DifficultyLevel.BEGINNER);
        course.setDeliveryFormat(DeliveryFormat.ONLINE);
        course.setCurrency("AZN");
        course.setContent(new HashMap<>());
        course = courseRepository.save(course);

        CourseGroup group = new CourseGroup();
        group.setCourse(course);
        group.setGroupCode("EVT-" + UUID.randomUUID().toString().substring(0, 6));
        group.setStartDate(LocalDate.now().plusDays(7));
        group.setTotalSeats(30);
        group.setStatus(GroupStatus.OPEN);
        group.setSchedule(List.of());
        group = courseGroupRepository.save(group);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + admin.getEmail() + "\",\"password\":\"adminpass123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // --- confirm an enrollment directly (status=CONFIRMED) -> should notify ---
        String enrollBody = "{\"userId\":\"" + student.getId() + "\",\"groupId\":\"" + group.getId()
                + "\",\"status\":\"CONFIRMED\",\"idempotencyKey\":\"evt-enroll-" + UUID.randomUUID() + "\"}";

        String enrollResponse = mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(enrollBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String enrollmentId = objectMapper.readTree(enrollResponse).get("id").asText();

        User finalStudent = student;
        await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUser_Id(finalStudent.getId());
            assertThat(notifications).anyMatch(n -> n.getType().equals("enrollment_confirmed"));
        });

        // --- create + capture a payment -> should notify AND audit-log ---
        String paymentBody = "{\"enrollmentId\":\"" + enrollmentId + "\",\"method\":\"CARD\",\"amount\":150.00"
                + ",\"idempotencyKey\":\"evt-pay-" + UUID.randomUUID() + "\"}";

        String paymentResponse = mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(paymentBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String paymentId = objectMapper.readTree(paymentResponse).get("id").asText();

        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/capture")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUser_Id(finalStudent.getId());
            assertThat(notifications).anyMatch(n -> n.getType().equals("payment_completed"));

            List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityId("Payment", paymentId);
            assertThat(auditLogs).anyMatch(a -> a.getAction().equals("payment.complete"));
        });
    }
}
