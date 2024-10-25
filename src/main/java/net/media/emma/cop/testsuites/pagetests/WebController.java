package net.media.emma.cop.testsuites.pagetests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Controller("pageLevelTestsController")
@RequestMapping("/pageLevelTests/")
@Slf4j
public class WebController {

    // }
    private static final Map<String, String> listOfTestSuites = new LinkedHashMap<>() {
        {
            put("YAML Config Based Tests (Simple)", "/yamlTestSuite/");
            put("YAML Config Based Tests (TestNG)", "/testDataInput");
            put("Page Level Checks - Test Suite", "/pageLevelTests/");
            put("Logging Pixel Data Validation Tests", "/loggingPixelsTestSuite/");
            put("Compliance - GDPR/TCF, CCPA, USPA etc - Tests", "testSuite4");
        }
    };
    @Autowired
    PageLevelTests pageLevelTests;
    @Autowired
    ObjectMapper genericObjectMapper;
    @Autowired
    FileOperations fileOperations;
    @Value("${harFileBasePath}")
    private String harFileBasePath;

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
    @Value("${pageLevelyamlFilesPath}")
    private String pageTestyamlPath;

    @GetMapping
    public ModelAndView openIndex() {

        ModelAndView mav = new ModelAndView("pageLevelTests");
        mav.addObject("files", fileOperations.listYamlFiles(pageTestyamlPath));// Arrays.asList(null,
        // "testCriteria.yaml",
        // "testCriteria2.yaml"));
        mav.addObject("testSuites", listOfTestSuites);
        return mav;
    }

    @PostMapping
    public ModelAndView getPageLevelTestsResult(@RequestParam String yaml) {

        ModelAndView modelAndView = new ModelAndView("pageLevelTestsDataReport");

        try {


            ObjectMapper mapper = new YAMLMapper();
            TestCriteria yamlTc = mapper.readValue(yaml, TestCriteria.class);
            // mav.addObject("message", yamlTc.getEnvironment());
            // mav.addObject("message", yamlTestSuite.getTestResults(yamlTc));
            // modelAndView.addObject("message", );


            modelAndView.addObject("message1", pageLevelTests.getPageLevelTestsData(yamlTc));

        } catch (JsonProcessingException e) {
            log.error("YAML Processing Error", e);
            modelAndView.addObject("message", e.getMessage());
        }
        return modelAndView;
    }

    @PostMapping("fileContent")
    @ResponseBody
    public String getFileContent(@RequestBody String fileName) {

//		if (fileName.length() > 0) {
//
//			try {
//				return new String(Files.readAllBytes(Paths.get("src/main/resources/pageLevelTestsYaml/" + fileName)));
//			} catch (IOException e) {
//				log.error("File Read Error", e);
//				return e.getMessage();
//			}
//		}
//		return "Input File Name Empty";

        ClassPathResource resource = new ClassPathResource(pageTestyamlPath + "/" + fileName);

        try {

            // return new String(Files.readAllBytes(Paths.get(resource.getURI())));

            InputStream inputStream = resource.getInputStream();

            String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

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