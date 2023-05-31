package net.media.emma.impl.playwright.network;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import net.media.emma.service.BrowserRemote;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaywrightMockRequest {

    Page page;
    public PlaywrightMockRequest(Page page){
        this.page = page;
    }

    public boolean mockRequest(String urlPattern, String replaceHostName, Set<String> bidExHosts) {
        AtomicBoolean isReplaced = new AtomicBoolean(false);
        page.route("**/" + urlPattern + "*", route -> {

            String replacedURL = "";
            String originalURl = route.request().url();

            for (String currentHost : bidExHosts) {
                if (originalURl.contains(currentHost)) {
                    replacedURL = originalURl.replace(currentHost, replaceHostName);
                    isReplaced.set(true);
                    break;
                }
            }
            route.resume(new Route.ResumeOptions().setUrl(replacedURL));
        });
        return isReplaced.get();
    }
}
