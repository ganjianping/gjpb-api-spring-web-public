package org.ganjp.blog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BlogApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private RootController rootController;
	
	@Value("${spring.application.name:GJPB}")
	private String applicationName;

	@Test
	@DisplayName("Context loads successfully")
	void contextLoads() {
		assertNotNull(applicationContext, "Application context should not be null");
	}
	
	@Test
	@DisplayName("Root controller is properly initialized")
	void rootControllerLoads() {
		assertNotNull(rootController, "RootController should be initialized");
		String welcomeMessage = rootController.getWelcomeMessage();
		assertThat(welcomeMessage).contains("Welcome");
		assertThat(welcomeMessage).contains("Blog APIs");
	}
	
	@Test
	@DisplayName("Application has the correct name")
	void applicationNameIsCorrect() {
		assertThat(applicationName).isEqualTo("GJPB");
	}
	
	@Test
	@DisplayName("Application has necessary beans")
	void requiredBeansExist() {
		assertThat(applicationContext.containsBean("rootController")).isTrue();
	}
	
	@Test
	@DisplayName("Default profile is active when no profile specified")
	void defaultProfileActive() {
		assertThat(environment.getActiveProfiles()).contains("dev");
		assertThat(environment.getDefaultProfiles()).contains("default");
	}
}
