/**
 * 
 */
package com.appirio.tech.core.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test cases to test authentication
 * 
 * @author sudo
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/dispatcher-servlet.xml" })
public class AuthenticationEndpointTest {

	@Autowired
	protected WebApplicationContext wac;
	@Autowired
	private FilterChainProxy springSecurityFilterChain;
	protected MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = webAppContextSetup(this.wac).addFilters(this.springSecurityFilterChain).build();
	}

	@Test
	public void callWithoutTokenResponds403() throws Exception {
		mockMvc.perform(get("/api/v3/"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void callWithInValidTokenResponds403() throws Exception {
		mockMvc.perform(get("/api/v3/").header("Authentication", "Bearer invalid_token"))
				.andExpect(status().isForbidden());
	}
}
