package com.appirio.tech.core.service.identity.representation;

import com.appirio.tech.core.api.v3.model.AbstractIdResource;
import com.appirio.tech.core.service.identity.util.Utils;

public class Credential extends AbstractIdResource {

	private String password;
	
	private String activationCode;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	// TODO:
	public String encodePassword() {
		if(this.password==null)
			return null;
		return Utils.encodePassword(this.password, "users");
	}
}
