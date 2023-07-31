package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.Files;
import java.nio.file.Paths;

@Controller("yamlTestSuiteController2")
@Slf4j
@RequestMapping("/yamlTestMulUrls/")
public class WebController2 {

	@Autowired
	MulUrlsTestSuite yamlTestSuite;
	@Autowired
	FileOperations fileOperations;
	@Autowired
	ObjectMapper genericObjectMapper;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

	@Value("${yamlTestCriteriaFilesPath}")
	private String yamlMulTestCriteriaFilesPath;

	@GetMapping
	public ModelAndView yamlTestSuite() {

		ModelAndView mav = new ModelAndView("yamlTestSuite");
		mav.addObject("files", fileOperations.listFilesUsingFilesList("src/main/resources/yamlTestMultipleUrls/"));// Arrays.asList(null,
		// "testCriteria.yaml",
		// "testCriteria2.yaml"));

		return mav;
	}

	@PostMapping
	public ModelAndView yamlTestSuite(@RequestParam String yaml) {

		ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

		try {

			ObjectMapper mapper = new YAMLMapper();
			TestCriteriaConsoleError yamlTc = mapper.readValue(yaml, TestCriteriaConsoleError.class);

			String message = yamlTestSuite.getTestResults(yamlTc);

			// mav. addObject("message2", message);
			mav.addObject("message1", mapper.readTree(message));

		} catch (JsonProcessingException | InterruptedException e) {
			log.error("YAML Processing Error", e);
			;
			mav.addObject("message", e.getMessage());
		}

		return mav;
	}

	// @Value("${yamlFolderPath}")
	// private String yamlFolderPath;

	@PostMapping("fileContent")
	@ResponseBody
	public String getFileContent(@RequestBody String fileName) {

		try {

			return new String(Files.readAllBytes(Paths.get("src/main/resources/yamlTestMultipleUrls/" + fileName)));
		} catch (Exception e) {
			log.error("Error reading file.", e);
			return "File  not found";
		}

	}

	@GetMapping("reportTest")
	public ModelAndView reportTest() {

		ModelAndView mav = new ModelAndView("reportTest");
		return mav;
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