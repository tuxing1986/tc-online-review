/**
 * 
 */
package com.appirio.tech.core.service.identity.view;

import io.dropwizard.views.View;

/**
 * @author sudo
 *
 */
public class CallbackView extends View {
	private static final String templateName = "callback.ftl";

	private String jwtToken;
	private boolean debug;
	private String redirectUrl;
	
	public CallbackView(String jwtToken, boolean debug, String redirectUrl){
		super(templateName);
		this.jwtToken = jwtToken;
		this.debug = debug;
		this.redirectUrl = redirectUrl;
	}

	public static String getTemplatename() {
		return templateName;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public boolean isDebug() {
		return debug;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

}
