package az.demo.NexoraAcademy;

import az.demo.NexoraAcademy.integration.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NexoraAcademyApplicationTests {

	@Test
	void contextLoads() {
	}

}
