/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.twittercbr.guvnor;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.ClockType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.examples.twittercbr.TwitterStatusListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * TwitterCBR
 */
public class TwitterCBRGuvnor {
    public static final boolean disableLog = true;
    private final TwitterCBRFrame frame;
    private StatefulKnowledgeSession ksession;
    private EventFeeder feeder;
    
    public static enum Mode {
        ONLINE, OFFLINE;
    }
    public static enum RulesSource {
        GUVNOR, ECLIPSE;
    }

    /**
     * Main method
     */
    public static void main(String[] args) throws TwitterException, IOException{
        TwitterCBRGuvnor app = new TwitterCBRGuvnor();
        app.start();
    }
    
    public TwitterCBRGuvnor() {
        frame = new TwitterCBRFrame( this );
    }
    
    public void start() {
        frame.show();
    }
    
    public void startEngine( Mode mode, RulesSource source ) {
        // Creates a knowledge base
        final KnowledgeBase kbase = createKnowledgeBase( source );
        ksession = createKnowledgeSession( kbase );
        ksession.setGlobal( "uiservice", new UIServiceImpl( frame ) );

        // Gets the stream entry point 
        final WorkingMemoryEntryPoint ep = ksession.getWorkingMemoryEntryPoint( "twitter" );
        
        // reads the status stream and feeds it to the engine 
        startFeed( ksession,
                   ep,
                   mode );
        
        // starts the engine
        startEngine();
    }

    private void startEngine() {
        new Thread( new Runnable() {
            @Override
            public void run() {
                try { 
                    frame.log( "Starting Engine" );
                    // Starts to fire rules in Drools Fusion
                    ksession.fireUntilHalt();
                    frame.log( "Stopping Engine");
                } catch( Exception e ) {
                    e.printStackTrace();
                    System.exit( 0 );
                }
            }
        }, "Engine Thread").start();
    }
    
    public void stopEngine() {
        if( feeder != null ) {
            feeder.stopFeed();
            feeder = null;
        }
        if( ksession != null ) {
            ksession.halt();
            ksession = null;
        }
    }

    /**
     * Feed events from the stream to the engine
     * @param ksession
     * @param ep
     */
    private void startFeed( final StatefulKnowledgeSession ksession,
                                    final WorkingMemoryEntryPoint ep,
                                    final Mode mode ) {
        StatusListener listener = new TwitterStatusListener( ksession, ep );
        switch( mode ) {
            case OFFLINE: {
                feeder = new OfflineEventFeeder( frame, listener );
                feeder.startFeed();
                break;
            }
            case ONLINE: {
                feeder = new OnlineEventFeeder( frame, listener );
                feeder.startFeed();
                break;
            }
        }
    }

    /**
     * Creates a Drools KnowledgeBase and adds the given rules file into it
     * @param source 
     */
    private KnowledgeBase createKnowledgeBase(RulesSource source) {
        // Configures the Stream mode
        KnowledgeBaseConfiguration kbconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        kbconf.setOption( EventProcessingOption.STREAM );
        
        // Creates a prototype kbase
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase( kbconf );
        
        // sets authentication credentials
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "admin".toCharArray());
            }
        });        
        
        // Creates the agent and loads the changeset from Guvnor
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        switch( source ) {
            case GUVNOR:
                kbuilder.add( ResourceFactory.newUrlResource( "http://localhost:8080/guvnor-5.4.0.Final-jboss-as-7.0/org.drools.guvnor.Guvnor/package/demo.twitter.pkg1/LATEST/ChangeSet.xml" ), 
                              ResourceType.CHANGE_SET );
                frame.log( "Loading rules from Guvnor...");
                break;
            case ECLIPSE:
                kbuilder.add(  ResourceFactory.newClassPathResource( "guvnor/twitterConversation.drl" ), ResourceType.DRL );
                frame.log( "Loading rules from Eclipse...");
                break;
        }
        
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        for( KnowledgePackage kpkg : kbase.getKnowledgePackages() ) {
            for( Rule rule : kpkg.getRules() ) {
                frame.log( "  - loading rule \""+rule.getName()+"\"" );
            }
        }
        
        
        
        return kbase;
    }

    /**
     * Creates a Drools Stateful Knowledge Session
     */
    private StatefulKnowledgeSession createKnowledgeSession( final KnowledgeBase kbase ) {
        final KnowledgeSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() ) );
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession( ksconf, null );
        return ksession;
    }

    static {
        // disable twitter4j log
        System.setProperty( "twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory" );
    }
    
    private static interface EventFeeder {
        public void startFeed();
        public void stopFeed();
    }
    
    private static class OfflineEventFeeder implements EventFeeder {
        private final AtomicBoolean keepFeeding = new AtomicBoolean(true);
        private StatusListener listener;
        private TwitterCBRFrame frame;
        
        public OfflineEventFeeder(TwitterCBRFrame frame, StatusListener listener) {
            this.listener = listener;
            this.frame = frame;
        }

        public void startFeed() {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    ObjectInputStream in = null;
                    try {
                        in = new ObjectInputStream( new FileInputStream( "src/main/resources/twitterstream.dump" ) );
                        frame.log( "Starting Feed..." );
                        
                        while( keepFeeding.get() ) {
                            // Read an event
                            Status st = (Status) in.readObject();
                            // call the listener
                            listener.onStatus( st );
                            Thread.yield();
                        }
                    } catch ( EOFException e ) {
                        frame.endOfStream();
                    } catch ( Exception e ) {
                        e.printStackTrace();
                    } finally {
                        if( in != null ) {
                            try { in.close(); } catch ( IOException e ) { }
                        }
                    }
                }
            }, "Offline Feeder Thread").start();
        }
        
        public void stopFeed() {
            keepFeeding.set( false );
            frame.log( "Stopping Feed" );
        }
    }
    
    private static class OnlineEventFeeder implements EventFeeder {
        private StatusListener listener;
        private TwitterStream twitterStream;
        private TwitterCBRFrame frame;
        
        public OnlineEventFeeder(TwitterCBRFrame frame, StatusListener listener) {
            this.listener = listener;
            this.frame = frame;
        }

        public void startFeed() {
            try { 
                twitterStream = new TwitterStreamFactory().getInstance();
                twitterStream.addListener( listener );
                frame.log( "Starting Feed..." );
                twitterStream.sample();
            } catch ( Exception e ) {
                frame.endOfStream();
                e.printStackTrace();
            }
        }
        
        public void stopFeed() {
            twitterStream.shutdown();
            frame.log( "Stopping Feed" );
        }
    }
}
