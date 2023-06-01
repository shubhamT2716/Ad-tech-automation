package net.media.emma.coptests;

import net.media.emma.reporting.ExtentReportListener;
import net.media.emma.utils.TestDataFromYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TestRunner {

    @Autowired
    private TestDataFromYaml testDataFromYaml;

    @Value("${yamlFilePath}")
    String yamlFilePath;

    public void runTests() {
        testDataFromYaml.setYamlFilePath(yamlFilePath);
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName("Cop Test Suite");

        XmlTest xmlTest = new XmlTest(xmlSuite);
        xmlTest.setName("Cop Test");
        List<XmlClass> classes = new ArrayList<>();
        classes.add(new XmlClass(ConfigureBrowser.class));
        classes.add(new XmlClass(MockRequest.class));
        classes.add(new XmlClass(MockResponse.class));
        classes.add(new XmlClass(NetworkCallsPositive.class));
        classes.add(new XmlClass(NetworkCallsNegative.class));
        classes.add(new XmlClass(ConsoleError.class));
        classes.add(new XmlClass(OpenUrl.class));
        classes.add(new XmlClass(AssertNetworkCalls.class));
        classes.add(new XmlClass(LogKeyParams.class));
        classes.add(new XmlClass(AssertConsoleError.class));
        xmlTest.setClasses(classes);

        TestNG testNG = new TestNG();
        testNG.setXmlSuites(Collections.singletonList(xmlSuite));
        testNG.addListener(new ExtentReportListener());
        testNG.run();

    }
}
