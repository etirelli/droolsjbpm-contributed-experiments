package org.drools.examples.twittercbr;

import java.util.concurrent.TimeUnit;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionPseudoClock;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TwitterStatusListener
        implements
        StatusListener {

    // Drools Fusion entry point
    private WorkingMemoryEntryPoint  ep;
    private SessionPseudoClock       clock;
    private StatefulKnowledgeSession ksession;

    /**
     * Default constructor. Stores the session entry point.
     * @param ep2 
     */
    public TwitterStatusListener(StatefulKnowledgeSession ksession,
                                 WorkingMemoryEntryPoint ep) {
        this.ep = ep;
        this.ksession = ksession;
        this.clock = ksession.getSessionClock();
    }

    /**
     * Whenever a new message (Status) is twitted, it is
     * inserted into the session entry point.
     */
    public void onStatus(Status status) {
        // Using the pseudo clock, advance the clock
        clock.advanceTime( status.getCreatedAt().getTime() - clock.getCurrentTime(), TimeUnit.MILLISECONDS );
        ep.insert( status );
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    }

    public void onScrubGeo(long userId,
                           long upToStatusId) {
    }

    public void onException(Exception ex) {
    }
}