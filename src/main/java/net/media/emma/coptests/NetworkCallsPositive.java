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
public class NetworkCallsPositive extends AbstractTestNGSpringContextTests {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired private net.media.emma.config.Loggers loggers;
    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    @Autowired
    AssertNetworkCalls assertNetworkCalls;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void networkCallsPos(Object object) {

        try {
            softAssertion.softAssert();
            Map<String, Object> testData = (Map<String, Object>) object;
            HashMap<String, String> mnetNetworkCalls = (HashMap<String, String>) testData.get("mnetNetworkCalls");
            for (Map.Entry<String, String> networkCall : mnetNetworkCalls.entrySet()) {
                CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> this.browserRemote.getRequestUrls(networkCall.getValue()));
                List<String> urls = future.join();
                assertNetworkCalls.logsUrls.put(networkCall.getKey(), urls);
            }
        }catch (Exception e) {
            softAssertion.getSoftAssert().fail("Error in getting network calls positive");
            loggers.standardLogger().error("Error in getting network calls positive", e);
        }finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }
}
