package net.media.emma.web;

import net.media.emma.coptests.TestRunner;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
public class ExecuteTests {

    private final FileOperations fileOperations;

    @Autowired
    public ExecuteTests(FileOperations fileOperations) {
        this.fileOperations = fileOperations;
    }

    @Autowired
    private TestRunner testRunner;

    @GetMapping("/testDataInput")
    public void executeTest() {
    }

    @PostMapping("/testDataInput")
    public String executeTest(String yaml) throws IOException {
        fileOperations.saveYamlFile(yaml);
        testRunner.runTests();
        return "report";
    }
}
