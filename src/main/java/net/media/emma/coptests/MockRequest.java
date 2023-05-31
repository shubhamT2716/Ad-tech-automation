package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@ContextConfiguration(classes = Loggers.class)
public class MockRequest extends AbstractTestNGSpringContextTests {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired private net.media.emma.config.Loggers loggers;
    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    static Set<String> bidExHosts = new HashSet<>(Arrays.asList(new String[]{"hbx.media.net", "contextual.media.net", "warp.media.net"}));
    static Set<String> bidExControllers = new HashSet<>(Arrays.asList(new String[]{"bidexchange.js", "clienttag.js"}));


    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void mockRequestt(Object object) {
        try {
            Map<String, Object> testData = (Map<String, Object>) object;
            if (!testData.get("environment").toString().isEmpty()) {
                for (String combinedUrl : combineTwoSets()) { //pass single url in mock request method if url replaced will break the for loop
                    if (this.browserRemote.mockRequest(combinedUrl, testData.get("environment").toString(), bidExHosts))
                        break;
                }
            }
        }catch (Exception e) {
            softAssertion.softAssert.fail("Unable to Mock Request");
            loggers.standardLogger().error("Unable to Mock Request", e);
        }finally {
            softAssertion.softAssert.assertAll();
        }
    }

    private Set<String> combineTwoSets() { //return combined set of two Set for bidexchange detection controller
        Set<String> urls = new HashSet<>();
        for (String bidExHost : bidExHosts) {
            for (String bidExController : bidExControllers) {
                urls.add(bidExHost + "/" + bidExController);
            }
        }
        return urls;
    }
}
