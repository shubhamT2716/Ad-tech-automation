package net.media.emma.cop.testsuites.yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;

@Component
@Slf4j
public class FileHandling {
	
	@Value("${reportFileBasePath}")
	private String reportFilePath;
	

	
	int dataIndexRow=1;
	public String setReportToFile(TestCriteriaBo testData , URLChecksReportModel reportModel , TestReportModel testModel) throws IOException
	{
		
//		response.setContentType("appication/octet-stream");
		
		String regex="(?:http[s]?:\\/\\/)?(?:www\\.)?([^\\/\\s]+)";
		
		Pattern pattern=Pattern.compile(regex);
		
		Matcher matcher=pattern.matcher(testData.getPublisherUrl());
		
		String domain=null;
		
		if(matcher.find())
		{
			 domain=matcher.group(1);
		}
		
//		String fileName="YAML_TEST_SUITE_REPORT.xls";
		String fileName=getFileNameForExcel(domain);
		String headerKey = "Content-Disposition";
//		String headerValue = "attachment;filename=data.xls";
		String headerValue = "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8");
		
		
//		response.setHeader(headerKey, headerValue);
		
//		Map<String, NegativeURLChecks> mapOfNegativeURLChecks=reportModel.getNegativeUrlChecks();
		
//		Map<String, PositiveURLChecks> mapOfPositiveURLChecks=reportModel.getPositiveUrlChecks();
		
//		XSSFWorkbook workbook=new XSSFWorkbook();
//		XSSFCellStyle cellStyle=workbook.createCellStyle();
//		
//		cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
//		
//		XSSFSheet sheet=workbook.createSheet("data1");
//		
//		XSSFRow publisherRow=sheet.createRow(0);
//		publisherRow.createCell(0).setCellValue("Publisher");
//		publisherRow.createCell(1).setCellValue(testData.getPublisherUrl());
//		
//		
//		dataIndexRow++;
//		
//		
//		XSSFRow bidexData=sheet.createRow(dataIndexRow);
//		bidexData.createCell(0).setCellValue("bidexOrClientagSize");
//		bidexData.createCell(1).setCellValue(testModel.getBidExController().getTotalSizeDownloaded());
//		
//		dataIndexRow++;
//		XSSFRow bidexSVr=sheet.createRow(dataIndexRow);
//		bidexSVr.createCell(0).setCellValue("SVR");
//		bidexSVr.createCell(1).setCellValue(testModel.getBidExController().getBidexData().getSvr());
//		
//		dataIndexRow++;
//		XSSFRow bidexCid=sheet.createRow(dataIndexRow);
//		bidexCid.createCell(0).setCellValue("Cid");
//		bidexCid.createCell(1).setCellValue(testModel.getBidExController().getBidexData().getCid());
//		
//		
//		dataIndexRow++;
//		XSSFRow ConsoleMessage=sheet.createRow(dataIndexRow);
//		ConsoleMessage.createCell(0).setCellValue("ConsoleMessage");
//		ConsoleMessage.createCell(1).setCellValue(testModel.getBidExController().getConsoleMessages().toString());
//		
//		
//		dataIndexRow++;
//		
//		
//		
////		bidexData.createCell(0).setCellValue(testModel.getBidExController().getTotalSizeDownloaded());
//		
//		
//		
//		XSSFRow row=sheet.createRow(dataIndexRow);
//		row.createCell(0).setCellValue("key");
//		row.createCell(1).setCellValue("label");
//		row.createCell(2).setCellValue("urlPattern");
//		row.createCell(3).setCellValue("testResult");
//		row.createCell(4).setCellValue("testMessages");
//		
//		
//		
//		mapOfNegativeURLChecks.entrySet().forEach(entry->{
//			
//			String key=entry.getKey();
//			
//			NegativeURLChecks negativeURLChecks=entry.getValue();
//			
//			XSSFRow dataRow=sheet.createRow(dataIndexRow);
//			
//			dataRow.createCell(0).setCellValue("negativeURLChecks");
//			dataRow.createCell(1).setCellValue(negativeURLChecks.getLabel());
//			dataRow.createCell(2).setCellValue(negativeURLChecks.getUrlPattern());
//			if(negativeURLChecks.getTestResult().toString().equalsIgnoreCase("PASS")||negativeURLChecks.getTestResult().toString().equalsIgnoreCase("FAIL"))
//			{
//				dataRow.createCell(3).setCellValue(negativeURLChecks.getTestResult().toString());
//				dataRow.setRowStyle(cellStyle);
//			}else {
//				dataRow.createCell(3).setCellValue(negativeURLChecks.getTestResult().toString());
//			}
//			
//			dataRow.createCell(4).setCellValue(negativeURLChecks.getMatchingUrls().toString());
//			dataIndexRow++;
//			
//			
//		});
//
//		// set index after negative URL checks
//		dataIndexRow++;
//		
//		mapOfPositiveURLChecks.entrySet().forEach(entry->{
//			
//			String key=entry.getKey();
//			
//			PositiveURLChecks positiveURLChecks=entry.getValue();
//			
//			XSSFRow dataRow=sheet.createRow(dataIndexRow);
//			dataRow.createCell(0).setCellValue("positiveURLChecks");
//			dataRow.createCell(1).setCellValue(key);
//			dataRow.createCell(2).setCellValue(positiveURLChecks.getUrlPattern());
//			dataRow.createCell(3).setCellValue(positiveURLChecks.getTestResult().toString());
//			dataRow.createCell(4).setCellValue(positiveURLChecks.getTestMessages().toString());
//			dataIndexRow++;
//			
//			
//		});
//		
//		
//		File file=new File(reportFilePath+fileName);
//		
////		ServletOutputStream outputStream=response.getOutputStream();
//		
//		FileOutputStream outputStream=new FileOutputStream(file);
//		
//		workbook.write(outputStream);
//		workbook.close();
//		outputStream.close();
//		
//		
//		return fileName;
		
		 // Create the Excel Workbook and Sheet
	    XSSFWorkbook workbook = new XSSFWorkbook();
	    XSSFSheet sheet = workbook.createSheet("data1");

	    // Initialize dataIndexRow to track row numbers
	    int dataIndexRow = 0;

	    // Create a row for publisher information
	    XSSFRow publisherRow = sheet.createRow(dataIndexRow++);
	    publisherRow.createCell(0).setCellValue("Publisher");
	    publisherRow.createCell(1).setCellValue(testData.getPublisherUrl());

	    // Add bidex data
	    XSSFRow bidexDataRow = sheet.createRow(dataIndexRow++);
	    bidexDataRow.createCell(0).setCellValue("bidexOrClientagSize");
	    bidexDataRow.createCell(1).setCellValue(testModel.getBidExController().getTotalSizeDownloaded());

	    XSSFRow bidexSvrRow = sheet.createRow(dataIndexRow++);
	    bidexSvrRow.createCell(0).setCellValue("SVR");
	    bidexSvrRow.createCell(1).setCellValue(testModel.getBidExController().getBidexData().getSvr());

	    XSSFRow bidexCidRow = sheet.createRow(dataIndexRow++);
	    bidexCidRow.createCell(0).setCellValue("Cid");
	    bidexCidRow.createCell(1).setCellValue(testModel.getBidExController().getBidexData().getCid());

	    XSSFRow consoleMessageRow = sheet.createRow(dataIndexRow++);
	    consoleMessageRow.createCell(0).setCellValue("ConsoleMessage");
	    consoleMessageRow.createCell(1).setCellValue(testModel.getBidExController().getConsoleMessages().toString());

	    // Add header for negative URL checks
	    XSSFRow negativeHeaderRow = sheet.createRow(dataIndexRow++);
	    negativeHeaderRow.createCell(0).setCellValue("key");
	    negativeHeaderRow.createCell(1).setCellValue("label");
	    negativeHeaderRow.createCell(2).setCellValue("urlPattern");
	    negativeHeaderRow.createCell(3).setCellValue("testResult");
	    negativeHeaderRow.createCell(4).setCellValue("testMessages");

	    // Add data for negative URL checks
	    Map<String, NegativeURLChecks> mapOfNegativeURLChecks = reportModel.getNegativeUrlChecks();
	    for (Map.Entry<String, NegativeURLChecks> entry : mapOfNegativeURLChecks.entrySet()) {
	        NegativeURLChecks negativeURLChecks = entry.getValue();

	        XSSFRow dataRow = sheet.createRow(dataIndexRow++);
	        dataRow.createCell(0).setCellValue("negativeURLChecks");
	        dataRow.createCell(1).setCellValue(negativeURLChecks.getLabel());
	        dataRow.createCell(2).setCellValue(negativeURLChecks.getUrlPattern());
	        dataRow.createCell(3).setCellValue(negativeURLChecks.getTestResult().toString());
	        dataRow.createCell(4).setCellValue(negativeURLChecks.getMatchingUrls().toString());
	    }

	    // Add a blank row between negative and positive URL checks
	    dataIndexRow++;

	    // Add header for positive URL checks
//	    XSSFRow positiveHeaderRow = sheet.createRow(dataIndexRow++);
//	    positiveHeaderRow.createCell(0).setCellValue("key");
//	    positiveHeaderRow.createCell(1).setCellValue("urlPattern");
//	    positiveHeaderRow.createCell(2).setCellValue("testResult");
//	    positiveHeaderRow.createCell(3).setCellValue("testMessages");

	    // Add data for positive URL checks
	    Map<String, PositiveURLChecks> mapOfPositiveURLChecks = reportModel.getPositiveUrlChecks();
	    for (Map.Entry<String, PositiveURLChecks> entry : mapOfPositiveURLChecks.entrySet()) {
	        PositiveURLChecks positiveURLChecks = entry.getValue();

	        XSSFRow dataRow = sheet.createRow(dataIndexRow++);
	        dataRow.createCell(0).setCellValue("positiveURLChecks");
	        dataRow.createCell(1).setCellValue(entry.getKey());
	        dataRow.createCell(2).setCellValue(positiveURLChecks.getUrlPattern());
	        dataRow.createCell(3).setCellValue(positiveURLChecks.getTestResult().toString());
	        dataRow.createCell(4).setCellValue(positiveURLChecks.getTestMessages().toString());
	    }

	    // Adjust column width to fit content
	    for (int i = 0; i < 5; i++) {
	        sheet.autoSizeColumn(i);
	    }

	    // Write the workbook to a file
	    File file = new File(reportFilePath + fileName);
	    try (FileOutputStream outputStream = new FileOutputStream(file)) {
	        workbook.write(outputStream);
	    }

	    // Close the workbook to release resources
	    workbook.close();

	    return fileName;

	}
	
	public String addSmokeReportInFile(Map<String, TestReportModel> report, List<TestReportModel> testReportList) throws IOException
	{
		
		XSSFWorkbook workbook=new XSSFWorkbook();
		XSSFSheet sheet=workbook.createSheet("data1");
		
		String fileName=getFileNameForExcel("smoke");

//		int dataRowIndex=0;
//		for(TestReportModel reportModel:testReportList)
//		{
//			
//			XSSFRow row=sheet.createRow(dataRowIndex);
//			int cellIndex=0;	
//			row.createCell(cellIndex).setCellValue("publisher");
//			row.createCell(1).setCellValue(reportModel.getTestMetaData().getPublisherUrl());
//			row.createCell(2).setCellValue("SVR");
//			row.createCell(3).setCellValue(reportModel.getBidExController().getBidexData().getSvr());	
//			dataRowIndex++;
//			
//		}
		
		
		XSSFRow row=sheet.createRow(0);
		row.createCell(0).setCellValue("Publisher");
		row.createCell(1).setCellValue("Svr");
		
		
		for(int i=0;i<testReportList.size();i++)
		{
			XSSFRow sheetRow=sheet.createRow(i+1);
			TestReportModel model=testReportList.get(i);
			
				
			sheetRow.createCell(0).setCellValue(model.getTestMetaData().getPublisherUrl());	
			sheetRow.createCell(1).setCellValue(model.getBidExController().getBidexData().getSvr());
			
		}
		
		File file=new File(reportFilePath+fileName);
		
		FileOutputStream fileOutputStream=new FileOutputStream(file);
		workbook.write(fileOutputStream);
		workbook.close();
		fileOutputStream.close();
				
		
		return null;
	}
	
	
	
	public static String getFileNameForExcel(String pubs ) {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return pubs+"_YAML_TEST_SUITE_REPORT_" + timeStamp + "-" + random + ".xls";
		// smoke test report
	}
	
	
	
}
