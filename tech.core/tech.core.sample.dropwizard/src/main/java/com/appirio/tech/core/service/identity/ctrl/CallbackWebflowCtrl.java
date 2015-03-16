/**
 * 
 */
package com.appirio.tech.core.service.identity.ctrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appirio.tech.core.auth.JWTAuthenticator;
import com.appirio.tech.core.service.identity.exception.AuthenticationException;
import com.appirio.tech.core.service.identity.view.CallbackView;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.auth0.jwt.JWTSigner.Options;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Temporary Login Resource for DW for secured Login Webflow of Auth0.
 * TODO: We should move this to pure RESTful if we can.
 * 
 * @author sudo
 *
 */
@Path("/callback")
@Produces(MediaType.TEXT_HTML)
public class CallbackWebflowCtrl {

	private static final Logger logger = LoggerFactory.getLogger(CallbackWebflowCtrl.class);

	private String clientDomain = "topcoder-dev.auth0.com";
	private String clientId		= "JFDo7HMkf0q2CkVFHojy3zHWafziprhT";
	private String clientSecret = "0fjm47MSE1ea18WRPX9v3K6EM3iI8dc0OF5VNc-NMTNWEiwBwsmfjEYqOBW9HLhY";
	private boolean debug = false; //a flag to debug Auth0 flow

	/**
	 * Callback from Auth0 service.
	 * see: https://docs.auth0.com/oauth-web-protocol
	 * 
	 * @param code
	 * @param state
	 * 		JSON format of CallbackState
	 * @param request
	 * @param resp
	 * @return
	 */
	@GET
	public CallbackView getCallbackPage(
			@QueryParam("state") String state,	//Value that survives redirects
			@QueryParam("code") String code,	//Authorization code from Auth0
			@Context HttpServletRequest request,
			@Context HttpServletResponse resp) throws Exception {
		
		Auth0Credential credential;
		if(code!=null) {
			credential = getAuth0AccessTokenFromCode(code, request.getRequestURL().toString());
		} else {
			throw new AuthenticationException("Failed to obtain valid code from Auth0");
		}
		String jwt_token = createJWTToken(credential.getIdToken());
		
		//parse the state to obtain redirectUrl and debug
		String[] stateArray = URLDecoder.decode(state, "UTF-8").split("&");
		Map<String, String> valMap = new HashMap<String, String>();
		for(String stateString : stateArray) {
			valMap.put(stateString.split("=")[0], stateString.split("=")[1]);
		}
		String redirectUrl = valMap.get("retUrl");
		if(valMap.get("setParam").equalsIgnoreCase("true")) {
			redirectUrl += "?userJWTToken=" + jwt_token;
		}
		if(valMap.get("debug").equalsIgnoreCase("true")) {
			debug = true;
		}
		
		return new CallbackView(jwt_token, debug, redirectUrl);
	}

	private String createJWTToken(String auth0Token) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, SignatureException, IOException, JWTVerifyException {
		JWTVerifier verifier = new JWTVerifier(Base64.decodeBase64(clientSecret));
		Map<String, Object> map = verifier.verify(auth0Token);
		logger.debug("obtained auth0 token:" + map);
		String userId = map.get("user_id").toString();

		Map<String, Object> claims = new HashMap<String, Object>();
		String[] parts = userId.split("\\|");
		claims.put(JWTAuthenticator.JWT_USER_ID, parts[parts.length-1]);
		claims.put("iss", "appirio:v3:150301");
		Options options = new Options();
		options.setExpirySeconds(60*10); //10 min.
		options.setIssuedAt(true);
		options.setJwtId(true);
		
		JWTSigner signer = new JWTSigner(clientSecret);
		return signer.sign(claims, options);
	}

	/**
	 * Obtain access_token of Auth0 for login user doing POST request to Auth0's oauth endpoint.
	 * 
	 * @param code
	 * 			the code returned from Auth0 during oauth handshake.
	 * @param redirectUrl
	 * 			redirectUrl to specify in Auth0. This must match the callback URL set in Auth0.
	 * @return
	 * @throws AuthenticationException 
	 */
	private Auth0Credential getAuth0AccessTokenFromCode(String code, String redirectUrl) throws AuthenticationException {
		try {
			String postResponseStr = new String();
			URL url = new URL("https://"+clientDomain+"/oauth/token");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			
			String postParameter = "client_id=" + clientId
					+ "&redirect_uri=" + redirectUrl
					+ "&client_secret=" + clientSecret
					+ "&code=" + code
					+ "&grant_type=authorization_code"
					+ "&scope=openid";
			
			PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
			printWriter.print(postParameter);
			printWriter.close();
	
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF8"));
			String str;
			while ((str = bufferReader.readLine()) != null) {
				postResponseStr = postResponseStr + str;
			}
			bufferReader.close();
			connection.disconnect();
			
			logger.debug("successfully obtained credential from auth0:" + postResponseStr);
			Auth0Credential credential = new ObjectMapper().readValue(postResponseStr, Auth0Credential.class);
			
			return credential;
		} catch (Exception e) {
			throw new AuthenticationException("Can not obtain access_token from auth0 service.", e);
		}
	}
}
