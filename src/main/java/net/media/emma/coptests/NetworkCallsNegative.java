package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@ContextConfiguration(classes = Loggers.class)
public class NetworkCallsNegative extends AbstractTestNGSpringContextTests {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired private net.media.emma.config.Loggers loggers;
    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    @Autowired
    AssertNetworkCalls assertNetworkCalls;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void networkCallsNeg(Object object) {

        try {
            softAssertion.softAssert();
            Map<String, Object> testData = (Map<String, Object>) object;
            HashMap<String, String> mnetNetworkCallsNegative = (HashMap<String, String>) testData.get("mnetNetworkCallsNegative");
            for (Map.Entry<String, String> networkCall : mnetNetworkCallsNegative.entrySet()) {
                CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> this.browserRemote.getRequestUrls(networkCall.getValue()));
                List<String> urls = future.join();
                assertNetworkCalls.logsUrlsNotPresent.put(networkCall.getKey(), urls);
            }
        }catch (Exception e) {
            softAssertion.getSoftAssert().fail("Error in getting network calls negative");
            loggers.standardLogger().error("Error in getting network calls negative", e);
        }finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }
}
