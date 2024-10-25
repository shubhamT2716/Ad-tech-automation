package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes = Loggers.class)
public class AssertNetworkCalls extends AbstractTestNGSpringContextTests {

    static HashMap<String, List<String>> logsUrls = new HashMap<>();
    static HashMap<String, List<String>> logsUrlsNotPresent = new HashMap<>();
    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired
    private net.media.emma.config.Loggers loggers;
    @Autowired
    private net.media.emma.coptests.SoftAssertion softAssertion;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void assertPositiveCalls(Object object) {
        try {
            softAssertion.softAssert();
            Map<String, Object> testData = (Map<String, Object>) object;
            System.out.println("Started --- ");
            for (Map.Entry<String, List<String>> reqUrlsData : logsUrls.entrySet()) {
                softAssertion.getSoftAssert().assertTrue(reqUrlsData.getValue().size() > 0, "Network Call not found -- " + reqUrlsData.getKey());
            }
        } catch (Exception e) {
            loggers.standardLogger().error("Error in assertion of network calls -- ", e);
        } finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void assertNegativeCalls(Object object) {
        try {
            softAssertion.softAssert();
            Map<String, Object> testData = (Map<String, Object>) object;
            System.out.println("Started --- ");
            for (Map.Entry<String, List<String>> reqUrlsData : logsUrlsNotPresent.entrySet()) {
                softAssertion.getSoftAssert().assertTrue(reqUrlsData.getValue().size() == 0, "Network Call found -- " + reqUrlsData.getKey());
            }
        } catch (Exception e) {
            loggers.standardLogger().error("Error in assertion of network calls -- ", e);
        } finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }
}
