package org.drools.examples.twittercbr.guvnor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import twitter4j.Status;

public class TwitterCBRFrame {
    
    private static final String ONLINE = "Online";
    private static final String OFFLINE = "Offline";
    private static final String GUVNOR = "Guvnor";
    private static final String ECLIPSE = "Eclipse";
    
    private JFrame frame;
    private LogPanel logPanel;
    private final TwitterCBRGuvnor app;
    private JButton start;
    private JButton stop;
    private TweetsPanel tweetsPanel;
    private TweetsChartPanel chart;

    public TwitterCBRFrame(TwitterCBRGuvnor app) {
        this.frame = buildFrame();
        this.app = app;
    }

    private JFrame buildFrame() {
        JPanel contentPanel = new JPanel(new GridLayout(2, 1));
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        JPanel topContentPanel = new JPanel( new BorderLayout() );
        JPanel commandPanel = buildCommandPanel();
        logPanel = new LogPanel();
        tweetsPanel = new TweetsPanel();
        chart = new TweetsChartPanel();
        
        topPanel.add( chart );
        topPanel.add( logPanel );

        topContentPanel.add( topPanel, BorderLayout.CENTER );
        topContentPanel.add( commandPanel, BorderLayout.SOUTH );
        
        contentPanel.add( topContentPanel );
        contentPanel.add( tweetsPanel );

        JFrame frame = new JFrame();
        frame.setContentPane(contentPanel);
        
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        frame.setTitle( "Drools Fusion Example: Playing with Tweets" );
        frame.setResizable( true );
        frame.setPreferredSize( new Dimension(900,700) );
        frame.pack();

        frame.setLocationRelativeTo( null );
        
        return frame;
    }
    
    private JPanel buildCommandPanel() {
        JRadioButton online = new JRadioButton(ONLINE);
        online.setMnemonic(KeyEvent.VK_N);
        online.setActionCommand(ONLINE);
        online.setAlignmentX( Component.LEFT_ALIGNMENT );
        online.setSelected(true);

        JRadioButton offline = new JRadioButton(OFFLINE);
        offline.setMnemonic(KeyEvent.VK_F);
        offline.setActionCommand(OFFLINE);
        offline.setAlignmentX( Component.LEFT_ALIGNMENT );
        
        JRadioButton guvnor = new JRadioButton(GUVNOR);
        guvnor.setMnemonic(KeyEvent.VK_G);
        guvnor.setActionCommand(GUVNOR);
        guvnor.setAlignmentX( Component.LEFT_ALIGNMENT );
        guvnor.setSelected(true);

        JRadioButton eclipse = new JRadioButton(ECLIPSE);
        eclipse.setMnemonic(KeyEvent.VK_E);
        eclipse.setActionCommand(ECLIPSE);
        eclipse.setAlignmentX( Component.LEFT_ALIGNMENT );
        
        //Group the radio buttons.
        final ButtonGroup eventsSource = new ButtonGroup();
        eventsSource.add(online);
        eventsSource.add(offline);
        
        final ButtonGroup rulesSource = new ButtonGroup();
        rulesSource.add( guvnor );
        rulesSource.add( eclipse );
        
        start = new JButton("Start");
        stop = new JButton("Stop");
        stop.setEnabled( false );
        start.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start.setEnabled( false );
                tweetsPanel.clear();
                chart.clear();
                app.startEngine( determineMode( eventsSource.getSelection().getActionCommand() ),
                                 determineRulesSource( rulesSource.getSelection().getActionCommand() ) );
                stop.setEnabled( true );
            }
            private TwitterCBRGuvnor.RulesSource determineRulesSource(String actionCommand) {
                if( ECLIPSE.equals( actionCommand ) ) { 
                    return TwitterCBRGuvnor.RulesSource.ECLIPSE;
                } else {
                    return TwitterCBRGuvnor.RulesSource.GUVNOR;
                }
            }
            private TwitterCBRGuvnor.Mode determineMode( String mode ) {
                if( ONLINE.equals( mode ) ) { 
                    return TwitterCBRGuvnor.Mode.ONLINE;
                } else {
                    return TwitterCBRGuvnor.Mode.OFFLINE;
                }
            }
        } );
        stop.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop.setEnabled( false );
                app.stopEngine();
                start.setEnabled( true );
            }
        } );
        
        JPanel modePanel = new JPanel( new FlowLayout() );
        modePanel.setBorder( BorderFactory.createTitledBorder( "Mode" ) );
        modePanel.add( online );
        modePanel.add( offline );

        JPanel sourcePanel = new JPanel( new FlowLayout() );
        sourcePanel.setBorder( BorderFactory.createTitledBorder( "Rules source" ) );
        sourcePanel.add( guvnor );
        sourcePanel.add( eclipse );
        
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add( modePanel );
        buttons.add( sourcePanel );
        buttons.add( start );
        buttons.add( stop );
        
        return buttons;
    }
    
    public void show() {
        this.frame.setVisible( true );
    }
    
    public void log( String message ) {
        this.logPanel.log( message );
    }
    
    public void showTweet( String msg, Status tweet ) {
        this.tweetsPanel.addTweet( msg, tweet );
    }
    
    public void endOfStream() {
        stop.doClick();
        log( "End of Stream");
    }
    
    public void reportTweetCount( long timestamp, int count ) {
        this.chart.addDataPoint( timestamp, count );
    }

}
