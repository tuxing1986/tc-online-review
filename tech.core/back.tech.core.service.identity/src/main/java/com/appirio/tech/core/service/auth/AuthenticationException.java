package com.appirio.tech.core.service.auth;

import com.appirio.tech.core.api.v3.exception.CMCRuntimeException;

@SuppressWarnings("serial")
public class AuthenticationException extends CMCRuntimeException {

	public AuthenticationException() {
	}

	public AuthenticationException(String arg0) {
		super(arg0);
	}

	public AuthenticationException(Throwable arg0) {
		super(arg0);
	}

	public AuthenticationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
