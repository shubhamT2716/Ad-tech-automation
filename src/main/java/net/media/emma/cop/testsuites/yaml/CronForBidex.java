package net.media.emma.cop.testsuites.yaml;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Slf4j
@Component
@EnableScheduling
public class CronForBidex {

	// List of strings "1", "2"
	List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
			"hbx-stage.media.net");
	List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js",
			"ss/nes/bidexl2.js");// , "catchpoint_content.js",
	// "topics.js");

	@Value("${bidexSizeFileBasePath}")
	private String bidexSizeFilePath;

	
//	@Scheduled(cron = "0 0 */1 * * *")// runs every one hour
	@Scheduled(cron = "0 */2 * * * *")
	public void getBidexSize() {
		Map<String, List<String>> listOfUrls = new HashMap<>();

//		List<String> hbUrls = Arrays.asList("https://www.nutritionix.com/brands/restaurant?mnet_test=1",
//				"https://time.com/?mnet_test=1", "https://www.webmd.com/?mnet_test=1");
		
		List<String> hbUrls = Arrays.asList("https://www.nutritionix.com/brands/restaurant?mnet_test=1");
//				"https://time.com/?mnet_test=1", "https://www.webmd.com/?mnet_test=1"

		listOfUrls.put("HB", hbUrls);

		Date d = new Date(System.currentTimeMillis());

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(d);

		int mainDate = calendar.get(Calendar.DATE);
		
		int month=calendar.get(Calendar.MONTH);
		
		int year=calendar.get(calendar.YEAR);
		
		String date=mainDate+"/"+month+"/"+year;
		
		
		try (Playwright playwright = Playwright.create()) {
			BrowserContext browser = playwright.chromium().launch(launchOptions("chrome")).newContext();

			listOfUrls.entrySet().forEach(entry -> {

				String key = entry.getKey();

				List<String> listUrls = entry.getValue();

				Map<String, Integer> listOfSize = new HashMap<>();

				Map<String, Map<String, Integer>> mapOflistofsize = new HashMap<>();

				for (String urls : listUrls) {

					String regEx = ".*/(" + String.join("|", bidExHosts) + ")/(" + String.join("|", bidExControlers)
							+ ").*";

					browser.onRequest(request -> {

						if (request.url().matches(regEx)) {
							int size = request.sizes().responseBodySize;

							System.err.println("new Size" + size);

//							String updatedSize=(size/1000)+"kb";

							listOfSize.put(urls, (size / 1000));

						}

					});
					
//					
//					browser.setDefaultNavigationTimeout(10000);
//					
//					browser.setDefaultTimeout(10000);
					

					Page page = browser.newPage();
					
					page.setDefaultNavigationTimeout(90000);
					
					
					

					page.navigate(urls);
					
					try {
						Thread.sleep(90000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
		

				mapOflistofsize.put(key, listOfSize);
				
				System.err.println(mapOflistofsize);

				File file = new File(bidexSizeFilePath + "bidex_size_report.xls");
				if (file.exists()) {
					try {

						
						System.err.println("In if conrdition ");
						
						
						FileInputStream fileInputStream = new FileInputStream(file);

						XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

						XSSFSheet sheet = workbook.getSheetAt(0);

						Iterator<Row> iterator = sheet.iterator();

						int rowNumber = 0;
						Row lastRow = null;
						
						
						
						System.err.println("get Sheet ");
						ExcelData excelData = new ExcelData();

						while (iterator.hasNext()) {
							Row row = iterator.next();

							if (rowNumber == 0) {
								rowNumber++;

								lastRow = row;
								

								System.err.println("get row:  "+rowNumber+" "+sheet.getLastRowNum());

								

							}
							
						
							
							System.err.println("after rowNumber");

							
							Iterator<Cell> cells = row.iterator();
							
							int cid = 0;
							
							
							System.err.println("In if conrdition upto cell  ");

							while (cells.hasNext()) {
								Cell cell = cells.next();

								

								
								
								switch (cid) {
								case 0:

//										 itype=cell.getStringCellValue();

									excelData.setItype(cell.getStringCellValue());

									break;

								case 1:

									excelData.setUrls(cell.getStringCellValue());

									break;

								case 2:
									excelData.setSize((int) cell.getNumericCellValue());

								default:
									break;
								}
								cid++;
							}	
								
								

//								}

								mapOflistofsize.entrySet().forEach(e -> {
									System.err.println("in checking condition"+e.getKey()+" "+e.getValue());
									
									System.err.println(excelData.getItype().equalsIgnoreCase(e.getKey()));
									
									if (excelData.getItype().equalsIgnoreCase(e.getKey())) {
										System.err.println(excelData.getItype().equalsIgnoreCase(e.getKey()));
										e.getValue().entrySet().forEach(liveData -> {

											String url = liveData.getKey();
											
											System.err.println(url);
											
											
											int urlSize = liveData.getValue();
											
											System.err.println(urlSize);
											
											System.out.println(excelData.getItype()+" : "+excelData.getUrls());
											
											System.out.println(url.startsWith(excelData.getUrls()));
											
											System.out.println(urlSize > excelData.getSize());
											
											if (url.startsWith(excelData.getUrls())) {
												if (urlSize > excelData.getSize()) {
													log.info("bidex size has increse to " + urlSize);
												}
											}

										});
									}
								});
								
								
								

							}
						
						System.err.println("excel data get:  "+excelData.getItype()+" : "+excelData.getSize()+excelData.getUrls());
						
						System.out.println("rowNumber: "+rowNumber+" sheet :  "+sheet.getLastRowNum());

//						if (rowNumber == sheet.getLastRowNum()) {
							
							
							System.err.println("check condition that rowNumber == sheet.getLastRowNum() "+rowNumber+" "+sheet.getLastRowNum());

							int rowNum = sheet.getLastRowNum() + 1;
							
							System.err.println("row Number : "+rowNum);

							Map<String, Integer> map = mapOflistofsize.get(key);

							for (Map.Entry<String, Integer> mapOfSize : map.entrySet()) {
								Row newRow = sheet.createRow(rowNum);

								newRow.createCell(0).setCellValue(key);
								newRow.createCell(1).setCellValue(mapOfSize.getKey());
								newRow.createCell(2).setCellValue(mapOfSize.getValue());
								newRow.createCell(3).setCellValue(date);
								rowNum++;
							}
							
							FileOutputStream fileOutputStream=new FileOutputStream(file);
							
							workbook.write(fileOutputStream);
							workbook.close();
							fileInputStream.close();
							fileOutputStream.close();
							
							
							
							
							System.out.println("After all done onlt checkpoint is pandding");


						

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					
					System.out.println("File in else condition");

					XSSFWorkbook workbook = new XSSFWorkbook();

					XSSFSheet sheet = workbook.createSheet("data1");
					
					
					
					
					
					

					mapOflistofsize.entrySet().forEach(map -> {

						String Key = map.getKey();

						Map<String, Integer> mSize = map.getValue();
						

						mSize.entrySet().forEach(sizes -> {

							int rowNumbers = 0;
							
							if(rowNumbers==0)
							{
								XSSFRow updateRow = sheet.createRow(rowNumbers);
								updateRow.createCell(0).setCellValue("Itype");
								updateRow.createCell(1).setCellValue("Urls");
								updateRow.createCell(2).setCellValue("Size");
								updateRow.createCell(3).setCellValue("Date");
							}
							
							rowNumbers++;
							
							
							XSSFRow row = sheet.createRow(rowNumbers);
							row.createCell(0).setCellValue(Key);

							row.createCell(1).setCellValue(sizes.getKey());
							row.createCell(2).setCellValue(sizes.getValue());
							row.createCell(3).setCellValue(date);
							rowNumbers++;

						});

					});

					File newFile = new File(bidexSizeFilePath + "bidex_size_report.xls");

					try {
						FileOutputStream fileOutputStream = new FileOutputStream(newFile);

						workbook.write(fileOutputStream);
						workbook.close();
						fileOutputStream.close();

					} catch (Exception e) {

						log.info("file error : " + e.getMessage());
						e.printStackTrace();
					}

				}

			});

		}

	}
	
	
	public void storeToFile()
	{
		
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	private class ExcelData {

		String itype;
		String urls;
		int size;

	}

	public LaunchOptions launchOptions(String channel) {

		LaunchOptions launchOptions = new LaunchOptions().setHeadless(true);

		launchOptions.setChannel(channel);

		return launchOptions;

	}

}
