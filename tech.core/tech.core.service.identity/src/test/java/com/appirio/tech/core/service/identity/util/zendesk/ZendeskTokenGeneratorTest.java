package com.appirio.tech.core.service.identity.util.zendesk;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.auth0.jwt.JWTVerifier;

public class ZendeskTokenGeneratorTest {

	@Test
	public void testGenerateToken() throws Exception {
		
		String userId = "JWT-USER-ID";
		String name   = "JWT-NAME";
		String email  = "JWT-EMAIL";
		String secret = "SECRET";

		// testee
		ZendeskTokenGenerator gen = new ZendeskTokenGenerator(secret);
		
		// test
		String result = gen.generateToken(userId, name, email);
		
		// check result
		assertNotNull(result);
		
		Map<String, Object> contents = new JWTVerifier(secret).verify(result); // should not cause any error.
		
		assertEquals(userId, contents.get("external_id"));
		assertEquals(name, contents.get("name"));
		assertEquals(email, contents.get("email"));
		assertNotNull(contents.get("iat"));
		assertNotNull(contents.get("jti"));
	}

}
