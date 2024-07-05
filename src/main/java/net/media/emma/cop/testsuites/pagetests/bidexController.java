package net.media.emma.cop.testsuites.pagetests;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.poi.hssf.record.PageBreakRecord.Break;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.TestDataResult;
import net.media.emma.utils.FileOperations;

@RestController("bidexController")
@RequestMapping("/autoTester/")
@Slf4j
public class bidexController {

	@Autowired
	bidexAndFeatureTests bidexAndFeatureTests; 
	
	@Autowired
	BidexAndFeatureTestsService bidexAndFeatureTestsService;
	
	@Autowired
	ObjectMapper genericObjectMapper;
	@Autowired
	FileOperations fileOperations;

	@Value("${harFileBasePath}")
	private String harFileBasePath;
	
	@Value("${autoTestFilePath}")
	private String autoTestFilePath;

	// @GetMapping("/domInject")
	// public String domINject() {

	// return "welcome2";

	// }

	// @PostMapping("/welcome2")
	// public void openAndDOMINject(@RequestParam String url,
	// @RequestParam String xPath,
	// @RequestParam String htmlElement,
	// @RequestParam Boolean append) {
	// String forceHtmlElement = "src/main/resources/templates/random.html";
	// System.out.println("Calling Dom Inject");
	// browserRemote.injectDom(url, xPath, forceHtmlElement, append);

	// }
	
	Set<ResponseMocking> listOfResponseMocking=new LinkedHashSet<>();
	
	private static final Map<String, String> listOfTestSuites = new LinkedHashMap<>() {
		{
			put("YAML Config Based Tests (Simple)", "/yamlTestSuite/");
			put("YAML Config Based Tests (TestNG)", "/testDataInput");
			put("Page Level Checks - Test Suite", "/pageLevelTests/");
			put("Logging Pixel Data Validation Tests", "/loggingPixelsTestSuite/");
			put("Compliance - GDPR/TCF, CCPA, USPA etc - Tests", "testSuite4");
            put("Auto Tester", "/bidexAndFeatureTests/");
		}
	};

	@GetMapping
	public ModelAndView openIndex() {

		ModelAndView mav = new ModelAndView("bidexAndFeatureTests");
		mav.addObject("files", fileOperations.listYamlFiles(autoTestFilePath));// Arrays.asList(null,
																													// "testCriteria.yaml",
																													// "testCriteria2.yaml"));
//		mav.addObject("testSuites", listOfTestSuites);
		return mav;
	}

	@PostMapping
	public ModelAndView getBidexLevelTestResults(@RequestParam String yaml, @RequestParam String featureMocking) {

		ModelAndView modelAndView = new ModelAndView("bidexAndFeatureTestsDataReport");

		try {
			
			
//			ResponseMocking mocking=new ResponseMocking();
			
//			if(listOfResponseMocking!=null)
//			{
//				for(ResponseMocking rm:listOfResponseMocking)
//				{
//					mocking.setFeature(rm.getFeature());
//					mocking.setResponse(rm.getResponse());
//					mocking.setUrl(rm.getUrl());
//				}
//				
//			}
			
			
						
//			TestCriteriaBo yamlTc = bidexAndFeatureTests.meargeYaml(yaml);
			
//			TestCriteriaBo yamlTc=bidexAndFeatureTests.simpleBuildYaml(yaml);
			
			
			
			List<ResponseMocking> responseMockingList=new ArrayList<>(listOfResponseMocking);
			
			
			BidexFeatureReport bidexFeatureReport=bidexAndFeatureTestsService.bidexTests(yaml,responseMockingList);
			
			 TestDataResult dataResult=TestDataResult.builder().businessFeaturesDetected(bidexFeatureReport.getBusinessFeaturesDetected()).bidex(bidexFeatureReport.getTestMetaDataBidex()).build();
			 
			modelAndView.addObject("rawData",dataResult);
			modelAndView.addObject("allFeatureData", bidexFeatureReport.getAllFeatureData());
			modelAndView.addObject("allCookiesData", bidexFeatureReport.getCookies());
			modelAndView.addObject("basicReport", bidexFeatureReport.getUrlChecksReport());
			modelAndView.addObject("featureReport", bidexFeatureReport.getFeatureReport());
			
		} catch (JsonProcessingException e) {
			log.error("YAML Processing Error", e);
			
			modelAndView.addObject("message", e.getMessage());
		}
		return modelAndView;
	}
	
	
	@GetMapping("responseMock")
	public ResponseMocking getMocking(@RequestParam String feature)
	{
		ResponseMocking mocking=new ResponseMocking();
		
		listOfResponseMocking.forEach(e->{
			if(e.getFeature().equalsIgnoreCase(feature))
			{
				mocking.setFeature(e.getFeature());
				mocking.setResponse(e.getResponse());
				mocking.setUrl(e.getUrl());
				
			}
		});
		
		
		return mocking;
		
	}
	
	
	
	@PostMapping("responseMocking")
	public String getDetailsOfModal(@RequestBody ResponseMocking responseMocking)
	{
	
		ResponseMocking mocking=new ResponseMocking();

		for(ResponseMocking  rm:listOfResponseMocking)
		{
			if(rm.getFeature().equalsIgnoreCase(responseMocking.getFeature())) {
				
				listOfResponseMocking.remove(rm);
				
				break;
			}
		}
		
		mocking.setFeature(responseMocking.getFeature());
		mocking.setUrl(responseMocking.getUrl());
		mocking.setResponse(responseMocking.getResponse());
		
		listOfResponseMocking.add(responseMocking);

		
		return responseMocking.getFeature();
		
		
	}
	

	@PostMapping("fileContent")
	@ResponseBody
	public String getFileContent(@RequestBody String fileName) {

//		if (fileName.length() > 0) {
//
//			try {
//				return new String(Files.readAllBytes(Paths.get("src/main/resources/bidexAndFeatureTestsYaml/" + fileName)));
//			} catch (IOException e) {
//				log.error("File Read Error", e);
//				return e.getMessage();
//			}
//		}
//		return "Input File Name Empty";
		
		ClassPathResource classPathResource=new ClassPathResource(autoTestFilePath+"/"+fileName);
		
		try {
			
			InputStream inputStream=classPathResource.getInputStream();
			
			String content=StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			
			inputStream.close();
			
			return content;
			
		}
		catch (Exception e) {
			
			log.info("Error while reading file : "+ e.getMessage());
			
			return "Error while reading file : "+e.getMessage();
			
			
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