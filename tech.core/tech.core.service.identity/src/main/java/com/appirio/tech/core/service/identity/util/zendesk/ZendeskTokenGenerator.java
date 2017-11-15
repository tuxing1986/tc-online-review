package com.appirio.tech.core.service.identity.util.zendesk;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTSigner.Options;

public class ZendeskTokenGenerator {

	private String secret;

	public ZendeskTokenGenerator() {
	}

	public ZendeskTokenGenerator(String secret) {
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public String generateToken(String userId, String name, String email) {
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("email", email);
		claims.put("name", name);
		claims.put("external_id", userId);
		
		Options options = new Options();
		options.setAlgorithm(Algorithm.HS256);
		options.setIssuedAt(true); // auto
		options.setJwtId(true); // auto
		
		JWTSigner signer = new JWTSigner(getSecret());
		return signer.sign(claims, options);
	}
}
