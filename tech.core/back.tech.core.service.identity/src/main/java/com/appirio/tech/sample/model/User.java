/**
 * 
 */
package com.appirio.tech.sample.model;

import org.springframework.stereotype.Component;

import com.appirio.tech.core.api.v3.model.AbstractIdResource;
import com.appirio.tech.core.api.v3.model.annotation.ApiMapping;

/**
 * @author sudo
 *
 */
@Component
public class User extends AbstractIdResource {
	
	public static final String RESOURCE_PATH = "users";

	private String handle;
	private String email;
	private String firstName;
	private String lastName;
	
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

	@ApiMapping(queryDefault=false)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@ApiMapping(queryDefault=false)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@ApiMapping(visible=false)
	public String getResourcePath() {
		return RESOURCE_PATH;
	}

}
