package com.yugabyte.yugastore.test;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.yugabyte.yugastore.ui.YugastoreFrontend;

@SpringBootTest(classes = YugastoreFrontend.class)
public class SpringAndReactApplicationTests {

	@Test
	public void contextLoads() {
	}

}
