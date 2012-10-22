package org.drools.examples.twittercbr.model;

import twitter4j.Status;

public interface UIService {
    
    public void show( Status tweet );
    
    public void show( Status tweet, String msg );
    
    public void reportTweetCount( long timestamp, int count );

}
