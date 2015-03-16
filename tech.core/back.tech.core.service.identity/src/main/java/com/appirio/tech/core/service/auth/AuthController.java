/**
 * 
 */
package com.appirio.tech.core.service.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Auth server Controller to handle login/logout and redirect to other application(micro services).
 * 
 * @author sudo
 *
 */
@RequestMapping(value="/pub")
@Controller
public class AuthController implements InitializingBean {

	protected final Logger logger = Logger.getLogger(getClass());
	
	@Value("${auth0.clientId}")
	private String clientId;
	
	@Value("${auth0.domain}")
	private String clientDomain;
	
	@Value("${auth0.clientSecret}")
	private String clientSecret;

	@RequestMapping(value="/login")
	public ModelAndView auth(
			@RequestParam(value="retUrl") String retUrl,
			@RequestParam(value="setParam", defaultValue="false") Boolean setParam,
			@RequestParam(value="debug", defaultValue="false") Boolean debug,
			HttpServletRequest request) throws UnsupportedEncodingException {
		
		//Security TODO: check retUrl is on the same domain.
		String state = "retUrl=" + retUrl 
				+ "&setParam=" + setParam
				+ "&debug=" + debug;
		
		ModelAndView modelAndView = new ModelAndView("login");
		modelAndView.addObject("clientId", clientId);
		modelAndView.addObject("clientDomain", clientDomain);
		//http://localhost:8080/tech.core.sample.auth/callback
		String url = request.getRequestURL().toString();
		modelAndView.addObject("callbackUrl", url.substring(0, url.length()-5) + "callback");
		modelAndView.addObject("auth0_state", URLEncoder.encode(state, "UTF-8"));
		modelAndView.addObject("debug", debug);
		
		return modelAndView;
	}

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
	@RequestMapping(value="/callback")
	public ModelAndView auth0Callback(
			@RequestParam(value="state", required=true) String state,	//Value that survives redirects
			@RequestParam(value="code", required=false) String code,	//Authorization code from Auth0
			@RequestParam(value="access_token", required=false) String accessToken,
			@RequestParam(value="id_token", required=false) String idToken,
			@RequestParam(value="token_type", required=false) String tokenType,
			HttpServletRequest request,
			HttpServletResponse resp) throws Exception {
		
		Auth0Credential credential;
		if(code!=null) {
			credential = getAuth0AccessTokenFromCode(code, request.getRequestURL().toString());
		} else {
			credential = new Auth0Credential();
			credential.setAccessToken(accessToken);
			credential.setIdToken(idToken);
			credential.setTokenType(tokenType);
		}
		ModelAndView modelAndView = new ModelAndView("redirect");
		modelAndView.addObject("auth0_access_token", credential.getAccessToken());
		modelAndView.addObject("auth0_id_token", credential.getIdToken());
		modelAndView.addObject("auth0_token_type", credential.getTokenType());
		
		//parse the state to obtain redirectUrl and debug
		String[] stateArray = URLDecoder.decode(state, "UTF-8").split("&");
		Map<String, String> valMap = new HashMap<String, String>();
		for(String stateString : stateArray) {
			valMap.put(stateString.split("=")[0], stateString.split("=")[1]);
		}
		String redirectUrl = valMap.get("retUrl");
		if(valMap.get("setParam").equalsIgnoreCase("true")) {
			redirectUrl += "?userJWTToken=" + credential.getIdToken();
		}
		modelAndView.addObject("redirectUrl", redirectUrl);
		if(valMap.get("debug").equalsIgnoreCase("true")) {
			modelAndView.addObject("debug", true);
		}
		
		return modelAndView;
	}

	@RequestMapping(value="/decode")
	@ResponseBody
	public Map<String, Object> decodeJwt(
			@RequestParam(value="token", required=false) String token) throws Exception {
		return new JWTVerifier(clientSecret, clientId).verify(token);
	}
	
	/**
	 * Obtain access_token of Auth0 for login user doing POST request to Auth0's oauth endpoint.
	 * 
	 * @param code
	 * 			the code returned from Auth0 during oauth handshake.
	 * @param redirectUrl
	 * 			redirectUrl to specify in Auth0. This must match the callback URL set in Auth0.
	 * @return
	 */
	private Auth0Credential getAuth0AccessTokenFromCode(String code, String redirectUrl) {
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

	@Override
	public void afterPropertiesSet() throws Exception {
		if(clientId==null || clientDomain==null || clientSecret==null) {
			throw new RuntimeException("Auth0 properties not set. Check auth0.properties file on classpath");
		}
	}
}
