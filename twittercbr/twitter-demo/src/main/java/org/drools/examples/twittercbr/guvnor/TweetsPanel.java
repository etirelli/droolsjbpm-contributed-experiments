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
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import twitter4j.Status;

/**
 * A panel to log information
 */
public class TweetsPanel extends JPanel {

    private static final long serialVersionUID = -6538366711433519153L;
    private static final int WIDTH = 900;

    private final JTable      table;

    private TweetsTableModel  model;

    public TweetsPanel() {
        setLayout( new BorderLayout() );
        model = new TweetsTableModel();
        table = new JTable( model );

        JScrollPane areaScrollPane = new JScrollPane( table );
        areaScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        table.setFillsViewportHeight( true );
        table.setDefaultRenderer( Date.class, new DateRenderer() );
        
        TableColumn column = null;
        for (int i = 0; i < TweetsTableModel.width.length; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth( (int) ( TweetsTableModel.width[i] * WIDTH ) ); 
        } 

        add( areaScrollPane,
             BorderLayout.CENTER );
        setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        //setPreferredSize(new Dimension(400, 50));
    }

    public void addTweet(final String msg,
                         final Status tweet) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                model.addTweet( msg, tweet );
                scrollToVisible();
            }
        } );
    }
    
    public void clear() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                model.clear();
            }
        } );
    }
    
    public void scrollToVisible() {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(model.getRowCount()-1, 0, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    private static class TweetsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1694613101970748288L;
        
        public static final String[] columns = new String[]{"Notes", "Time", "User", "Tweet", "Location"};
        public static final double[] width = new double[]{.1, .1, .1, .6, .1};
        
        private List<TweetData>       list;

        public TweetsTableModel() {
            list = new ArrayList<TweetData>( 1000 );
        }

        public void clear() {
            int size = list.size();
            if( size > 0 ) {
                list.clear();
                fireTableRowsDeleted( 0, size-1 );
            }
        }

        @Override
        public int getRowCount() {
            return list.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class< ? > getColumnClass(int columnIndex) {
            return columnIndex == 1 ? Date.class : String.class;
        }

        @Override
        public Object getValueAt(int rowIndex,
                                 int columnIndex) {
            return list.get( rowIndex ).data[columnIndex];
        }

        public void addTweet(final String msg,
                             final Status tweet) {
            int row = list.size();
            this.list.add( new TweetData( msg, tweet ) );
            fireTableRowsInserted( row, row );
        }
    }

    private static class TweetData {
        public final Object[] data;

        public TweetData(String msg,
                         Status tweet) {
            this.data = new Object[]{
                    msg,
                    tweet.getCreatedAt(),
                    tweet.getUser().getScreenName(),
                    tweet.getText(),
                    tweet.getPlace() != null ? tweet.getPlace().getCountryCode() : ""
            };
        }
    }
    
    private static class DateRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -1671818538702708948L;
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        public DateRenderer() { super(); }

        public void setValue(Object value) {
            setText((value == null) ? "" : formatter.format(value));
        }
    }    
    
}
