package net.media.emma.cop.testsuites.pagetests.bidders;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
public abstract class AbstractBidder {

    String bidderCallFormat;
    JsonNode yhbPostData;

    //abstract get ad unit path that retuns string
    public abstract String getAdUnitPath();

    //abstract get sizes that returns set of strings
    public abstract Set<String> getSizes();

}
