package net.media.emma.utils;

import java.text.SimpleDateFormat;
import org.springframework.stereotype.Component;

@Component
public class CommanBrowserUtil {

	public static String getFileNameForHar() {

		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
		String random = String.valueOf((int) (Math.random() * 10000));
		return "generated-har-file-" + timeStamp + "-" + random + ".har";
	}
}
