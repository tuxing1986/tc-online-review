package com.appirio.tech.core.sample.representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShiroRepresentation {
	private String iniConfig;
	private String userName;
	private boolean isPermitted;
	
	public ShiroRepresentation(String iniConfig, String userName, boolean isPermitted) {
		this.iniConfig = iniConfig;
		this.userName = userName;
		this.isPermitted = isPermitted;
	}
	
	@JsonProperty
	public String getIniConfig() {
		return iniConfig;
	}

	public String getUserName() {
		return userName;
	}

	public boolean getIsPermitted() {
		return isPermitted;
	}
}
