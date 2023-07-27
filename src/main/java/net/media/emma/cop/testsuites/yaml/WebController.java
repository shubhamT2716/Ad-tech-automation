package net.media.emma.cop.testsuites.yaml;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.FileOperations;

@Controller("yamlTestSuiteController")
@Slf4j
@RequestMapping("/yamlTestSuite/")
public class WebController {

	@Autowired
	GenericTestSuite yamlTestSuite;
	@Autowired
	FileOperations fileOperations;
	@Autowired
	ObjectMapper mapper;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

	@Value("${yamlTestCriteriaFilesPath}")
	private String yamlTestCriteriaFilesPath;

	@GetMapping("/autoTest")
	public ModelAndView getAutoTest() {

		Set<String> listOfYaml = fileOperations.listFilesUsingFilesList(yamlTestCriteriaFilesPath);

		Map<String, JsonNode> result = new HashMap<>();

		ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

		try {
			for (String yaml : listOfYaml) {

				String yamlContent = fileOperations.readFileAsString(yamlTestCriteriaFilesPath + yaml);

				Thread thread = new Thread(yamlContent);
				thread.start();
				// thread.join();

				ObjectMapper objectMapper = new YAMLMapper();
				TestCriteriaBo testCriteria = objectMapper.readValue(yamlContent, TestCriteriaBo.class);

				String message = yamlTestSuite.getTestResults(testCriteria);

				result.put(yaml, mapper.readTree(message));
			}
			mav.addObject("message1", result);
		} catch (Exception e) {
			log.error("Error reading file.", e);
			mav.addObject("message", e.getMessage());
		}

		log.info("Result = " + result);

		return mav;
	}

//	@Autowired
//	private YamlTestSuiteScheduling scheduling;
//
//	@GetMapping("/schedulerReport")
//	public ModelAndView generatedReportFromScheduling() {
//		return this.scheduling.getAutoTest();
//	}

	@GetMapping
	public ModelAndView yamlTestSuite() {

		ModelAndView mav = new ModelAndView("yamlTestSuite");
		mav.addObject("files", fileOperations.listFilesUsingFilesList(yamlTestCriteriaFilesPath));// Arrays.asList(null,
		// "testCriteria.yaml",
		// "testCriteria2.yaml"));

		return mav;
	}

	@PostMapping
	public ModelAndView yamlTestSuite(@RequestParam String yaml) {

		ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

		try {

			ObjectMapper mapper = new YAMLMapper();
			TestCriteriaBo yamlTc = mapper.readValue(yaml, TestCriteriaBo.class);

			String message = yamlTestSuite.getTestResults(yamlTc);

			// mav. addObject("message2", message);
			mav.addObject("message1", mapper.readTree(message));

		} catch (JsonProcessingException e) {
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

			return new String(Files.readAllBytes(Paths.get(yamlTestCriteriaFilesPath + fileName)));
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