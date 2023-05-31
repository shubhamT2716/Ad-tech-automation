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

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;
    @Autowired private net.media.emma.config.Loggers loggers;
    @Autowired private net.media.emma.coptests.SoftAssertion softAssertion;

    @Test
    public void assertConsoleError() {
        try {
            softAssertion.softAssert.assertTrue(ConsoleError.hasError.size() == 0);
            softAssertion.softAssert.assertAll();
        }catch (Exception e){
            loggers.standardLogger().error("Console error fetching issue", e);
        }finally {
            softAssertion.softAssert.assertAll();
        }
    }
}
