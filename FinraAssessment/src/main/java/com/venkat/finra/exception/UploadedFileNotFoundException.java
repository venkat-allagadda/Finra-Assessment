package com.venkat.finra.exception;

public class UploadedFileNotFoundException extends FileUploadException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadedFileNotFoundException(String message) {
		super(message);
	}

	public UploadedFileNotFoundException(Throwable cause) {
		super(cause);
	}

	public UploadedFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
