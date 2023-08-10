package net.media.emma.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.TestReportModel;

@Component
@Slf4j
public class CommonBrowserUtil {

	public static String getFileNameForHar() {

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return "generated-har-file-" + timeStamp + "-" + random + ".har";
	}

	public static String getFileNameForZip() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return "smokeTest-report-" + timeStamp + "-" + random + ".zip";
		// smoke test report
	}

	public static String createZipFile(String zipFilePath, Map<String, TestReportModel> rawData) {

		String fileName = getFileNameForZip();

		File file = new File(zipFilePath + fileName);

		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
			ZipEntry entry = new ZipEntry("RawData.txt");
			zos.putNextEntry(entry);

			for (Map.Entry<String, TestReportModel> model : rawData.entrySet()) {

//				String dataRaw = model.getKey() + "-" + model.getValue().getTestMetaData() + " "
//						+ model.getValue().getBidExController() + " " + model.getValue().getNetworkAnalytics() + " "
//						+ model.getValue().getTestMetaData() + " " + model.getValue().getUrlChecks();

				String dataRaw = model.getKey() + " " + model.getValue().toString();

				log.info("dataRaw = " + dataRaw);

				byte[] data = dataRaw.getBytes();

				zos.write(data, 0, data.length);

			}
			zos.closeEntry();

			zos.close();

			return fileName;

		} catch (Exception e) {
			e.printStackTrace();

			return null;

		}

	}

	public static JsonNode readDataFromZip(String fileName) {

		try {
			ZipFile file = new ZipFile(fileName);

			ZipEntry entry = file.entries().nextElement();

			InputStream inputStream = file.getInputStream(entry);

			ObjectMapper mapper = new YAMLMapper();

			JsonNode model = mapper.convertValue(inputStream, JsonNode.class);

			return model;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}

	}

}
