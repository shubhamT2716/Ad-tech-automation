package net.media.emma.coptests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.options.Cookie;
import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ContextConfiguration(classes = Loggers.class)
public class NetworkCalls extends AbstractTestNGSpringContextTests {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void test1(Object object) {

        try {
            Map<String, Object> testData = (Map<String, Object>) object;
            System.out.println(testData.keySet());
            String url = testData.get("urls").toString();
            String userAgent = testData.get("userAgent").toString();
            Map<String, String> cookies = (Map<String, String>) testData.get("cookies");
            HashMap<String, String> mnetNetworkCalls = (HashMap<String, String>) testData.get("mnetNetworkCalls");
            HashMap<String, String> mnetNetworkCallsNegative = (HashMap<String, String>) testData.get("mnetNetworkCallsNegative");
            HashMap<String, Map<String, List<String>>> logKeysToVerify = (HashMap<String, Map<String, List<String>>>) testData.get("keysToVerify");
            HashMap<String, String> responseSimulate = new HashMap<>();
            if (testData.get("responseSimulate") != null) {
                responseSimulate = (HashMap<String, String>) testData.get("responseSimulate");
            }
            String env = testData.get("environment").toString();
            List<Cookie> cookieList = new ArrayList<>();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Cookie cookie = new Cookie(key, value);
                cookie.setUrl(url);
                cookieList.add(cookie);
            }
           browserRemote.openUrl(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
