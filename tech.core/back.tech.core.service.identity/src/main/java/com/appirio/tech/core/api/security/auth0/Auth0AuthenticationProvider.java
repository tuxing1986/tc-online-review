package com.appirio.tech.core.api.security.auth0;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.auth0.jwt.JWTVerifier;

/**
 * Implementation of {@link AuthenticationProvider} that sets {@link UserDetails}
 * verifying auth0's bear JWT token.
 */
public class Auth0AuthenticationProvider implements AuthenticationProvider, InitializingBean {

	protected final Logger logger = Logger.getLogger(getClass());
	
	private JWTVerifier jwtVerifier = null;
	private String clientSecret = null;
	private String clientId = null;

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String token = ((Auth0JWTToken) authentication).getJwt();
		Map<String, Object> decoded;
		try {
			decoded = jwtVerifier.verify(token);
			logger.debug("Decoded JWT token" + decoded);
			
			((Auth0JWTToken) authentication).setAuthenticated(true);
			((Auth0JWTToken) authentication).setPrincipal(new Auth0UserDetails(decoded));
			((Auth0JWTToken) authentication).setDetails(decoded);
			return authentication;
		} catch (Exception e) {
			logger.debug("Error occured while decoding JWT token " + e.getLocalizedMessage());
			throw new Auth0AuthenticationException("Authentication error occured");
		} 
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return Auth0JWTToken.class.isAssignableFrom(authentication);
	}

	@Override /* InitializingBean */
	public void afterPropertiesSet() throws Exception {
		if ((clientSecret == null) || (clientId == null)) {
			throw new RuntimeException("clientId or clientSecret not set. Verify auth0.properties file is in root classpath.");
		}
		jwtVerifier = new JWTVerifier(clientSecret, clientId);
	}
}
