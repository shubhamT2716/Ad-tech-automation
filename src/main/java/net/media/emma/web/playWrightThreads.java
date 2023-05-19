package net.media.emma.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.BrowserRemote;

@Slf4j
@Controller
public class playWrightThreads {

    @Autowired BrowserRemote browserRemote;

    @GetMapping("/playWrightThreads")
    public String openPlayWrightThreads() {
    
        return "playWrightThreads";
    }

    @PostMapping("/playWrightThreads")
    public String postPlayWrightThreads(@RequestParam String url1, @RequestParam String url2) {

        browserRemote.openMultiBrowser(url1, url2);

        log.info("opening two urls in async mode");
        return "playWrightThreads";
    }
    
}
