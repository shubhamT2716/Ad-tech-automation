package net.media.emma.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class Welcome {

    private static final Map<String, String> listOfTestSuites = new LinkedHashMap<>() {
        {
            put("YAML Config Based Tests (Simple)", "/yamlTestSuite/");
            put("Page Level Checks - Test Suite", "/pageLevelTests/");
            put("Logging Pixel Data Validation Tests", "/loggingPixelsTestSuite/");
            put("Compliance - GDPR/TCF, CCPA, USPA etc - Tests", "/privacyComplianceTestSuite/");
        }
    };
    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;

    @GetMapping("/welcome")
    public String openUrl() {
        return "welcome";
    }

    @PostMapping("/welcome")
    public void openUrlPost(@RequestParam String url) {

        // browserRemote.injectDom(url, url, null);
        browserRemote.openUrl(url);
    }

    @GetMapping("/")
    public ModelAndView openIndex() {

        ModelAndView mav = new ModelAndView("emma-index");
        mav.addObject("testSuites", listOfTestSuites);
        return mav;
    }


}