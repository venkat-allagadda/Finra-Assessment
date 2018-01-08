package com.venkat.finra.exception;

public class FileFormatNotSupportedException extends FileUploadException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileFormatNotSupportedException(String message) {
		super(message);
	}

	public FileFormatNotSupportedException(Throwable cause) {
		super(cause);
	}

	public FileFormatNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}
}
