package net.media.emma.coptests;

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
        classes.add(new XmlClass(NetworkCalls.class));
      //  classes.add(new XmlClass(NC.class)); // Add the new class here
        xmlTest.setClasses(classes);

        TestNG testNG = new TestNG();
        testNG.setXmlSuites(Collections.singletonList(xmlSuite));
        testNG.run();

    }
}
