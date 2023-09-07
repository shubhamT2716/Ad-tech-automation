package net.media.emma.cop.testsuites.loggingpixels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.utils.FileOperations;

@Controller("loggingPixelsTestSuiteController")
@Slf4j
@RequestMapping("/loggingPixelsTestSuite/")
public class WebController {

//    @Autowired GenericTestSuite yamlTestSuite;
	@Autowired
	FileOperations fileOperations;
	@Autowired
	LoggingPixelTestSuite pixelTestSuite;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

	@Value("${loggingpixelsTestCriteriaFilesPath}")
	private String loggingpixelsTestCriteriaFilesPath;

	@GetMapping
	public ModelAndView yamlTestSuite() {

		ModelAndView mav = new ModelAndView("loggingPixelTestSuite");
		mav.addObject("files", fileOperations.listYamlFiles(loggingpixelsTestCriteriaFilesPath));// Arrays.asList(null,
																											// "testCriteria.yaml",
																											// "testCriteria2.yaml"));

		return mav;
	}

	@PostMapping
	public ModelAndView yamlTestSuite(@RequestParam String yaml) {
		ModelAndView mav = new ModelAndView("loggingPixelTestSuiteReport");
		LoggingPixelTestDataModel testCriteria;

		try {
			ObjectMapper mapper = new YAMLMapper();
			testCriteria = mapper.readValue(yaml, LoggingPixelTestDataModel.class);

		} catch (Exception e) {
			log.error("YAML Processing Error", e);
			mav.addObject("message", e.getMessage());
			return mav;
		}

		mav.addObject("message1", pixelTestSuite.getLoggingPixelTestResults(testCriteria));

		return mav;
	}

	@PostMapping("fileContent")
	@ResponseBody
	public String getFileContent(@RequestBody String fileName) {

//		if (fileName.length() > 0) {
//
//			try {
//				return new String(Files.readAllBytes(Paths.get(loggingpixelsTestCriteriaFilesPath + fileName)));
//			} catch (IOException e) {
//				log.error("File Read Error", e);
//				return e.getMessage();
//			}
//		}
//		return "Input File Name Empty";
		
		ClassPathResource resource = new ClassPathResource("yaml/" + fileName);
		
		try {
			
			// return new String(Files.readAllBytes(Paths.get(resource.getURI())));

			InputStream inputStream = resource.getInputStream();
			
			String content =StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

			inputStream.close();

			return content;

		} catch (Exception e) {
			log.error("Error reading file.", e);
			return e.getMessage();
		}	
	}

	@GetMapping("harDownload/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {

		// add a new function in FileOperations.java to get the file as a resource
		Resource resource = fileOperations.loadFileAsResource(harFileBasePath + fileName);

		if (resource == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
}