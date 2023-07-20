package net.media.emma.cop.testsuites.pagetests;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Slf4j
@NoArgsConstructor
public class Slots {

    String path;
    Set<Size> sizes;
    Map<String, List<String>> targetting;

    public Set<String> getSizes(){
        log.info("Set of sizes " + sizes);
        return sizes.stream().map(size -> size.getWidth() + "x" + size.getHeight()).collect(Collectors.toSet());
        // [ "300x250", "300x600"]
    }

    public String getAdUnitPath(){
        String adUnitPathTrimmed = "";
        adUnitPathTrimmed = path.substring(path.lastIndexOf("/") + 1).replace('"', ' ').trim();
        log.info("Trimmed path " + adUnitPathTrimmed);
        //pls add trim logic here
        return adUnitPathTrimmed;
    }

    public String getSlothPath(){
        this.path= path;
        return path;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Size{
        int width;
        int height;
    }



}



