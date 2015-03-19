package com.appirio.tech.core.service.identity.representation;

import static com.appirio.tech.core.service.identity.util.Constants.*;
import java.util.regex.Matcher;

import com.appirio.tech.core.api.v3.model.AbstractIdResource;
import com.appirio.tech.core.service.identity.util.Utils;

public class User extends AbstractIdResource {

	private String handle;
	private String email;
	private String firstName;
	private String lastName;
	private Credential credential;
	private String status;
	
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
		return INTERNAL_STATUS_ACTIVE.equals(this.status);
	}
	
	public void setActive(Boolean active) {
		this.status = (active!=null && active) ? INTERNAL_STATUS_ACTIVE : INTERNAL_STATUS_INACTIVE;
	}
	
	// Internal representation of user's status (Active or not)
	public static final String INTERNAL_STATUS_ACTIVE = "A";
	public static final String INTERNAL_STATUS_INACTIVE = "U";
	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Credential getCredential() {
		return credential;
	}
	
	public void setCredential(Credential credential) {
		this.credential = credential;
	}
	
	/* validation logics will be moved to Validation framework */
	
	public String validate() {
		String result = validateFirstName();
		if(result!=null) return result;
		
		result = validateLastName();
		if(result!=null) return result;
		
		result = validatePassoword();
		if(result!=null) return result;

		result = validateHandle();
		if(result!=null) return result;
		
		result = validateEmail();
		if(result!=null) return result;
		
		return result;
	}


	//
	/**
	 * do validation check on handle
	 * @return error message or null for valid status
	 */
	public String validateHandle() {
		// Mandatory
		if (this.handle==null || this.handle.length()==0)
			return String.format(MSG_TEMPLATE_MANDATORY, "Handle");
		
		// Range check
	    if (this.handle.length() < MIN_LENGTH_HANDLE || this.handle.length() > MAX_LENGTH_HANDLE) {
	    	return String.format(MSG_TEMPLATE_INVALID_MINMAX_LENGTH, "handle", MIN_LENGTH_HANDLE, MAX_LENGTH_HANDLE);
	    }
		if (this.handle.contains(" ")) {
			return MSG_TEMPLATE_INVALID_HANDLE_CONTAINS_SPACE;
		}
		if (!Utils.containsOnly(handle, HANDLE_ALPHABET, false)) {
			return MSG_TEMPLATE_INVALID_HANDLE_CONTAINS_FORBIDDEN_CHARS;
		}
		if (Utils.containsOnly(handle, HANDLE_PUNCTUATION, false)) {
			return MSG_TEMPLATE_INVALID_HANDLE_CONTAINS_ONLY_PUNCTUATION;
		}
		if (handle.toLowerCase().trim().startsWith("admin")) {
			return MSG_TEMPLATE_INVALID_HANDLE_STARTS_WITH_ADMIN;
		}
		/*
		if (Utils.checkInvalidHandle(handle)) {
			return "The handle you entered is not valid.";
		}
		*/
		return null;
	}
	
	public String validateEmail() {
		// Mandatory
		if (this.email==null || this.email.length()==0)
			return String.format(MSG_TEMPLATE_MANDATORY, "Email address");

		// Range check
	    if (this.email.length() > MAX_LENGTH_EMAIL) {
	    	return String.format(MSG_TEMPLATE_INVALID_MAX_LENGTH, "email address", MAX_LENGTH_EMAIL);
	    }
	    
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            return MSG_TEMPLATE_INVALID_EMAIL;
        }
        return null;
	}

	public String validateFirstName() {
		// Not mandatory
		if (this.firstName==null || this.firstName.length()==0)
			return null;
		// Range check
	    if (this.firstName.length() > MAX_LENGTH_FIRST_NAME) {
	    	return String.format(MSG_TEMPLATE_INVALID_MAX_LENGTH, "first name", MAX_LENGTH_FIRST_NAME);
	    }
		return null;
	}
	
	public String validateLastName() {
		// Not mandatory
		if (this.lastName==null || this.lastName.length()==0)
			return null;
		// Range check
	    if (this.lastName.length() > MAX_LENGTH_LAST_NAME) {
	    	return String.format(MSG_TEMPLATE_INVALID_MAX_LENGTH, "last name", MAX_LENGTH_LAST_NAME);
	    }
		return null;
	}
	
	protected String validateName(String fieldName, String nameValue, int maxlength) {
		// Not mandatory
		if (nameValue==null || nameValue.length()==0)
			return null;
		// Range check
	    if (nameValue.length() > maxlength) {
	    	return String.format(MSG_TEMPLATE_INVALID_MAX_LENGTH, fieldName, maxlength);
	    }
		return null;
	}
	
	public String validatePassoword() {
		// Mandatory
		if (this.credential==null || this.credential.getPassword()==null || this.credential.getPassword().length()==0)
			return String.format(MSG_TEMPLATE_MANDATORY, "Password");
		
		// Range check
		String password = this.credential.getPassword();
	    if (password.length() < MIN_LENGTH_PASSWORD || password.length() > MAX_LENGTH_PASSWORD) {
	    	return String.format(MSG_TEMPLATE_INVALID_MINMAX_LENGTH, "passowrd", MIN_LENGTH_PASSWORD, MAX_LENGTH_PASSWORD);
	    }
        // length OK, check password strength.
        int strength = Utils.calculatePasswordStrength(password);
        switch (strength) {
        case 0:
        case 1:
        case 2:
        	return MSG_TEMPLATE_INVALID_PASSWORD;
        default:
            break;
        }

		return null;
	}

}
