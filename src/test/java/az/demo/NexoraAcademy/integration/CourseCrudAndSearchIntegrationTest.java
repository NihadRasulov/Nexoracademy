package az.demo.NexoraAcademy.integration;

import az.demo.NexoraAcademy.entity.catalog.Category;
import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.repository.catalog.CategoryRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies: public GET works unauthenticated, unauthenticated POST is
 * rejected, an ADMIN can create a course, and the search/filter/pagination
 * endpoint (Task 13) finds it.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CourseCrudAndSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private Short categoryId;

    @BeforeEach
    void setUp() throws Exception {
        String adminEmail = "admin-" + UUID.randomUUID() + "@example.com";
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setFullName("Test Admin");
        admin.setPasswordHash(passwordEncoder.encode("adminpass123"));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setProfile(new HashMap<>());
        userRepository.save(admin);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + adminEmail + "\",\"password\":\"adminpass123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        adminToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        Category category = new Category();
        category.setSlug("integration-test-cat-" + UUID.randomUUID().toString().substring(0, 8));
        category.setName("Integration Test Category");
        category.setSortOrder(0);
        category.setActive(true);
        categoryId = categoryRepository.save(category).getId();
    }

    @Test
    void unauthenticatedUserCanReadButNotWriteCourses() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk());

        String body = "{\"slug\":\"unauth-course\",\"categoryId\":" + categoryId
                + ",\"title\":\"Unauth Course\",\"difficulty\":\"BEGINNER\",\"deliveryFormat\":\"ONLINE\"}";

        mockMvc.perform(post("/api/v1/courses").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateCourseAndItIsFoundBySearch() throws Exception {
        String uniqueWord = "Zzyxab" + UUID.randomUUID().toString().substring(0, 6);
        String slug = "search-course-" + UUID.randomUUID().toString().substring(0, 8);
        String body = "{\"slug\":\"" + slug + "\",\"categoryId\":" + categoryId
                + ",\"title\":\"" + uniqueWord + " Spring Boot Masterclass\""
                + ",\"difficulty\":\"INTERMEDIATE\",\"deliveryFormat\":\"ONLINE\",\"published\":true,\"active\":true}";

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value(slug));

        mockMvc.perform(get("/api/v1/courses")
                        .param("q", uniqueWord)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value(slug))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void creatingCourseWithInvalidSlugPatternReturns400() throws Exception {
        String body = "{\"slug\":\"Invalid Slug With Spaces\",\"categoryId\":" + categoryId
                + ",\"title\":\"Bad Slug Course\",\"difficulty\":\"BEGINNER\",\"deliveryFormat\":\"ONLINE\"}";

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.slug").exists());
    }
}
