/**
 * 
 */
package com.appirio.tech.core.service.identity.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import io.dropwizard.views.View;

/**
 * @author sudo
 *
 */
public class LoginView extends View {
	private static final String templateName = "login.ftl";

	private String clientDomain = "topcoder-dev.auth0.com";
	private String clientId		= "JFDo7HMkf0q2CkVFHojy3zHWafziprhT";
	private String callbackUrl;
	private String auth0_state;
	private boolean debug = false;
	
	public LoginView(String retUrl, boolean setParam, boolean debug, HttpServletRequest request) throws UnsupportedEncodingException{
		super(templateName);
		//Security TODO: check retUrl is on the same domain.
		String state = "retUrl=" + retUrl 
				+ "&setParam=" + setParam
				+ "&debug=" + debug;
		auth0_state = URLEncoder.encode(state, "UTF-8");
		String url = request.getRequestURL().toString();
		callbackUrl = url.substring(0, url.length()-"pub/login".length()) + "/v3/callback";
	}

	public String getClientDomain() {
		return clientDomain;
	}

	public String getClientId() {
		return clientId;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public String getAuth0_state() {
		return auth0_state;
	}

	public boolean isDebug() {
		return debug;
	}

}
