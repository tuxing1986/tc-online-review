package com.appirio.tech.core.sample.exception;

import com.appirio.tech.core.api.v3.exception.APIRuntimeException;

@SuppressWarnings("serial")
public class StorageException extends APIRuntimeException {

	public StorageException() {
	}

	public StorageException(String arg0) {
		super(arg0);
	}

	public StorageException(Throwable arg0) {
		super(arg0);
	}

	public StorageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
