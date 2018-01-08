package com.venkat.finra.service;

import org.springframework.stereotype.Component;

@Component
public class FileStorageLocationProperties {

	/**
	 * This is the class where the directory location is present
	 */
	private String directoryLocation = "C:/Assessments";

	public String getDirectoryLocation() {
		return directoryLocation;
	}

	public void setDirectoryLocation(String directoryLocation) {
		this.directoryLocation = directoryLocation;
	}

}
