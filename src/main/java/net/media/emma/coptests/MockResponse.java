package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

@Service
@ContextConfiguration(classes = Loggers.class)
public class MockResponse extends AbstractTestNGSpringContextTests {

    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired
    private net.media.emma.config.Loggers loggers;
    @Autowired
    private net.media.emma.coptests.SoftAssertion softAssertion;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void mockResponse(Object object) {
        try {
            softAssertion.softAssert();
            Map<String, Object> testData = (Map<String, Object>) object;
            HashMap<String, String> responseSimulate = new HashMap<>();
            if (testData.get("responseSimulate") != null) {
                responseSimulate = (HashMap<String, String>) testData.get("responseSimulate");
            }
            if (!("responseSimulate").isEmpty()) {
                this.browserRemote.mockResponse(responseSimulate.get("urlPattern"), responseSimulate.get("jsonPath"), responseSimulate.get("updatedValue"));
            }
        } catch (Exception e) {
            softAssertion.getSoftAssert().fail("Unable to Mock Response");
            loggers.standardLogger().error("Unable to Mock Response", e);
        } finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }
}
