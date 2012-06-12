package org.drools.examples.twittercbr.guvnor;

import java.text.MessageFormat;

import org.drools.examples.twittercbr.model.UIService;

import twitter4j.Status;

public class UIServiceImpl implements UIService {
    
    public void show( Status tweet )  {
        System.out.println( MessageFormat.format( "[{0,time,full}] ({3}) @{1} - {2}",
                                                  tweet.getCreatedAt(),
                                                  tweet.getUser().getScreenName(),
                                                  tweet.getText(),
                                                  tweet.getPlace() != null ? tweet.getPlace().getCountry() : "N.A." ) );
    }

}
