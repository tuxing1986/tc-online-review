package com.appirio.tech.core.api.security.auth0;

import org.springframework.security.core.AuthenticationException;


/**
 *
 */
@SuppressWarnings("serial")
public class Auth0AuthenticationException extends AuthenticationException {

	public Auth0AuthenticationException(String msg) {
		super(msg);
	}

	public Auth0AuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

	public Auth0AuthenticationException(Exception e) {
		super(e.getMessage(), e);
	}

}
