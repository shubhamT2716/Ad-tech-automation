package net.media.emma.utils;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan
public class CommanBrowserUtil {

	@Value("${file.base_path}")
	private static String basePath;

	public static String getFileNameForHar() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return basePath + timeStamp + "-" + random + ".har";
	}

}
