package net.media.emma.cop.testsuites.yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Color;
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
		
		
		
//		String fileName="YAML_TEST_SUITE_REPORT.xls";
		String fileName=getFileNameForExcel();
		String headerKey = "Content-Disposition";
//		String headerValue = "attachment;filename=data.xls";
		String headerValue = "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8");
		
		
//		response.setHeader(headerKey, headerValue);
		
		Map<String, NegativeURLChecks> mapOfNegativeURLChecks=reportModel.getNegativeUrlChecks();
		
		Map<String, PositiveURLChecks> mapOfPositiveURLChecks=reportModel.getPositiveUrlChecks();
		
		XSSFWorkbook workbook=new XSSFWorkbook();
		XSSFCellStyle cellStyle=workbook.createCellStyle();
		
		cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
		
		XSSFSheet sheet=workbook.createSheet("data1");
		
		XSSFRow publisherRow=sheet.createRow(0);
		publisherRow.createCell(0).setCellValue("Publisher");
		publisherRow.createCell(1).setCellValue(testData.getPublisherUrl());
		
		
		dataIndexRow++;
		
		
		XSSFRow bidexData=sheet.createRow(dataIndexRow);
		bidexData.createCell(0).setCellValue("bidexOrClientagSize");
		bidexData.createCell(1).setCellValue(testModel.getBidExController().getTotalSizeDownloaded());
		
		dataIndexRow++;
		XSSFRow bidexSVr=sheet.createRow(dataIndexRow);
		bidexSVr.createCell(0).setCellValue("SVR");
		bidexSVr.createCell(1).setCellValue(testModel.getBidExController().getBidexData().getSvr());
		
		dataIndexRow++;
		XSSFRow bidexCid=sheet.createRow(dataIndexRow);
		bidexCid.createCell(0).setCellValue("Cid");
		bidexCid.createCell(1).setCellValue(testModel.getBidExController().getBidexData().getCid());
		
		
		dataIndexRow++;
		XSSFRow ConsoleMessage=sheet.createRow(dataIndexRow);
		ConsoleMessage.createCell(0).setCellValue("ConsoleMessage");
		ConsoleMessage.createCell(1).setCellValue(testModel.getBidExController().getConsoleMessages().toString());
		
		
		dataIndexRow++;
		
		
		
//		bidexData.createCell(0).setCellValue(testModel.getBidExController().getTotalSizeDownloaded());
		
		
		
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
			if(negativeURLChecks.getTestResult().toString().equalsIgnoreCase("PASS")||negativeURLChecks.getTestResult().toString().equalsIgnoreCase("FAIL"))
			{
				dataRow.createCell(3).setCellValue(negativeURLChecks.getTestResult().toString());
				dataRow.setRowStyle(cellStyle);
			}else {
				dataRow.createCell(3).setCellValue(negativeURLChecks.getTestResult().toString());
			}
			
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
			dataRow.createCell(1).setCellValue(key);
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
	
	public String addSmokeReportInFile(Map<String, TestReportModel> report, List<TestReportModel> testReportList)
	{
		
		XSSFWorkbook workbook=new XSSFWorkbook();
		XSSFSheet sheet=workbook.createSheet("data1");
		
		

		for(TestReportModel reportModel:testReportList)
		{
			XSSFRow row=sheet.createRow(0);		
			
		}
		
		return null;
	}
	
	
	
	public static String getFileNameForExcel() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return "YAML_TEST_SUITE_REPORT" + timeStamp + "-" + random + ".xls";
		// smoke test report
	}
	
	
	
}
