package net.media.emma.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileOperations {

    @Autowired
    private net.media.emma.config.Loggers loggers;

    @Value("${yamlFilePath}")
    private String yamlFilePath;

    public void saveYamlFile(String fileContent) {
        try {
            Files.write(Paths.get(yamlFilePath), fileContent.getBytes());
        } catch (IOException e) {
            loggers.standardLogger().error("YAML Write Failed", e);
        }
    }
}
