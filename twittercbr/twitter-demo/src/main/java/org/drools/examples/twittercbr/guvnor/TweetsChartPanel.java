/*
 * Copyright 2010 JBoss Inc
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.RectangleInsets;

/**
 * A panel to log information
 */
public class TweetsChartPanel extends JPanel {

    private static final long serialVersionUID = -6538366711433519153L;
    private TimeSeries        tweets;

    public TweetsChartPanel() {
        setLayout( new BorderLayout() );

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        tweets = new TimeSeries( "Tweets/30s" );
        tweets.setMaximumItemCount( 10 );

        dataset.addSeries( tweets );

        JFreeChart chart = ChartFactory.createTimeSeriesChart( "Tweets in the last 30 seconds",
                                                               "Time",
                                                               "Tweets",
                                                               dataset,
                                                               false,
                                                               false,
                                                               false );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride( new SimpleDateFormat( "HH:mm:ss" ) );

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLowerBound( 0 );
        rangeAxis.setUpperBound( 2500 );

        plot.setBackgroundPaint( Color.lightGray );
        plot.setDomainGridlinePaint( Color.white );
        plot.setRangeGridlinePaint( Color.white );
        plot.setAxisOffset( new RectangleInsets( 5.0, 5.0, 5.0, 5.0 ) );
        plot.setDomainCrosshairVisible( true );
        plot.setRangeCrosshairVisible( true );

        XYItemRenderer r = plot.getRenderer();
        if ( r instanceof XYLineAndShapeRenderer ) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible( true );
            renderer.setBaseShapesFilled( true );
            renderer.setDrawSeriesLineAsPath( true );
            renderer.setSeriesItemLabelsVisible( 0, true );
        }

        ChartPanel panel = new ChartPanel( chart );

        add( panel,
             BorderLayout.CENTER );
        setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        setPreferredSize( new Dimension( 400, 50 ) );
    }

    public void addDataPoint(long timestamp,
                             int count) {
        tweets.add( new FixedMillisecond( timestamp ), count, true );
    }

    public void clear() {
        tweets.clear();
    }

}
