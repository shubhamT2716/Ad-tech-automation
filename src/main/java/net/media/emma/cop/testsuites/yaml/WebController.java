package net.media.emma.cop.testsuites.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
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
	ObjectMapper yamlMapper;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

	@Value("${yamlTestCriteriaFilesPath}")
	private String yamlTestCriteriaFilesPath;

	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String yamlSmokeTestSuiteFilesPath;

	@Value("${zipFileBasePath}")
	private String zipFilePath;

	@Autowired
	YamlTestSuiteScheduling yamlTestSuiteScheduling;

	@GetMapping("/smokeTestSuite")
	public ModelAndView threadPoolTestForm() {

		ModelAndView mav = new ModelAndView("smokeTestSuite");
		mav.addObject("fileNames", new ArrayList<>(getFileNamesForSmokeTest()));
		return mav;

	}

	@PostMapping("/smokeTestSuite")
	public ModelAndView runThreadPoolTest(@RequestParam("env") String environment, @RequestParam("ua") String userAgent,
			@RequestParam(value = "selectedFiles", required = false) List<String> selectedFiles) {

		ModelAndView mav = new ModelAndView("smokeTestSuiteReport");

		SmokeTestRawDataDto result = yamlTestSuiteScheduling.getAutoTest(environment, userAgent, selectedFiles, false);

		try {
			mav.addObject("message2", result.getSmokeReport());
			mav.addObject("message1", result.getRawData());
			mav.addObject("message3", result.getBidExReport());
			mav.addObject("message4", result.getErrorMessage());

		} catch (Exception e) {
			log.info("Error reading file.", e);
			mav.addObject("message", e.getMessage());
		}

		return mav;

	}

	@GetMapping("/smokeTestSuite/{fileName}")
	public ModelAndView runReortForEmail(@PathVariable("fileName") String fileName) {

		ModelAndView mav = new ModelAndView("smokeTestSuiteReport");

		JsonNode resultData = yamlTestSuiteScheduling.readDataFromZip(fileName);

		try {
			mav.addObject("message2", resultData);
		} catch (Exception e) {
			log.info("Error in reading zip = " + e);
			mav.addObject("message", e.getMessage());
		}

		return mav;

	}

	@GetMapping("/autoTest")
	public ModelAndView getAutoTest() {

		Set<String> listOfYaml = fileOperations.listFilesUsingFilesList(yamlTestCriteriaFilesPath);

		Map<String, JsonNode> result = new HashMap<>();

		ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

		int i = 0;
		try {
			for (String yaml : listOfYaml) {

				if (i < 2) {
					String yamlContent = fileOperations.readFileAsString(yamlTestCriteriaFilesPath + yaml);

					Thread thread = new Thread(yamlContent);
					thread.start();
					// thread.join();

					ObjectMapper objectMapper = new YAMLMapper();
					TestCriteriaBo testCriteria = objectMapper.readValue(yamlContent, TestCriteriaBo.class);

					TestReportModel message = yamlTestSuite.getTestResults(testCriteria);

					JsonNode jsonNode = yamlMapper.convertValue(message, JsonNode.class);

					result.put(yaml, jsonNode);
				}
				i++;
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
		mav.addObject("files", fileOperations.listYamlFiles(yamlTestCriteriaFilesPath));// Arrays.asList(null,
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

			TestReportModel message = yamlTestSuite.getTestResults(yamlTc);

			// mav. addObject("message2", message);

			JsonNode jsonNode = yamlMapper.convertValue(message, JsonNode.class);

			mav.addObject("message1", jsonNode);

		} catch (JsonProcessingException e) {
			log.error("YAML Processing Error", e);
			mav.addObject("message", e.getMessage());
		}

		return mav;
	}

	// @Value("${yamlFolderPath}")
	// private String yamlFolderPath;

	@PostMapping("fileContent")
	@ResponseBody
	public String getFileContent(@RequestBody String fileName) {
	
//		String filePath =  yamlTestCriteriaFilesPath + "/" + fileName;
//
//		
////		 String file = Paths.get(filePath).toString();
//
//		    try {
//		        // Read the file content using java.nio.file.Files
//		        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
//		        String content = new String(bytes, StandardCharsets.UTF_8);
//		        return content;
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		        return "Error while reading file: " + e.getMessage();
//		    }

		
		ClassPathResource classPathResource = new ClassPathResource(yamlTestCriteriaFilesPath+"/" + fileName);

		try {
			InputStream inputStream = classPathResource.getInputStream();

			String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

			inputStream.close();

			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while reading Yaml" + e.getMessage();
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

	@GetMapping("zipDownload/{fileName}")
	public ResponseEntity<?> showZipContent(@PathVariable String fileName) {
		Resource resource = fileOperations.loadFileAsResource(zipFilePath + fileName);

		if (resource == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);

	}

	private Set<String> getFileNamesForSmokeTest() {
		// Fetch the list of YAML file names from the directory
		Set<String> fileNames = fileOperations.listYamlFiles(yamlSmokeTestSuiteFilesPath);
		// Add additional filtering logic if needed (e.g., exclude specific files)
		return fileNames;
	}

}