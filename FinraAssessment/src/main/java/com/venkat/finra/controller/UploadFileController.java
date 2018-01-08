package com.venkat.finra.controller;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.venkat.finra.exception.*;
import com.venkat.finra.service.FileUploadService;

@Controller
public class UploadFileController {

	private static final Logger logger = LogManager.getLogger(UploadFileController.class);

	@Autowired
	private FileUploadService apiService;

	/**
	 * GET API to retrieve the existing files
	 * 
	 * @param model
	 *            contains the list of files stored
	 * @return the thymleaf template
	 * @throws IOException
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String fetchStoredFiles(Model model) throws IOException {
		logger.info("retrieving the existing files from the disk");
		model.addAttribute("files",
				apiService.readExistingFiles().map(filePath -> MvcUriComponentsBuilder
						.fromMethodName(UploadFileController.class, "fetchFile", filePath.getFileName().toString())
						.build().toString()).collect(Collectors.toList()));

		return "FileUpload";
	}

	/**
	 * GET API to render default error page
	 * 
	 * @return the thymleaf template
	 */
	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String defaultErrorPage() {
		return "error";
	}

	/**
	 * GET API to convert the generated resource object into a http restful uri
	 * 
	 * @param filename
	 *            corresponds to a restful resource
	 * @return resource for downloading the persisted file
	 */
	@RequestMapping(value = "/files/{filename:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Resource> fetchFile(@PathVariable String filename) {
		logger.debug("fetching a single file from the disk");
		Resource file = apiService.buildFileURI(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	/**
	 * POST API to upload a file
	 * 
	 * @param file
	 *            to be stored on the disk
	 * @param fileDesc
	 *            contains the description of the uploaded file
	 * @param redirectAttributes
	 *            has the message attribute for user confirmation
	 * @param request
	 *            to verify if the corresponding upload request conforms to
	 *            multipart MIME type
	 * @return the thymleaf template
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String UploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "meta-data", required = false) String fileDesc, RedirectAttributes redirectAttributes,
			HttpServletRequest request) {

		logger.info("Uploading a file into the disk.........");

		try {
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (!isMultipart) {
				logger.error("File format not supported!");
				throw new FileFormatNotSupportedException(
						"The requested file format is not supported by the application");
			} else {
				apiService.persistFile(file, fileDesc);
				redirectAttributes.addFlashAttribute("message",
						"You successfully uploaded " + file.getOriginalFilename() + "!");
				logger.debug(file.getOriginalFilename() + " is successfully uploaded into the disk");
			}

		} catch (FileUploadException e) {

			throw new FileUploadException(e);
		}
		return "redirect:/";
	}

	/**
	 * GET API to delete the existing
	 * 
	 * @param model
	 *            to add attributes/properties on template
	 * @return the thymleaf template
	 */
	@RequestMapping(value = "/deleteFiles", method = RequestMethod.GET)
	public String deleteFiles(Model model) {
		logger.info("removing all the existing files from the disk and recreating the directory structure");
		apiService.deleteAll();
		apiService.init();
		model.addAttribute("message", "All the existing file(s) are deleted successfully!");
		return "FileUpload";
	}
}
