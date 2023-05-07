package net.media.emma.utils;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class HARParser {

    @Autowired
    protected Logger logger;

    public String getCreatorName(){

        HarReader harReader = new HarReader();
        
        try {
            Har har;
            har = harReader.readFromFile(new File("myhar.har"));
            return har.getLog().getCreator().getName();
        } catch (HarReaderException e) {
        
            e.printStackTrace();
            logger.atLevel(Level.INFO).addArgument(e.getMessage()).log(e.getMessage());
            
        }

        return "Error";
    }   
}