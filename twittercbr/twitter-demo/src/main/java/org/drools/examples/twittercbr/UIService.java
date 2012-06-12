package org.drools.examples.twittercbr;

import java.text.MessageFormat;

import twitter4j.Status;

public class UIService {
    
    public void show( Status tweet ) {
        System.out.println( MessageFormat.format( "[{0,time,full}] ({3}) @{1} - {2}",
                                                  tweet.getCreatedAt(),
                                                  tweet.getUser().getScreenName(),
                                                  tweet.getText(),
                                                  tweet.getPlace().getCountry() ) );
    }

}
