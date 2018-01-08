package com.venkat.finra.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.venkat.finra.controller.UploadFileController;
import com.venkat.finra.exception.FileUploadException;
import com.venkat.finra.exception.UploadedFileNotFoundException;

@Service
public class FileUploadServiceImpl implements FileUploadService {

	private static final Logger logger = LogManager.getLogger(UploadFileController.class);

	private Path rootDirectoryPath;

	@Autowired
	public FileUploadServiceImpl(FileStorageLocationProperties rootDirectory) {

		this.rootDirectoryPath = fetchRootDirectoryPath(rootDirectory);

	}

	/**
	 * get the directory location where all the files are stored into the disk
	 * @param rootDirectory for getting the storage location
	 * @return Path 
	 */
	private Path fetchRootDirectoryPath(FileStorageLocationProperties rootDirectory) {
		return Paths.get(rootDirectory.getDirectoryLocation());
	}

    /**
     * this method stores the uploaded file in the disk, if the file already exists then it is replaced by default
     * @param file that needs to be stored
     * @param fileDesc description of the file
     */
	@Override
	public void persistFile(MultipartFile file, String fileDesc) {

		try {
			if (file.isEmpty()) {
				throw new FileUploadException("File Upload failed because of empty file " + file.getOriginalFilename());
			}
			Files.copy(file.getInputStream(), this.rootDirectoryPath.resolve(file.getOriginalFilename()),
					StandardCopyOption.REPLACE_EXISTING);
			writeMetaData(file.getOriginalFilename(), fileDesc);

		} catch (IOException e) {
			throw new FileUploadException("File Upload failed " + file.getOriginalFilename(), e);
		}
	}

	/**
	 * this method writes the supplied file description from the user into a text file and store them in the disk
	 * @param fileName uploaded file name
	 * @param fileDesc description of the file
	 */
	private void writeMetaData(String fileName, String fileDesc) {

		String content = "This file is meta data for " + fileName + "\n\n" + fileDesc;
		BufferedWriter writer = null;
		try {
			writer = Files.newBufferedWriter(this.rootDirectoryPath.resolve(generateMetaDataFileName(fileName)));
			writer.write(content);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	/**
	 * this method generates the appropriate filename based on the uploaded file name for storing metadata
	 * @param fileName uploaded file name
	 * @return string of meta data file name to be stored
	 */
	private String generateMetaDataFileName(String fileName) {

		return FilenameUtils.removeExtension(fileName).concat("_metadata").concat(".txt");

	}

	/**
	 * read all the existing files from the disk by fetching their Path objects and return a stream of Path
	 * objects
	 * @return Stream<Path> 
	 */
	@Override
	public Stream<Path> readExistingFiles() {

		try {
			return Files.walk(this.rootDirectoryPath, 1).filter(filePath -> !filePath.equals(this.rootDirectoryPath))
					.map(filePath -> this.rootDirectoryPath.relativize(filePath));
		} catch (IOException e) {
			throw new FileUploadException("Reading stored files has failed", e);
		}

	}

	/**
	 * append the filename to the root directory path i.e. storage path on the disk
	 * @param filename stored file name
	 * @return Path 
	 */
	private Path getFilePath(String filename) {
		return this.rootDirectoryPath.resolve(filename);
	}

	/**
	 * generate a restful resource/object from the given file path
	 * @param filename stored file name
	 * @return Resource for converting into a restful object by rest controller
	 */
	@Override
	public Resource buildFileURI(String filename) {
		try {
			Path file = getFilePath(filename);
			Resource uri = new UrlResource(file.toUri());
			if (uri.exists() || uri.isReadable()) {
				return uri;
			} else {
				throw new UploadedFileNotFoundException("Unable to fetch the file: " + filename);

			}
		} catch (MalformedURLException e) {
			throw new UploadedFileNotFoundException("Unable to fetch the file: " + filename, e);
		}
	}

	/**
	 * delete all the existing files from the disk
	 */
	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootDirectoryPath.toFile());

	}

	/**
	 * initialize the directory for storing files on the disk
	 */
	@Override
	public void init() {
		try {
			if (Files.notExists(this.rootDirectoryPath)) {
				Path pkd = Files.createDirectory(rootDirectoryPath);
			}

		} catch (IOException e) {
			throw new FileUploadException("Could not initialize the persistence directory location", e);
		}
	}

}
