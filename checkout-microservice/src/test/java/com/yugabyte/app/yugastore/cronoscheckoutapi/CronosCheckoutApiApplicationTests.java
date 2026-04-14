package com.yugabyte.app.yugastore.cronoscheckoutapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context-load test disabled: checkout-microservice uses YCQL (Cassandra) and
 * requires a live YugabyteDB instance plus Eureka/Feign service discovery.
 */
@Disabled("Requires a live YugabyteDB/Cassandra instance and Eureka")
@SpringBootTest
public class CronosCheckoutApiApplicationTests {

	@Test
	public void contextLoads() {
	}

}
