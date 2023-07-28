package net.media.emma.cop.components;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.HarMode;

public class BrowserFactory {

	private Playwright playwright = Playwright.create();

	// Map of Browser Type with Device and User Agents
	public static Map<String, String> browserContexts = new HashMap<String, String>() {
		{
			put("Chrome.Mobile.iPhone",
					"Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/114.0.5735.50 Mobile/15E148 Safari/604.1");
			put("Chrome.Desktop.Mac",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
			put("Firefox.Desktop.Mac",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0");
			put("Chrome.Desktop.Catchpoint",
					"Mozilla/5.0 (X11; Linux x86_64; Catchpoint) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
			put("Safari.Mobile.google-bot",
					"Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/W.X.Y.Z Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
			put("Webkit.Tablet.iPad",
					"Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148");
			put("rubbish", "Googlebot/2.1 (+http://www.google.com/bot.html)");
			put("Chrome.Desktop.hw","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
			put("Chrome.Desktop.newhw","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36");
			put("Chrome.Desktop.lw","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
			put("Chrome.Mobile.hw","Mozilla/5.0 (Linux; Android 11; Pixel 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.96 Mobile Safari/537.36");
			put("Chrome.Mobile.newhw","Mozilla/5.0 (Linux; Android 13; SM-G930F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Mobile Safari/537.36");
			put("Chrome.Mobile.lw","Mozilla/5.0 (Linux; Android 7.1.2; Sony Xperia 5 IV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");
			put("Firefox.Desktop.hw","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:62.0) Gecko/20100101 Firefox/62.0");
			put("Firefox.Desktop.newhw","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0");
			put("Firefox.Desktop.lw","Mozilla/5.0 (Macintosh; Intel Mac OS X 13_0; rv:115.0) Gecko/20100101 Firefox/115.0");
			put("Safari.Desktop.hw","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Version/7.0 Safari/537.36");
			put("Safari.Desktop.newhw","Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/605.1.15");
			put("Safari.Desktop.lw","Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1");
		}
	};

	private static NewContextOptions getCommonBrowserContext(String userAgent, Boolean isMobile, String harFileName) {

		NewContextOptions newContextOptions = new NewContextOptions().setUserAgent(userAgent)
				.setRecordHarPath(Paths.get(harFileName)).setRecordHarMode(HarMode.FULL).setIgnoreHTTPSErrors(true);
		// .setStorageStatePath(Paths.get("emma-working-directory/browser-context-storage/state.json"));

		if (isMobile) {
			newContextOptions.setViewportSize(360, 640).setIsMobile(true).setHasTouch(true);
		}

		return newContextOptions;
	}

	private static BrowserType.LaunchPersistentContextOptions getSystemBrowserContextOptions(String userAgent,
			Boolean isMobile, List<String> extension, String filename) {

		BrowserType.LaunchPersistentContextOptions newContextOptions = new BrowserType.LaunchPersistentContextOptions()
				.setUserAgent(userAgent).setRecordHarPath(Paths.get(filename)).setRecordHarMode(HarMode.FULL)
				.setIgnoreHTTPSErrors(true).setHeadless(false).setArgs(extension);

		if (isMobile) {
			newContextOptions.setViewportSize(360, 640).setIsMobile(true).setHasTouch(true);
		}

		return newContextOptions;
	}

	private static LaunchOptions getBrowserLaunchOptions(String channel) {
		LaunchOptions lo = new LaunchOptions().setHeadless(false);

		if (channel.length() > 0)
			lo.setChannel(channel);

		return lo;
	}

	private static Playwright getPlaywrightObj() {
		// return playwright;
		return Playwright.create();
	}

	private static Browser getBrowserByType(String browserType) {

		if (browserType.contains("Chrome")) {
			return getPlaywrightObj().chromium().launch(getBrowserLaunchOptions("chrome"));
		}

		if (browserType.contains("Firefox")) {
			return getPlaywrightObj().firefox().launch();
		}

		if (browserType.contains("Edge")) {
			return getPlaywrightObj().chromium().launch(getBrowserLaunchOptions("msedge"));
		}

		if(browserType.contains("Safari")){ //webkit is equal to safari (as per playwright documentation
			return getPlaywrightObj().webkit().launch(getBrowserLaunchOptions(""));
		}

		return getPlaywrightObj().webkit().launch(); 
	}

	private static BrowserContext getSystemBrowserByType(String browserType, List<Map<String, String>> cookies,
			List<String> extension, String fileName, String browserPath) {
		if (browserType.contains("Chrome")) {
			return getPlaywrightObj().chromium().launchPersistentContext(Paths.get(browserPath),
					getSystemBrowserContextOptions(browserContexts.get(browserType), browserType.contains("Mobile"),
							extension, fileName));
		}

		return getPlaywrightObj().webkit().launchPersistentContext(Paths.get(browserPath));
	}

	// Returns Playwright's Page obj with given browserType and cookies
	// @Bean(name = "getPageWithNewContext")
	// @Scope("prototype")
	public static Page getPageWithNewContext(String browserType, List<Map<String, String>> cookies,
			String harFileName) {

		BrowserContext newBc = getBrowserByType(browserType).newContext(
				getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile"), harFileName));

//		BrowserContext.StorageStateOptions storageStateOptions = new BrowserContext.StorageStateOptions();
//		newBc.storageState(
//				storageStateOptions.setPath(Paths.get("emma-working-directory/browser-context-storage/state.json")));

		newBc.setDefaultNavigationTimeout(120000);
		// .addCookies(cookies)
		return newBc.newPage();
	}

	public static BrowserContext getBc(String browserType, List<Map<String, String>> cookies) {

		return getBc(browserType, cookies, null);

	}

	public static BrowserContext getBc(String browserType, List<Map<String, String>> cookies, String harFileName) {

		BrowserContext newBc = getBrowserByType(browserType).newContext(
				getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile"), harFileName));

		BrowserContext.StorageStateOptions storageStateOptions = new BrowserContext.StorageStateOptions();
		newBc.storageState(
				storageStateOptions.setPath(Paths.get("emma-working-directory/browser-context-storage/state.json")));

		newBc.setDefaultNavigationTimeout(120000);

		return newBc;
	}

	public static BrowserContext getBcWithUserAgent(String browserType, String userAgent, List<Map<String, String>> cookies, String harFileName) {
		BrowserContext newBc =  getBrowserByType(browserType).newContext(
				getCommonBrowserContext(userAgent, browserType.contains("Mobile"), harFileName));
		return newBc;
	}

	public static BrowserContext getSystemBc(String browserType, List<Map<String, String>> cookies,
			List<String> extension, String fileName, String browserPath) {
		BrowserContext newBc = getSystemBrowserByType(browserType, cookies, extension, fileName, browserPath);
		return newBc;
	}

	public static BrowserContext getNewBrowserContext(String browserType, List<Map<String, String>> cookies) {

		BrowserContext newBc = getBrowserByType(browserType).newContext(
				getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile"), null));
		newBc.setDefaultNavigationTimeout(120000);
		// .addCookies(cookies)
		return newBc;
	}
}