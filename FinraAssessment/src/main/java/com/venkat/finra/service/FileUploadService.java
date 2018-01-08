package com.venkat.finra.service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

	public void persistFile(MultipartFile file, String metaData);

	public Stream<Path> readExistingFiles();

	public Resource buildFileURI(String filename);

	public void deleteAll();

	public void init();
}
