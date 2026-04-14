package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Context-load test disabled: products-microservice uses YCQL (Cassandra) and
 * requires a live YugabyteDB instance. Use the Mockito-based unit tests instead.
 */
@Disabled("Requires a live YugabyteDB/Cassandra instance")
@SpringBootTest
public class CassandraClientApplicationTests {

	@Test
	public void contextLoads() {
	}

}
