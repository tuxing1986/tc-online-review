package com.appirio.tech.core.service.identity.util.zendesk;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Test;

import com.appirio.tech.core.api.v3.util.jwt.JWTToken;
import com.appirio.tech.core.service.identity.representation.Authorization;
import com.auth0.jwt.JWTVerifier;


public class ZendeskAuthPluginTest {

	@Test
	public void testProcess() throws Exception {
		
		String userId = "JWT-USER-ID";
		String name   = "JWT-NAME";
		String email  = "JWT-EMAIL";

		String idPrefix = "ID-PREFIX";
		String secret = "SECRET";
		
		String token = createToken(userId, name, email, secret);
		
		Authorization auth = new Authorization();
		auth.setToken(token);
	
		// testee
		ZendeskAuthPlugin testee = spy(new ZendeskAuthPlugin());
		testee.setSecret(secret);
		testee.setIdPrefix(idPrefix);
		
		// test
		testee.process(auth);
		
		// check result
		assertEquals(token, auth.getToken());
		
		String zendeskJwt = auth.getZendeskJwt();
		assertNotNull(zendeskJwt);
		
		Map<String, Object> contents = new JWTVerifier(secret).verify(zendeskJwt); // should not cause any error.
		
		assertEquals(testee.createExternalId(userId), contents.get("external_id"));
		assertEquals(name, contents.get("name"));
		assertEquals(email, contents.get("email"));
		assertNotNull(contents.get("iat"));
		assertNotNull(contents.get("jti"));
		
	}
	
	@Test
	public void testProcess_ZendeskTokenIsNotCreatedWhenNameIsNullInSourceJWT() throws Exception {
		
		String userId = "JWT-USER-ID";
		String email  = "JWT-EMAIL";
		
		// test: name is null
		testProcess_CaseThatZendeskTokenIsNotCreated(userId, null, email);
	}
	
	@Test
	public void testProcess_ZendeskTokenIsNotCreatedWhenEmailIsNullInSourceJWT() throws Exception {
		
		String userId = "JWT-USER-ID";
		String name  = "JWT-NAME";
		
		// test: name is null
		testProcess_CaseThatZendeskTokenIsNotCreated(userId, name, null);
	}

	@Test
	public void testProcess_ZendeskTokenIsNotCreatedWhenUserIdIsNullInSourceJWT() throws Exception {
		
		String email  = "JWT-EMAIL";
		String name  = "JWT-NAME";
		
		// test: name is null
		testProcess_CaseThatZendeskTokenIsNotCreated(null, name, email);
	}

	
	protected void testProcess_CaseThatZendeskTokenIsNotCreated(String userId, String name, String email) throws Exception {
		
		String idPrefix = "ID-PREFIX";
		String secret = "SECRET";
		
		String token = createToken(userId, name, email, secret);
		
		Authorization auth = new Authorization();
		auth.setToken(token);
	
		// testee
		ZendeskAuthPlugin testee = spy(new ZendeskAuthPlugin());
		testee.setSecret(secret);
		testee.setIdPrefix(idPrefix);
		
		// test
		testee.process(auth);
		
		// check result
		assertEquals(token, auth.getToken());
		
		assertNull(auth.getZendeskJwt());
	}
	
	
	@Test
	public void testCreateExternalId() throws Exception {
		
		String userId = "JWT-USER-ID";
		String idPrefix = "ID-PREFIX";
		
		// testee
		ZendeskAuthPlugin testee = spy(new ZendeskAuthPlugin());
		testee.setIdPrefix(idPrefix);
		
		// test
		String result = testee.createExternalId(userId);
		
		// check result
		assertNotNull(result);
		assertEquals(idPrefix+":"+userId, result);
	}
	
	@Test
	public void testDecorateForTest_Prod() throws Exception {
		
		String name = "JWT-NAME";
		String idPrefix = "ID-PREFIX";
		
		// testee
		ZendeskAuthPlugin testee = spy(new ZendeskAuthPlugin());
		testee.setIdPrefix(idPrefix);
		
		// test
		String result = testee.decorateForTest(name);
		
		// check result
		assertNotNull(result);
		assertEquals(name, result);
	}
	
	@Test
	public void testDecorateForTest_Dev() throws Exception {
		
		String name = "JWT-NAME";
		String idPrefix = "ID-PREFIX-Dev";
		
		// testee
		ZendeskAuthPlugin testee = spy(new ZendeskAuthPlugin());
		testee.setIdPrefix(idPrefix);
		
		// test
		String result = testee.decorateForTest(name);
		
		// check result
		assertNotNull(result);
		assertEquals(name+"."+idPrefix, result);
	}
	
	@Test
	public void testDecorateForTest_QA() throws Exception {
		
		String name = "JWT-NAME";
		String idPrefix = "ID-PREFIX-QA";
		
		// testee
		ZendeskAuthPlugin testee = spy(new ZendeskAuthPlugin());
		testee.setIdPrefix(idPrefix);
		
		// test
		String result = testee.decorateForTest(name);
		
		// check result
		assertNotNull(result);
		assertEquals(name+"."+idPrefix, result);
	}

	protected String createToken(String userId, String name, String email, String secret) {
		JWTToken jwt = new JWTToken();
		jwt.setHandle(name);
		jwt.setEmail(email);
		jwt.setUserId(userId);
		String token = jwt.generateToken(secret);
		return token;
	}

}
