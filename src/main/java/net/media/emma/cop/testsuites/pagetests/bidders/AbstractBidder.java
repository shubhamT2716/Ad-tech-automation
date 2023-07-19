package net.media.emma.cop.testsuites.pagetests.bidders;

import java.util.Set;

public abstract class AbstractBidder {

   //abstract get ad unit path that retuns string
    public abstract String getAdUnitPath();

    //abstract get sizes that returns set of strings
    public abstract Set<String> getSizes();
    
}
