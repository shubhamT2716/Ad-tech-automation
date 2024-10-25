package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Map;

@ContextConfiguration(classes = Loggers.class)
public class OpenUrl extends AbstractTestNGSpringContextTests {

    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void openUrl(Object object) {
        Map<String, Object> testData = (Map<String, Object>) object;
        browserRemote.openUrl(testData.get("url").toString());
    }
}
