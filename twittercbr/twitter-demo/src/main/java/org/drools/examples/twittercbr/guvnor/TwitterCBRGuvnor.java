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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.TimeUnit;

import org.drools.ClockType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.conf.EventProcessingOption;
import org.drools.examples.twittercbr.TwitterStatusListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionPseudoClock;

import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterException;

/**
 * TwitterCBR
 */
public class TwitterCBRGuvnor {
    public static final boolean disableLog = true;

    /**
     * Main method
     */
    public static void main(String[] args) throws TwitterException, IOException{
        
        // Creates a knowledge base
        final KnowledgeBase kbase = createKnowledgeBase();
        
        // Creates a knowledge session
        final StatefulKnowledgeSession ksession = createKnowledgeSession( kbase );
        
        // Gets the stream entry point 
        final WorkingMemoryEntryPoint ep = ksession.getWorkingMemoryEntryPoint( "twitter" );
        
        new Thread( new Runnable() {
            @Override
            public void run() {
                // Starts to fire rules in Drools Fusion
                ksession.fireUntilHalt();
            }
        }).start();

        // reads the status stream and feeds it to the engine 
        feedEvents( ksession,
                    ep );
        
        ksession.halt();
        
    }

    /**
     * Feed events from the stream to the engine
     * @param ksession
     * @param ep
     */
    private static void feedEvents( final StatefulKnowledgeSession ksession,
                                    final WorkingMemoryEntryPoint ep ) {
        try {
            StatusListener listener = new TwitterStatusListener( ep );
            ObjectInputStream in = new ObjectInputStream( new FileInputStream( "src/main/resources/twitterstream.dump" ) );
            SessionPseudoClock clock = ksession.getSessionClock();
            
            for( int i = 0; ; i++ ) {
                try {
                    // Read an event
                    Status st = (Status) in.readObject();
                    // Using the pseudo clock, advance the clock
                    clock.advanceTime( st.getCreatedAt().getTime() - clock.getCurrentTime(), TimeUnit.MILLISECONDS );
                    // call the listener
                    listener.onStatus( st );
                } catch( IOException ioe ) {
                    break;
                }
            }
            in.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a Drools KnowledgeBase and adds the given rules file into it
     */
    private static KnowledgeBase createKnowledgeBase() {
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
        KnowledgeAgentConfiguration kaconf = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
        KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( "agent", kbase, kaconf );
        kagent.applyChangeSet( ResourceFactory.newUrlResource( "http://localhost:8080/guvnor-5.4.0.Final-jboss-as-7.0/org.drools.guvnor.Guvnor/package/demo.twitter.pkg1/LATEST/ChangeSet.xml" ) );
        
        return kagent.getKnowledgeBase();
    }

    /**
     * Creates a Drools Stateful Knowledge Session
     */
    private static StatefulKnowledgeSession createKnowledgeSession( final KnowledgeBase kbase ) {
        final KnowledgeSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() ) );
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession( ksconf, null );
        ksession.setGlobal( "uiservice", new UIServiceImpl() );
        return ksession;
    }

    static {
        // disable twitter4j log
        System.setProperty( "twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory" );
    }
    
}
