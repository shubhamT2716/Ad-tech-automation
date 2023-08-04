package net.media.emma.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

@Component
public class CommonBrowserUtil {

	public static String getFileNameForHar() {

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return "generated-har-file-" + timeStamp + "-" + random + ".har";
	}

//	public static String getFileNameForZip(){
//		String timeStamp=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new Date());
//		String random=Sting 
//	
//	// smoke test report	
//	}

	public static void createZipFile(String rawData) {
		File file = new File("");
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
			ZipEntry entry = new ZipEntry("RawData.txt");
			zos.putNextEntry(entry);

			byte[] data = rawData.getBytes();

			zos.write(data, 0, data.length);

			zos.closeEntry();

			zos.close();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}
