package com.yugabyte.yugastore.ui.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DashboardRestConsumerTest {

	private DashboardRestConsumer dashboardRestConsumer;
	private MockRestServiceServer server;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		dashboardRestConsumer = new DashboardRestConsumer();
		ReflectionTestUtils.setField(dashboardRestConsumer, "restTemplate", restTemplate);
		ReflectionTestUtils.setField(dashboardRestConsumer, "restUrlBase", "http://localhost:8081/api/v1/");
		server = MockRestServiceServer.bindTo(restTemplate).build();
	}

	@Test
	void register_stripsHopByHopHeadersFromErrorResponse() {
		HttpHeaders downstreamHeaders = new HttpHeaders();
		downstreamHeaders.setContentType(MediaType.APPLICATION_JSON);
		downstreamHeaders.add(HttpHeaders.TRANSFER_ENCODING, "chunked");
		downstreamHeaders.add(HttpHeaders.SET_COOKIE, "JSESSIONID=internal");

		server.expect(requestTo("http://localhost:8081/api/v1/auth/register"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("{\"email\":\"merchant@example.com\",\"password\":\"test123\",\"passwordConfirm\":\"test123\"}"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST)
						.headers(downstreamHeaders)
						.body("{\"message\":\"Registration request rejected.\",\"fieldErrors\":{\"password\":\"Try one with at least 8 characters.\"}}"));

		ResponseEntity<String> response = dashboardRestConsumer.register(
				"{\"email\":\"merchant@example.com\",\"password\":\"test123\",\"passwordConfirm\":\"test123\"}");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getHeaders().containsKey(HttpHeaders.TRANSFER_ENCODING)).isFalse();
		assertThat(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE)).isFalse();
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(response.getBody()).contains("Try one with at least 8 characters.");

		server.verify();
	}

	@Test
	void createMerchantSignup_targetsGatewayMerchantSignupRoute() {
		server.expect(requestTo("http://localhost:8081/api/v1/merchant-signup"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("{\"companyName\":\"Northwind Books\",\"tenantKey\":\"northwind-books\"}"))
				.andRespond(withStatus(HttpStatus.CREATED)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"tenantId\":\"8\",\"tenantKey\":\"northwind-books\",\"companyName\":\"Northwind Books\",\"merchantAdminUserId\":\"42\"}"));

		ResponseEntity<String> response = dashboardRestConsumer.createMerchantSignup(
				"{\"companyName\":\"Northwind Books\",\"tenantKey\":\"northwind-books\"}");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).contains("northwind-books");

		server.verify();
	}

	@Test
	void getMerchantContext_targetsGatewayMerchantContextRoute() {
		server.expect(requestTo("http://localhost:8081/api/v1/merchant-context"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(DashboardRestConsumer.AUTH_USER_ID_HEADER, "42"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"tenantId\":\"8\",\"tenantKey\":\"northwind-books\",\"companyName\":\"Northwind Books\",\"merchantAdminUserId\":\"42\"}"));

		ResponseEntity<String> response = dashboardRestConsumer.getMerchantContext("42");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("northwind-books");

		server.verify();
	}

	@Test
	void getMerchantContexts_targetsGatewayMerchantContextListRoute() {
		server.expect(requestTo("http://localhost:8081/api/v1/merchant-context/list"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(DashboardRestConsumer.AUTH_USER_ID_HEADER, "42"))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON)
						.body("[{\"tenantId\":\"8\",\"tenantKey\":\"northwind-books\",\"companyName\":\"Northwind Books\",\"merchantAdminUserId\":\"42\"}]"));

		ResponseEntity<String> response = dashboardRestConsumer.getMerchantContexts("42");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("northwind-books");

		server.verify();
	}
}