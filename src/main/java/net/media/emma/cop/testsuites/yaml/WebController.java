package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller("yamlTestSuiteController")
@Slf4j
@RequestMapping("/yamlTestSuite/")
@RestController
public class WebController {

    @Autowired
    GenericTestSuite yamlTestSuite;
    @Autowired
    FileOperations fileOperations;
    @Autowired
    ObjectMapper yamlMapper;
    @Autowired
    YamlTestSuiteScheduling yamlTestSuiteScheduling;
    @Value("${harFileBasePath}")
    private String harFileBasePath;
    @Value("${yamlTestCriteriaFilesPath}")
    private String yamlTestCriteriaFilesPath;
    @Value("${testPageFilePath}")
    private String testPageFilePath;
    @Value("${yamlSmokeTestSuiteFilesPath}")
    private String yamlSmokeTestSuiteFilesPath;
    @Value("${zipFileBasePath}")
    private String zipFilePath;
    @Value("${commonYamlFilePath}")
    private String commonYamlFile;
    @Value("${autoTestFilePath}")
    private String autoTesterYamlFile;
    @Value("${reportFileBasePath}")
    private String reportFilePath;
    @Autowired
    private TestPageResponseMapping mapping;
    @Autowired
    private WebhookServiceIn webhookService;

    @GetMapping("/smokeTestSuite")
    public ModelAndView threadPoolTestForm() {

        ModelAndView mav = new ModelAndView("smokeTestSuite");
        mav.addObject("fileNames", new ArrayList<>(getFileNamesForSmokeTest()));
        mav.addObject("commonFile", new ArrayList<>(getCommonFilesForSmokeTest()));
        return mav;

    }

    @GetMapping("smokeTestSuiteItype")
    public Map<String, Set<String>> getFilesOnItypeForForm(@RequestParam(name = "itype", required = false) String itype) {

        Map<String, Set<String>> mav = new HashMap<>();

        if (!itype.equalsIgnoreCase("all")) {

            mav.put("fileNames", getFileNamesForSmokeTestItype(itype));
            mav.put("commonFile", getCommonFilesForApi(itype));

        } else {
            mav.put("fileNames", getFileNamesForSmokeTest());
            mav.put("commonFile", getCommonFilesForSmokeTest());
        }


        return mav;

    }

    @PostMapping("/smokeTestSuite")
    @ResponseBody
    public ModelAndView runThreadPoolTest(@RequestParam("env") String environment, @RequestParam("ua") String userAgent,
                                          @RequestParam(value = "selectedFiles", required = false) List<String> selectedFiles,
                                          @RequestParam(value = "commonFiles", required = false) List<String> commonFiles,
                                          @RequestParam(value = "email", required = false) String email,
                                          @RequestParam(value = "itype", required = false) String itype) {


        ModelAndView mav = new ModelAndView("smokeTestSuiteReport");


        SmokeTestRawDataDto result = yamlTestSuiteScheduling.getAutoTest(environment, userAgent, selectedFiles, commonFiles, null, itype, email, true);
        // SmokeTestRawDataDto result = yamlTestSuiteScheduling.getAutoTest(environment, userAgent, selectedFiles,commonFiles,null,email, true);

        try {
            mav.addObject("message2", result.getSmokeReport());
            mav.addObject("message1", result.getRawData());
            mav.addObject("message3", result.getControllerReport());
            mav.addObject("message4", result.getErrorMessage());

        } catch (Exception e) {
            log.info("Error reading file.", e);
            mav.addObject("message", e.getMessage());
        }

        return mav;

    }

    @GetMapping("/smokeTest")
    @ResponseBody
    public ResponseEntity<?>/*ModelAndView*/ smokeTestAPi(@RequestParam("env") String environment, @RequestParam("ua") String userAgent,
                                                          @RequestParam(value = "pubs", required = false) String pubs,
                                                          @RequestParam(value = "email", required = false) String email,
                                                          @RequestParam(value = "itype", required = false) String itype) {
//			@RequestParam(value = "commonFiles",required = false)List<String> commonFiles) {


        List<String> commonFile = new ArrayList<>(getCommonFilesForApi(itype));

        List<String> autoTest = new ArrayList<>(getAutoTesterFilesTest());


        ModelAndView mav = new ModelAndView("smokeTestSuiteReport");

        // SmokeTestRawDataDto result = yamlTestSuiteScheduling.getAutoTest(environment, userAgent, autoTest,commonFile,pubs,itype, false);
        SmokeTestRawDataDto result = yamlTestSuiteScheduling.getAutoTest(environment, userAgent, autoTest, commonFile, pubs, itype, email, true);

        try {


            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.info("Error reading file.", e);
            mav.addObject("message", e.getMessage());

            return (ResponseEntity<?>) ResponseEntity.badRequest();
        }


    }


    @GetMapping("/smokeTestSuite/{fileName}")
    public ModelAndView runReortForEmail(@PathVariable("fileName") String fileName) {

        ModelAndView mav = new ModelAndView("zipFileSmokeReport");

        Map<String, Map<String, Object>> resultData = yamlTestSuiteScheduling.readDataFromZip(fileName);

        try {
            mav.addObject("message2", resultData.get("basicReport"));
            mav.addObject("message1", resultData.get("fullReport"));

        } catch (Exception e) {
            log.info("Error in reading zip = " + e);
            mav.addObject("message", e.getMessage());
        }

        return mav;

    }
    
    
    @GetMapping("/smokeTestSuiteScheduler/{fileName}")
    public ModelAndView getSchedulerFileReport(@PathVariable("fileName")String fileName)
    {
    	
    	ModelAndView mav=new ModelAndView("SchedulerReportNew");
    	
    	Map<String, Map<String, Object>> resultData=yamlTestSuiteScheduling.readDataFromSchedulerFile(fileName);
    	
    	try {
    		
    		mav.addObject("report", resultData.get("basicReport"));
    		
    	}catch (Exception e) {
			log.info("error in reading scheduler file: "+e); 
    		mav.addObject("message1", e.getMessage());
		}
    	
    	return mav;
    	
    }


    @GetMapping("/userAgent")
    public Map<String, String> getUserAgent() {

        Map<String, String> map = BrowserFactory.browserContexts;

        return map;
    }


    @GetMapping
    public ModelAndView yamlTestSuite() {

        ModelAndView mav = new ModelAndView("yamlTestSuite");
        mav.addObject("files", fileOperations.listYamlFiles(yamlTestCriteriaFilesPath));// Arrays.asList(null,

        return mav;
    }

    @PostMapping
    public ModelAndView yamlTestSuite(@RequestParam String yaml,
                                      @RequestParam(name = "url", required = false) String url,
                                      @RequestParam(name = "response", required = false) String response) {

        ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

        try {


            ObjectMapper mapper = new YAMLMapper();

            TestCriteriaBo yamlTc = mapper.readValue(yaml, TestCriteriaBo.class);

            TestReportModel message = yamlTestSuite.getTestResults(yamlTc, url, response);

            JsonNode jsonNode = yamlMapper.convertValue(message, JsonNode.class);

            mav.addObject("message1", jsonNode);

        } catch (JsonProcessingException e) {
            log.error("YAML Processing Error", e);
            mav.addObject("message", e.getMessage());
        }

        return mav;
    }

    @GetMapping("/testpage")
    public ModelAndView getTestPage() {
        ModelAndView mav = new ModelAndView("testPage");
        mav.addObject("files", fileOperations.listOfTestPage(testPageFilePath));

        return mav;
    }

    @PostMapping("/testpage")
    public ModelAndView testPageSuite(@RequestParam String yaml,
                                      @RequestParam(name = "url", required = false) String url,
                                      @RequestParam(name = "response", required = false) String response) {

        ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

        try {

            ObjectMapper mapper = new YAMLMapper();

            TestCriteriaBo yamlTc = mapper.readValue(yaml, TestCriteriaBo.class);

            TestReportModel message = yamlTestSuite.getTestResults(yamlTc, url, response);

            JsonNode jsonNode = yamlMapper.convertValue(message, JsonNode.class);

            mav.addObject("message1", jsonNode);

        } catch (JsonProcessingException e) {
            log.error("YAML Processing Error", e);
            mav.addObject("message", e.getMessage());
        }

        return mav;
    }


    @PostMapping("/data")
    public String/*ModelAndView*/ demoWebhooks(@RequestBody(required = false) String payload) {
        log.info("Webhooks called ");
        log.info(payload);

        new Thread(() -> this.webhookService.executeWebhook(payload)).start();

        return "complete";


    }


    @PostMapping("fileContent")
    @ResponseBody
    public String getFileContent(@RequestBody String fileName) {

        ClassPathResource classPathResource = new ClassPathResource(yamlTestCriteriaFilesPath + "/" + fileName);

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

    //	@PostMapping("testPageFileContent")
//	@ResponseBody
    public String getTestPageFileContent(String fileName) {

        ClassPathResource classPathResource = new ClassPathResource(testPageFilePath + "/" + fileName);

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

    @PostMapping("testPageFileContent")
    @ResponseBody
    public Map<String, String> getTestPageContent(@RequestBody String filename) {
        Map<String, String> content = new HashMap<>();

        content.put("yaml", getTestPageFileContent(filename));
        content.put("url", this.mapping.getTestPageUrl(filename));
        content.put("response", this.mapping.gettestPageResponse(filename));

        return content;

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


    @GetMapping("/downloadFile")
    public ResponseEntity<?> getExelFiles(@RequestParam String fileName) {

        String filepath = reportFilePath + fileName;
        // Load the Excel file
        Resource resource = new FileSystemResource(filepath);

        // Set the appropriate headers for Excel file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }


    private Set<String> getFileNamesForSmokeTest() {
        // Fetch the list of YAML file names from the directory
        Set<String> fileNames = fileOperations.listYamlFiles(yamlSmokeTestSuiteFilesPath);
        // Add additional filtering logic if needed (e.g., exclude specific files)
        return fileNames;
    }

    private Set<String> getFileNamesForSmokeTestItype(String itype) {
        // Fetch the list of YAML file names from the directory
        Set<String> fileNames = fileOperations.listYamlFiles(yamlSmokeTestSuiteFilesPath + "/" + itype);
        // Add additional filtering logic if needed (e.g., exclude specific files)
        return fileNames;
    }

    private Set<String> getCommonFilesForSmokeTest() {
        Set<String> fileName = fileOperations.listYamlFiles(commonYamlFile);

        return fileName;
    }

    private Set<String> getCommonFilesForApi(String itype) {
        Set<String> fileName = fileOperations.listYamlFiles(commonYamlFile + "/" + itype);

        return fileName;
    }


    private Set<String> getAutoTesterFilesTest() {
        Set<String> fileName = fileOperations.listYamlFiles(autoTesterYamlFile);

        return fileName;
    }


}