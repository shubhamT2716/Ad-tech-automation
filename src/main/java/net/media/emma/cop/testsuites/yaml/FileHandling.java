package net.media.emma.cop.testsuites.yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;

@Component
public class FileHandling {
	
	@Value("${reportFileBasePath}")
	private String reportFilePath;
	

	
	int dataIndexRow=1;
	public String setReportToFile(TestCriteriaBo testData , URLChecksReportModel reportModel) throws IOException
	{
		
//		response.setContentType("appication/octet-stream");
		
		
		
//		String fileName="YAML_TEST_SUITE_REPORT.xls";
		String fileName=getFileNameForExcel();
		String headerKey = "Content-Disposition";
//		String headerValue = "attachment;filename=data.xls";
		String headerValue = "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8");
		
		
//		response.setHeader(headerKey, headerValue);
		
		Map<String, NegativeURLChecks> mapOfNegativeURLChecks=reportModel.getNegativeUrlChecks();
		
		Map<String, PositiveURLChecks> mapOfPositiveURLChecks=reportModel.getPositiveUrlChecks();
		
		XSSFWorkbook workbook=new XSSFWorkbook();
		XSSFSheet sheet=workbook.createSheet("data1");
		
		XSSFRow publisherRow=sheet.createRow(0);
		publisherRow.createCell(0).setCellValue("Publisher");
		publisherRow.createCell(1).setCellValue(testData.getPublisherUrl());
		
		
		dataIndexRow++;
		
		
		XSSFRow row=sheet.createRow(dataIndexRow);
		row.createCell(0).setCellValue("key");
		row.createCell(1).setCellValue("label");
		row.createCell(2).setCellValue("urlPattern");
		row.createCell(3).setCellValue("testResult");
		row.createCell(4).setCellValue("testMessages");
		
		
		
		mapOfNegativeURLChecks.entrySet().forEach(entry->{
			
			String key=entry.getKey();
			
			NegativeURLChecks negativeURLChecks=entry.getValue();
			
			XSSFRow dataRow=sheet.createRow(dataIndexRow);
			dataRow.createCell(0).setCellValue("negativeURLChecks");
			dataRow.createCell(1).setCellValue(negativeURLChecks.getLabel());
			dataRow.createCell(2).setCellValue(negativeURLChecks.getUrlPattern());
			dataRow.createCell(3).setCellValue(negativeURLChecks.getTestResult().toString());
			dataRow.createCell(4).setCellValue(negativeURLChecks.getMatchingUrls().toString());
			dataIndexRow++;
			
			
		});

		// set index after negative URL checks
		dataIndexRow++;
		
		mapOfPositiveURLChecks.entrySet().forEach(entry->{
			
			String key=entry.getKey();
			
			PositiveURLChecks positiveURLChecks=entry.getValue();
			
			XSSFRow dataRow=sheet.createRow(dataIndexRow);
			dataRow.createCell(0).setCellValue("positiveURLChecks");
			dataRow.createCell(1).setCellValue(positiveURLChecks.getLabel());
			dataRow.createCell(2).setCellValue(positiveURLChecks.getUrlPattern());
			dataRow.createCell(3).setCellValue(positiveURLChecks.getTestResult().toString());
			dataRow.createCell(4).setCellValue(positiveURLChecks.getTestMessages().toString());
			dataIndexRow++;
			
			
		});
		
		
		File file=new File(reportFilePath+fileName);
		
//		ServletOutputStream outputStream=response.getOutputStream();
		
		FileOutputStream outputStream=new FileOutputStream(file);
		
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
		
		
		return fileName;

	}
	
	public static String getFileNameForExcel() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return "YAML_TEST_SUITE_REPORT" + timeStamp + "-" + random + ".xls";
		// smoke test report
	}
	
	
}
