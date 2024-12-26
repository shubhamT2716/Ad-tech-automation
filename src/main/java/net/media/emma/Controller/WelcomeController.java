package net.media.emma.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class WelcomeController {

    private static final Map<String, String> listOfTestSuites = new LinkedHashMap<>() {
        {
            put("Yaml Rules for Publisher ", "/yamlTestSuite/");
            put("Smoke Test Suite", "/yamlTestSuite/smokeTestSuite");
            //put("Page Level Checks - Test Suite", "/pageLevelTests/");
            //put("Logging Pixel Data Validation Tests", "/loggingPixelsTestSuite/");
            put("Compliance - GDPR/TCF, CCPA, USPA etc - Tests", "/privacyComplianceTestSuite/");
            put("Auto Tester", "/autoTester/");
            //put("Show Report", "/yamlTestSuite/show-report");
            put("Yaml Rules for TestPage", "/yamlTestSuite/testpage");
//            put("custom Yaml", "/yamlTestSuite/customYaml");
        }
    };

    @GetMapping("/")
    public ModelAndView openIndex() {

        ModelAndView mav = new ModelAndView("emma-index");
        mav.addObject("testSuites", listOfTestSuites);
        return mav;
    }


}