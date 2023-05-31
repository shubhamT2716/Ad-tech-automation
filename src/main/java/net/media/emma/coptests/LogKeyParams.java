package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.ExtractKeyValueFromLogs;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ContextConfiguration(classes = Loggers.class)
public class LogKeyParams extends AbstractTestNGSpringContextTests {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired private net.media.emma.config.Loggers loggers;
    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    @Autowired
    AssertNetworkCalls assertNetworkCalls;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void verifyKeyParams(Object object) throws UnsupportedEncodingException {
        try {
            Map<String, Object> testData = (Map<String, Object>) object;
            HashMap<String, Map<String, List<String>>> logKeysToVerify = (HashMap<String, Map<String, List<String>>>) testData.get("keysToVerify");
            for (Map.Entry<String, List<String>> reqUrlsData : assertNetworkCalls.logsUrls.entrySet()) {
                if (logKeysToVerify.containsKey(reqUrlsData.getKey())) {
                    HashMap<String, List<String>> keyValPair = (HashMap<String, List<String>>) logKeysToVerify.get(reqUrlsData.getKey());
                    for (Map.Entry<String, List<String>> keyVal : keyValPair.entrySet()) {
                        List<String> values = ExtractKeyValueFromLogs.getMulValOfSameKeyFromDiffUrls(reqUrlsData.getValue(), keyVal.getKey());
                        for (int i = 0; i < keyVal.getValue().size(); i++) {
                            softAssertion.softAssert().assertTrue(values.contains(keyVal.getValue().get(i)), "Value " + keyVal.getValue().get(i) + " not found for key " + keyVal.getKey() + " <br> ");
                        }
                    }
                }
            }
        }catch (Exception e){
            loggers.standardLogger().error("Error in assertion of key  params -- ",e);
        }finally {
            softAssertion.softAssert().assertAll();
        }

    }

}
