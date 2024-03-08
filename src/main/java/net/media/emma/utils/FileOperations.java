package net.media.emma.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.fasterxml.jackson.core.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileOperations {

	@Autowired
	private net.media.emma.config.Loggers loggers;

	@Value("${yamlFilePath}")
	private String yamlFilePath;
	
	@Value("${commonYamlFilePath}")
	private String commonYaml;
	
	@Value("${featureTestFilePath}")
	private String featureYaml;
	
	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String yamlSmokeTestSuiteFilesPath;
	

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


	public Set<String> listYamlFiles(String dir)
	{
		Set<String> yamlFiles = new HashSet<String>();
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources(dir + "/*.yaml");
			for (Resource resource : resources) {
				yamlFiles.add(resource.getFilename());
			}
		} catch (IOException e) {
			loggers.standardLogger().error("YAML List Failed", e);
		}

		return yamlFiles;
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

	public String readFileAsString(String fileName) {
		try {
			return new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (IOException e) {
			loggers.standardLogger().error("YAML Read Failed", e);
			return null;
		}
	}

    public InputStream readFileAsInputStream(String string) {
        try {

			//open file in read only mode
			return Files.newInputStream(Paths.get(string), java.nio.file.StandardOpenOption.READ);

		} catch (IOException e) {
			loggers.standardLogger().error("YAML Read Failed", e);
			return null;
		}
    }

	public String mergeYaml(String yamlFileWithPath, String commonYamlFile)
	{
		
		System.err.println(commonYamlFile);
		
		
		
		Yaml yaml=new Yaml(new Constructor(Map.class));

		try(FileReader fileReader=new FileReader(yamlFileWithPath);
				FileReader fileReader2=new FileReader("src/main/resources/commonYaml/common.yaml"))
		{
			Map<String,Object> data1=yaml.load(fileReader);
			
			System.err.println("data1"+data1);
			Map<String,Object> data2=yaml.load(fileReader2);
			
			System.err.println(data1);
			System.err.println(data2);
				

			data1.putAll(data2);

			return yaml.dump(data1);
			
		}
		catch(Exception e)
		{
			log.error("YAML Merge Failed", e);
			return null;
		}
		
	}
	
	public String getCommonFile(String commonFile)
	{
		ClassPathResource commonYamlClassPath=new ClassPathResource(commonYaml+"/"+commonFile);
		
		 String commonContent=null;
		 try {
			 InputStream stream=commonYamlClassPath.getInputStream();
			 
			 commonContent=StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
			 
			 return commonContent;
		 }
		 catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
		
		
	}
	
	
	public String getFeatureFiles(String featureFiles)
	{
		ClassPathResource commonYamlClassPath=new ClassPathResource(featureYaml+"/"+featureFiles);
		
		 String commonContent=null;
		 try {
			 InputStream stream=commonYamlClassPath.getInputStream();
			 
			 commonContent=StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
			 
			 return commonContent;
		 }
		 catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
		
		
	}
	
	
	
	public String getYamlFileContent(String commonFile)
	{
		ClassPathResource commonYamlClassPath=new ClassPathResource(yamlSmokeTestSuiteFilesPath+"/"+commonFile);
		
		 String commonContent=null;
		 try {
			 InputStream stream=commonYamlClassPath.getInputStream();
			 
			 commonContent=StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
			 
			 return commonContent;
		 }
		 catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
		
		
	}

	
	
}
