package net.media.emma.coptests;

import net.media.emma.config.Loggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@Service
@ContextConfiguration(classes = Loggers.class)
public class AssertConsoleError extends AbstractTestNGSpringContextTests {

    @Autowired
    private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired
    private net.media.emma.config.Loggers loggers;
    @Autowired
    private net.media.emma.coptests.SoftAssertion softAssertion;

    @Test
    public void assertConsoleError() {
        try {
            softAssertion.softAssert();
            softAssertion.getSoftAssert().assertTrue(ConsoleError.hasError.size() == 0, "Console Error Found");
        } catch (Exception e) {
            softAssertion.getSoftAssert().assertAll();
            loggers.standardLogger().error("Console error fetching issue", e);
        } finally {
            softAssertion.getSoftAssert().assertAll();
        }
    }
}
