package az.demo.NexoraAcademy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class NexoraAcademyApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(NexoraAcademyApplication.class);
		application.addInitializers(context -> {
			var environment = context.getEnvironment();
			boolean production = Arrays.asList(environment.getActiveProfiles()).contains("prod");
			boolean configLoaded = environment.getProperty("app.config-loaded", Boolean.class, false);

			if (production && !configLoaded) {
				throw new IllegalStateException(
						"Production configuration was not loaded. Remove SPRING_CONFIG_LOCATION, "
								+ "SPRING_CONFIG_NAME and SPRING_CONFIG_ADDITIONAL_LOCATION overrides."
				);
			}
		});
		application.run(args);
	}

}
