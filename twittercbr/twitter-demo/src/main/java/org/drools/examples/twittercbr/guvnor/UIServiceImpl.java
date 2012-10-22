package org.drools.examples.twittercbr.guvnor;

import org.drools.examples.twittercbr.model.UIService;

import twitter4j.Status;

public class UIServiceImpl
        implements
        UIService {

    private TwitterCBRFrame frame;

    public UIServiceImpl(TwitterCBRFrame frame) {
        this.frame = frame;
    }

    public void show(Status tweet) {
        frame.showTweet( "", tweet );
    }

    public void show(Status tweet,
                     String msg) {
        frame.showTweet( msg, tweet );
    }

    public void reportTweetCount(long timestamp,
                                 int count) {
        frame.reportTweetCount( timestamp, count );
    }

}
