/**
 * 
 */
package com.appirio.tech.core.service.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Credential returned from Auth0 auth. (scope=openid)
 * 
 * @author sudo
 *
 */
public class Auth0Credential {
	@JsonProperty(value="id_token")
	private String idToken;
	@JsonProperty(value="access_token")
	private String accessToken;
	@JsonProperty(value="token_type")
	private String tokenType;
	
	public String getIdToken() {
		return idToken;
	}
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
}
