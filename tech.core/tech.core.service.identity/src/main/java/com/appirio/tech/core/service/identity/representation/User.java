package com.appirio.tech.core.service.identity.representation;

import com.appirio.tech.core.api.v3.model.AbstractIdResource;

public class User extends AbstractIdResource {

	private String handle;
	private String email;
	private String firstName;
	private String lastName;
	private Credential credential;
	private Boolean active = false;
	
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	// Internal representation user's status (Active or not)
	public static final String INTERNAL_STATUS_ACTIVE = "A";
	public static final String INTERNAL_STATUS_INACTIVE = "U";
	public String getStatus() {
		return isActive() ? INTERNAL_STATUS_ACTIVE : INTERNAL_STATUS_INACTIVE;
	}
	public void setStatus(String status) {
		if(status==null || status.trim().length()==0)
			return;
		setActive(INTERNAL_STATUS_ACTIVE.equals(status));
	}
	public Credential getCredential() {
		return credential;
	}
	public void setCredential(Credential credential) {
		this.credential = credential;
	}
}
