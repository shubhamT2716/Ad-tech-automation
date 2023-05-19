package net.media.emma.web;

import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ExecuteTests {

    private final FileOperations fileOperations;

    @Autowired
    public ExecuteTests(FileOperations fileOperations) {
        this.fileOperations = fileOperations;
    }

    @GetMapping("/testDataInput")
    public void printDemo2() {
    }

    @PostMapping("/testDataInput")
    public String printDemoPos2t(String yaml) {
        fileOperations.saveYamlFile(yaml);
        return "report";
    }
}
