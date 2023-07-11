package net.media.emma.cop.testsuites.pagetests;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class SlotsDataUtil {

    private List<SlotsData> slots;

    @Getter @Setter @Builder
    public static class SlotsData {

        String adUnitPath;
        Set<Size> sizes;
        Map<String, List<String>> targetting;

        public Set<String> getSetOfSizes(){

           return sizes.stream().map(size -> size.getWidth() + "x" + size.getHeight()).collect(Collectors.toSet());
        }

    }

    @Getter @Setter
    public static class Size{
        int width;
        int height;
    }
}