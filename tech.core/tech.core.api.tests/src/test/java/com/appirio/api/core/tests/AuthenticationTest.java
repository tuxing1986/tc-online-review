package com.appirio.api.core.tests;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.appirio.automation.api.DefaultResponse;
import com.appirio.automation.api.config.AuthenticationConfiguration;
import com.appirio.automation.api.config.EnvironmentConfiguration;
import com.appirio.automation.api.exception.DefaultRequestProcessorException;
import com.appirio.automation.api.model.Auth0Info;
import com.appirio.automation.api.model.AuthenticationInfo;
import com.appirio.automation.api.service.AuthenticationService;


public class AuthenticationTest {
	private AuthenticationService service = null;

	@BeforeClass
	public void setUp() {
		
		EnvironmentConfiguration.initialize();
		AuthenticationConfiguration.initialize();
		service = new AuthenticationService();	
	}


	/**
	 * Access authorizations api end point from Auth0’s login code obtained after 
	 * OAuth2 or OpenID flow. Payload is ignored
	 * @throws Exception
	 */
	// @todo anjali - this has some issues, discussed with kohata, ignoring it
	// for now.
	@Test
	public void testAuthorization_auth0Code() throws Exception {
		Auth0Info authInfo = service.getAuth0Info();
		//Assert.assertNotNull(accessToken);
	}


	/**
	 * Access authorizations api end point from JWT token obtained from Auth0.
	 * Payload should contain a jwt token and a refresh token[optional] gained from Auth0.
	 *  @throws Exception
	 */
	@Test
	public void testAuthorization_jwtToken() throws Exception {
		AuthenticationInfo info = service.authenticate();
		Assert.assertNotNull(info.getJwtToken());
	}


	/**
	 * Access authorizations api end point by passing Client-ID and Secret. Client should obtain the 
	 * client ID and secret from Identity service in advance
	 * @throws Exception
	 */
	@Test
	public void testAuthorization_clientId() throws Exception {
		AuthenticationInfo info = service.authenticate(AuthenticationConfiguration.getAuthClientIdMap());
		Assert.assertNotNull(info.getJwtToken());

	}

	/**
	 * Access authorizations api end point by passing Client-ID and Secret. Client should obtain the 
	 * client ID and secret from Identity service in advance
	 * @throws Exception
	 */
	@Test(expectedExceptions=DefaultRequestProcessorException.class)
	public void testAuthorization_clientId_Empty() throws Exception {
		service.authenticate(new HashMap<String, String>());
	}

	/**
	 * Access authorizations api end point by passing Client-ID and Secret. Client should obtain the 
	 * client ID and secret from Identity service in advance
	 * @throws Exception
	 */
	@Test(expectedExceptions=DefaultRequestProcessorException.class)
	public void testAuthorization_clientId_Invalid() throws Exception {
		String invalidClientIdSecret = "211132311##";
		Map<String, String> invalidParams = new HashMap<String, String>();
		invalidParams.put("clientId", invalidClientIdSecret);
		invalidParams.put("secret", invalidClientIdSecret);

		service.authenticate(invalidParams);
		//Assert.assertNull(info);

	}
	/**
	 * Deletes user’s every access token and invokes JWT. If {id} is specified, 
	 * Logout from specific SSO system only. Payload is ignored.
	 * @throws Exception
	 */
	@Test
	public void testAuthorization_deleteJWTToken() throws Exception {
		AuthenticationInfo info = service.authenticate();
		DefaultResponse response = service.logout(info);
		Assert.assertEquals(response.getCode(), 200);

	}

	/**
	 * Deletes user’s every access token and invokes JWT. If {id} is specified, 
	 * Logout from specific SSO system only. Payload is ignored.
	 * @throws Exception
	 */
	@Test
	public void testAuthorization_Id_deleteJWTToken() throws Exception {
		//String auth0JWTToken = service.getAuth0JWTToken();
		AuthenticationInfo info = service.authenticate();
		String id = "/1";
		DefaultResponse response = service.logout(info, id);
		Assert.assertEquals(response.getCode(), 200);

	}

	/**
	 * Re-issues jwt token and returns Authentication object with it.
	 * For expired jwt token, the server will look for user’s access token and create new jwt.
	 * If access token itself is removed, returns 403 Forbidden.
	 * Payload is ignored.
	 * Note 1: This call can only be made within our new microservices by whitelisting servers 
	 * (internal subnet), until clientid/secret flow is implemented.
	 * @throws Exception
	 */
	@Test
	public void testReissueJWTToken() throws Exception {
		AuthenticationInfo info = service.authenticate();
		String id = "/1";
		info = service.reAuthenticate(info, id);

	}
}