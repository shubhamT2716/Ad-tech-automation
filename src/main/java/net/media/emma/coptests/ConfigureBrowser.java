package net.media.emma.coptests;

import com.microsoft.playwright.options.Cookie;
import lombok.SneakyThrows;
import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ContextConfiguration(classes = Loggers.class)
public class ConfigureBrowser extends AbstractTestNGSpringContextTests {

    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;

    @Autowired private net.media.emma.config.Loggers loggers;

    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml .class)
    public void configureBrowser(Object object) {
        try {
            softAssertion.softAssert();
            Map<String, Object> testData = (Map<String, Object>) object;
            browserRemote.createPlaywrightInstance();
            browserRemote.initializeBrowser(testData.get("userAgent").toString());
            browserRemote.launchBrowser(testData.get("userAgent").toString());
            Map<String, String> cookies = (Map<String, String>) testData.get("cookies");
            List<Cookie> cookieList = new ArrayList<>();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Cookie cookie = new Cookie(key, value);
                cookie.setUrl(testData.get("url").toString());
                cookieList.add(cookie);
            }
            browserRemote.setBrowserContext(testData.get("userAgent").toString(), cookieList);
            browserRemote.initiatePage();
        } catch (Exception e) {
            softAssertion.getSoftAssert().fail("Unable to Configure Browser");
            loggers.standardLogger().error("Unable to Configure Browser", e);
        }finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }
}
