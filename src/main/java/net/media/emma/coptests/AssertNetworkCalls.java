package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes = Loggers.class)
public class AssertNetworkCalls extends AbstractTestNGSpringContextTests {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired private net.media.emma.config.Loggers loggers;
    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    static HashMap<String, List<String>> logsUrls = new HashMap<>();
    static HashMap<String, List<String>> logsUrlsNotPresent = new HashMap<>();

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void assertPositiveCalls(Object object) {
        try {
            Map<String, Object> testData = (Map<String, Object>) object;
            System.out.println("Started --- ");
            for (Map.Entry<String, List<String>> reqUrlsData : logsUrls.entrySet()) {
                softAssertion.softAssert.assertTrue(reqUrlsData.getValue().size() > 0, "Network Call not found -- " + reqUrlsData.getKey());
            }

            for (Map.Entry<String, List<String>> reqUrlsData : logsUrlsNotPresent.entrySet()) {
                softAssertion.softAssert.assertTrue(reqUrlsData.getValue().size() == 0, "Network Call found -- " + reqUrlsData.getKey());
            }
        }catch(Exception e) {
            loggers.standardLogger().error("Error in assertion of network calls -- ",e);
        }finally {
            softAssertion.softAssert.assertAll();
        }
    }
}
