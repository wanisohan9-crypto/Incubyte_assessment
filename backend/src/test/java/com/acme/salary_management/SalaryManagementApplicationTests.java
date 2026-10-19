package com.acme.salary_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"app.seed.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.datasource.url=jdbc:sqlite::memory:",
		"spring.datasource.hikari.maximum-pool-size=1"
})
class SalaryManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
