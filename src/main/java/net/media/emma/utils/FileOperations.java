package net.media.emma.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

	public Set<String> listFilesUsingFilesList(String dir) {

		try {

			try (Stream<Path> stream = Files.list(Paths.get(dir))) {
				return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName).map(Path::toString)
						.collect(Collectors.toSet());
			}
		} catch (IOException e) {
			loggers.standardLogger().error("YAML List Failed", e);
			return new java.util.HashSet<String>();
		}
	}

	// loadFileAsResource
	public Resource loadFileAsResource(String fileName) {

		try {
			Path filePath = Paths.get(fileName);
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			}
		} catch (Exception e) {
			log.error("File read error: " + fileName, e);
		}

		return null;
	}
}
