package com.appirio.tech.sample.exception;

import com.appirio.tech.core.api.v3.exception.CMCRuntimeException;

@SuppressWarnings("serial")
public class StorageException extends CMCRuntimeException {

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
