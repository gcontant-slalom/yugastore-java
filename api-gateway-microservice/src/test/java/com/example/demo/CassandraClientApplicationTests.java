package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context-load test disabled: api-gateway-microservice requires Eureka and
 * all downstream Feign clients to be available.
 */
@Disabled("Requires a live Eureka server and all downstream microservices")
@SpringBootTest
public class CassandraClientApplicationTests {

	@Test
	public void contextLoads() {
	}

}
