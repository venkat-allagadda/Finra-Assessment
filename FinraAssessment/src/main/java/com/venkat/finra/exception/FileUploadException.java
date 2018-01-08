package com.venkat.finra.exception;

public class FileUploadException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileUploadException(String message) {
		super(message);
	}

	public FileUploadException(Throwable cause) {
		super(cause);
	}

	public FileUploadException(String message, Throwable cause) {
		super(message, cause);
	}

}
