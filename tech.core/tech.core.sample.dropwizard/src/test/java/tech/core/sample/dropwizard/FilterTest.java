package tech.core.sample.dropwizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.dropwizard.testing.junit.DropwizardAppRule;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;

import com.appirio.tech.core.api.v3.dropwizard.APIBaseConfiguration;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.appirio.tech.core.api.v3.util.jwt.JWTToken;
import com.appirio.tech.core.auth.JWTAuthProvider;
import com.appirio.tech.core.sample.app.SampleApplication;
import com.appirio.tech.core.sample.representation.Sample;
import com.appirio.tech.core.sample.storage.InMemoryUserStorage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class FilterTest {

	@ClassRule
	public static final DropwizardAppRule<APIBaseConfiguration> RULE = new DropwizardAppRule<APIBaseConfiguration>(
			SampleApplication.class, "src/test/resources/config.yml");

	@Test
	public void testOrderBy() throws Exception {
		//Assert that list is ordered by email
		List<Sample> samples = makeSampleCall("orderBy=email");
		for(int i=1; i<samples.size(); i++) {
			String current = samples.get(i).getEmail();
			String previous = samples.get(i-1).getEmail();
			assertTrue(current.compareTo(previous) > 0);
		}

		//Assert that list is ordered by email
		samples = makeSampleCall("orderBy=handle");
		for(int i=1; i<samples.size(); i++) {
			String current = samples.get(i).getHandle();
			String previous = samples.get(i-1).getHandle();
			assertTrue(current.compareTo(previous) > 0);
		}

		//Assert that list is desc ordered
		samples = makeSampleCall("orderBy=" + URLEncoder.encode("handle desc", "UTF-8"));
		for(int i=1; i<samples.size(); i++) {
			String current = samples.get(i).getHandle();
			String previous = samples.get(i-1).getHandle();
			assertTrue(current.compareTo(previous) < 0);
		}
	}

	@Test
	public void testFilter() throws Exception {
		String filter = InMemoryUserStorage.instance().getUserList().get(1).getHandle();
		List<Sample> samples = makeSampleCall("filter=" + URLEncoder.encode(
				"handle=" + filter, "UTF-8"));
		for(Sample sample : samples) {
			assertEquals(filter, sample.getHandle());
		}
	}
	
	@Test
	public void testLimit() throws Exception {
		List<Sample> samples = makeSampleCall("limit=0");
		assertEquals(samples.size(), 0);

		samples = makeSampleCall("limit=5");
		assertEquals(samples.size(), 5);
	}
	
	@Test
	public void testPartialResponse() throws Exception {
		List<Sample> samples = makeSampleCall("fields=" + URLEncoder.encode("id,handle", "UTF-8"));
		for(Sample sample : samples) {
			assertNotNull(sample.getId());
			assertNotNull(sample.getHandle());
			//other fields should be null
			assertNull(sample.getEmail());
			assertNull(sample.getCreatedAt());
		}
	}
	
	private List<Sample> makeSampleCall(String param) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Client client = new Client();
		ClientResponse response = client.resource(
									String.format("http://localhost:%d/v3/apisamples?%s",
										RULE.getLocalPort(),
										param))
									.header("Authorization", "Bearer " + TestTokenCreator.getJWTToken())
									.get(ClientResponse.class);
		ApiResponse apiResp = response.getEntity(ApiResponse.class);
		return apiResp.getContentResourceList(Sample.class);
	}

	public static class TestTokenCreator {
		public static String getDomain() {
			return RULE.getConfiguration().getAuthDomain();
		}
		public static String getSecret() {
			JWTAuthProvider provider = new JWTAuthProvider(getDomain());
			return provider.getSecret();
		}
		public static String getJWTToken() {
			JWTToken jwt = new JWTToken();
			jwt.setIssuer(jwt.createIssuerFor(getDomain()));
			return jwt.generateToken(getSecret());
		}
	}
	
}
