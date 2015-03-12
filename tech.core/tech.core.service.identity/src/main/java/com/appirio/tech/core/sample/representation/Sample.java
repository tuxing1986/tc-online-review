/**
 * 
 */
package com.appirio.tech.core.sample.representation;

import com.appirio.tech.core.api.v3.model.AbstractIdResource;

/**
 * @author sudo
 *
 */
public class Sample extends AbstractIdResource {

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
}
