/**
 * 
 */
package com.appirio.tech.core.service.identity.ctrl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Credential returned from Auth0 auth. with "scope=openid, offline_access"
 * 
 * @author sudo
 *
 */
public class Auth0Credential {
	@JsonProperty(value="id_token")
	private String idToken;
	@JsonProperty(value="access_token")
	private String accessToken;
	@JsonProperty(value="refresh_token")
	private String refreshToken;
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
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
}
