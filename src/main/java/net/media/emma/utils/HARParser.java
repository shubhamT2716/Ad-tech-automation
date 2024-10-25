package net.media.emma.utils;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class HARParser {

    @Autowired
    protected Logger logger;

    public String getCreatorName(String harFileName) {

        HarReader harReader = new HarReader();

        try {
            Har har;
            har = harReader.readFromFile(new File(harFileName));
            return har.getLog().getCreator().getName();
        } catch (HarReaderException e) {

            e.printStackTrace();
            logger.atLevel(Level.INFO).addArgument(e.getMessage()).log(e.getMessage());

        }

        return "Error";
    }
}