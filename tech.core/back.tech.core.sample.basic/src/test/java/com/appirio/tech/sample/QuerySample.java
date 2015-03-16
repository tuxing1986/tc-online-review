/**
 * 
 */
package com.appirio.tech.sample;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.appirio.tech.sample.storage.InMemoryStorage;

/**
 * @author sudo
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/dispatcher-servlet.xml" })
public class QuerySample {

	@Autowired
	protected WebApplicationContext webApplicationContext;
	protected MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
	}
	
	@Test
	public void testUserQuery() throws Exception {
		mockMvc.perform(get("/api/v2/users?fields=id,handle&filter=" + URLEncoder.encode("handle=sudo0124", "UTF-8")))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	public void testUserLimitQuery() throws Exception {
		mockMvc.perform(get("/api/v2/users?fields=id,handle&limit=3&offset=2&orderBy=handle"))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	public void testUserMetadata() throws Exception {
		mockMvc.perform(get("/api/v2/users?metadata=true&limit=0"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("result.metadata.fields").exists())
				.andExpect(jsonPath("result.metadata.totalCount").value(InMemoryStorage.instance().getUserList().size()))
				.andDo(print());
	}
}
