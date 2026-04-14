package com.yugabyte.app.yugastore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.yugabyte.app.yugastore.cart.YugastoreCart;

@SpringBootTest(classes = YugastoreCart.class)
public class YugastoreCartTests {

	@Test
	public void contextLoads() {
	}

}
