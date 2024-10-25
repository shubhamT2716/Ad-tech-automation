package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

@Service
@ContextConfiguration(classes = Loggers.class)
public class ConsoleError extends AbstractTestNGSpringContextTests {

    static List<String> hasError;

    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired
    private net.media.emma.config.Loggers loggers;
    @Autowired
    private net.media.emma.coptests.SoftAssertion softAssertion;

    @Test(dataProvider = "datapo", dataProviderClass = TestDataFromYaml.class)
    public void verifyConsoleError(Object object) {
        try {
            hasError = browserRemote.verifyConsoleError("bidexchange");
        } catch (Exception e) {
            softAssertion.softAssert().fail("Unable to read console error");
            loggers.standardLogger().error("Unable to read console error", e);
        } finally {
            softAssertion.softAssert().assertAll();
        }
    }
}
