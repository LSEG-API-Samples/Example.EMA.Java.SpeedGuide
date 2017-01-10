package com.thomsonreuters.ema.example.gui.SpeedGuide.view;

import com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuide;
import com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuideConsumer;
import com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuide.StatusIndicator;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

public class SpeedGuideViewController implements Initializable
{
	// Controls defined within the FXML configuration
	@FXML private Label 			version;
	@FXML private Button			home;
	@FXML private Button			previous;
	@FXML private Button			next;
	@FXML private TextField			ric;
	@FXML private Button			submit;
	@FXML private Label				servicesLabel;
	@FXML private ComboBox<String>	services;
	@FXML private Button			connect;
	@FXML private TextArea			textArea;
	@FXML private ScrollPane 		statusPane;
	@FXML private TextFlow			statusText;

    private LinkedHashMap<String, String> m_mapOfPages = new LinkedHashMap<String, String>();
    private List<String> m_listOfPages = new ArrayList<String>();
    private List<String> m_listOfRICs = new ArrayList<String>();
    private int		 m_pageCount = 0;
    private int 	 m_pageTotal = 0;
	private boolean  m_debug = false;
	private SpeedGuideConsumer m_consumer;
	private SpeedGuideConnection m_connection;

    private ObservableList<String> m_services = FXCollections.observableArrayList();

	@Override		// Method called by the FXMLLoader when initialization is done
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		version.setText(version.getText() + SpeedGuide.VER_CODE);
	}

	public void defineControlBindings(SpeedGuideConsumer consumer) {
		m_consumer = consumer;

        home.disableProperty().bind(Bindings.when(m_consumer._connected).then(false).otherwise(true));
        previous.disableProperty().bind(Bindings.when(m_consumer._connected).then(false).otherwise(true));
        next.disableProperty().bind(Bindings.when(m_consumer._connected).then(false).otherwise(true));
        ric.disableProperty().bind(Bindings.when(m_consumer._connected).then(false).otherwise(true));
        submit.disableProperty().bind(Bindings.when(m_consumer._connected).then(false).otherwise(true));

        // The connection button has many states.
        //		Enabled: 	When we are disconnected and need to manually connect
        //		Disabled:	When we are in the process of trying to connect
        //		Visible:	When we are disconnected.
        //		Invisible:	When we are connected.
        //		Managed:	When we are disconnected.  In this state, the button is visible and we want the
        //					HBox to manage the layout of the button.
        //		UnManaged:	When we are connected.  In this state, the button is invisible but we do not want
        //					HBox to manage the layout of the button.  This allows us to properly display the
        //					Services ComboBox within the HBox, right-aligned.
        connect.visibleProperty().bind(Bindings.when(m_consumer._connected).then(false).otherwise(true));
        connect.disableProperty().bind(Bindings.when(m_consumer._connecting).then(true).otherwise(false));
        connect.managedProperty().bind(connect.visibleProperty());

        servicesLabel.visibleProperty().bind(Bindings.when(m_consumer._connected).then(true).otherwise(false));

        services.visibleProperty().bind(Bindings.when(m_consumer._connected).then(true).otherwise(false));

        // Determine what to display within the "Available Services" combo box when the control becomes visible
        services.visibleProperty().addListener(new ChangeListener<Boolean>() {
        	@Override public void changed(ObservableValue<? extends Boolean> o, Boolean oldVal, Boolean newVal)
        	{
        		// This will avoid a InvalidState Exception because we're not within the JavaFX thread
    			Platform.runLater(new Runnable()
    			{
    				@Override
    				public void run()
    				{
    	        		if ( newVal )
    	        		{
		        			services.setItems(m_services);
		        			if (m_services.contains(m_consumer.getService()))
		        				services.setValue(m_consumer.getService());
		        			else
		        				services.setPromptText(m_consumer.getService());
    	        		}
    	        		else
    	        			m_services.clear();
    				}
    			});
        	}
        });
	}

	public void setConnection(SpeedGuideConnection connection) {
		m_connection = connection;
	}

	public void setDebug(boolean debug) {
		m_debug = debug;
	}

	public void addService(String service) {
		m_services.add(service);
	}

	@FXML
	private void clickedHome() {
       	if (m_debug) System.out.println("_pageCount ="+m_pageCount);
    	String pageStr = m_listOfPages.get(0);

    	String item = m_listOfRICs.get(0);

    	textArea.clear();
    	textArea.setText(pageStr);
    	ric.setText(item);

    	// Reset
    	m_pageCount = 1;
	}

	@FXML
	private void clickedPrevious() {
    	if (m_debug) System.out.println("_pageCount ="+m_pageCount);
    	if (m_pageCount > 1) {
        	m_pageCount--;

        	String pageStr = m_listOfPages.get(m_pageCount-1);
        	String item = m_listOfRICs.get(m_pageCount-1);
        	textArea.clear();
        	textArea.setText(pageStr);
        	ric.setText(item);
    	} else {
    		if (m_debug) System.out.println("No PREVIOUS");
    	}
	}

	@FXML
	private void clickedNext() {
    	if (m_debug) System.out.println("_pageCount ="+m_pageCount +" _pageTotal="+m_pageTotal);
    	if (m_pageCount < m_pageTotal) {

        	String pageStr = m_listOfPages.get(m_pageCount);
        	String item = m_listOfRICs.get(m_pageCount);
        	m_pageCount++;
        	textArea.clear();
        	textArea.setText(pageStr);
        	ric.setText(item);
    	} else {
    		if (m_debug) System.out.println("No NEXT Page");
    	}
	}

	@FXML
	private void enteredRic() {
    	m_consumer.subscribe(ric.getText().trim());
	}

	@FXML
	private void clickedSubmit() {
   		m_consumer.subscribe(ric.getText().trim());
	}

	@FXML
	private void clickedConnect() {
    	m_connection.connectionDialog();
	}

	@FXML
	private void selectedService() {
    	m_consumer.setService(services.getSelectionModel().getSelectedItem().toString());
   		m_consumer.subscribe(ric.getText().trim());
	}

    /**
     * Add Event Listener handler for the TextArea:
     * 1. detect double click on <> and obtain (RIC) text inside of <>
     * 2. subscribe to RIC
     */
	@FXML
	private void clickedTextArea(MouseEvent event) {
        if (event.getClickCount() == 2) {
        	// Get the range of the selected text
            IndexRange range = textArea.getSelection();

            if ( findRegion(range, '<', '>') ) {	// Check if region is embedded within < > brackets
            	String ric = textArea.getSelectedText().trim();
            	if (m_debug) System.out.println("Selected text: [" + ric + "]");
            	m_consumer.subscribe(ric);
            } else {
            	if ( findRegion(range, '[', ']') ) {   // Check if region is embedded within [ ] brackets
            		String news = textArea.getSelectedText().trim();
            		if (m_debug) System.out.println("Selected text: [" + news + "]");
            		updateStatus("Only text within < > supported.", StatusIndicator.RESPONSE_ERROR);
            	}
            }

            // Unmark region
            textArea.selectRange(0,0);
        }
	}

	// Mark off region embedded within the open and close tokens.
	private boolean findRegion(IndexRange startingRange, int openToken, int closeToken)
	{
		if ( findStartPoint(startingRange, openToken, closeToken) ) {
			int start = textArea.getSelection().getStart();
			if ( findEndPoint(startingRange, openToken, closeToken) ) {
				textArea.selectRange(start, textArea.getSelection().getEnd());
				return(true);
			}
		}

		// Selected text not embedded with open/close tokens
		return(false);
	}

	// Find the starting token location, if available, based on where the user double clicked
	private boolean findStartPoint(IndexRange range, int openToken, int closeToken)
	{
		int start = range.getStart();
		int end = range.getEnd();

		while (true) {
			textArea.selectRange(start-10, end);
			IndexRange selection = textArea.getSelection();

			// Determine if we've reached the start of the text area
			if ( selection.getStart() == start ) return(false);

			// Extract the text region of interest
			String region = textArea.getSelectedText();

			// Determine if we've encountered a close token
			int cpos = region.lastIndexOf(closeToken);

			// Determine if we've encountered an Open token
			int opos = region.lastIndexOf(openToken);

			if (cpos > opos) return(false);

			if (opos >= 0) {
				textArea.selectRange(selection.getStart()+opos+1, end);
				return(true);
			}

			// We should not be on the previous line
			if ( region.indexOf('\n') >= 0 ) return(false);

			start = selection.getStart();
		}
	}

	// Find the ending token location, if available, based on where the user double clicked
	private boolean findEndPoint(IndexRange range, int openToken, int closeToken)
	{
		int start = range.getStart();
		int end = range.getEnd();

		while (true) {
			textArea.selectRange(start, end+10);
			IndexRange selection = textArea.getSelection();

			// Determine if we've reached the end of the text area
			if ( selection.getEnd() == end ) return(false);

			// Extract the text region of interest
			String region = textArea.getSelectedText();

			// Determine if we've encountered a close token
			int cpos = region.indexOf(closeToken);

			// Determine if we've encountered an Open token
			int opos = region.indexOf(openToken);

			if (opos >= 0) {
				if ( cpos < 0 || opos < cpos ) return(false);
			}

			if (cpos >= 0) {
				textArea.selectRange(range.getStart(), start+cpos);
				return(true);
			}

			// We should not be on the previous line
			if ( region.indexOf('\n') >= 0 ) return(false);

			end = selection.getEnd();
		}
	}

    /**
     * Update ric in header
     *
     * @param ric
     */
    public void updateRic( String txt )
    {
    	ric.setText(txt);
    }

    public void updateTxtArea( String ric, String pageStr )
    {
    	updateRic(ric);
    	textArea.setText(pageStr);

    	if ( !ric.isEmpty()) {
    		// before adding the new page, check
	    	int extrapages = (m_pageTotal - m_pageCount) ;
	    	if (m_debug) System.out.println("extrapages=" + extrapages + " pageTotal=" + m_pageTotal +" pageCount=" + m_pageCount +
	    									" listOfPages.size=" + m_listOfPages.size());
	    	if ( extrapages > 0 ) {
	    		// remove pages
	    		int countSubs = 0;
	    		for (int i=(m_pageTotal-1); m_pageCount <=i; i--) {
	    			if (m_debug) System.out.println("REMOVE page num="+i + " RIC=" + m_listOfRICs.get(i)+ "     _listOfPages.size=" + m_listOfPages.size());
	    			m_listOfPages.remove(i);
	    			m_listOfRICs.remove(i);
	    			countSubs++;
	    		}
	    		m_pageTotal = m_pageTotal - countSubs;
	    		if (m_debug) System.out.println("countSubs="+ countSubs +" _pageTotal="+m_pageTotal);
	    	}

	    	m_mapOfPages.put(ric, pageStr);
	    	m_listOfPages.add(pageStr);
	    	m_listOfRICs.add(ric);

	    	m_pageTotal++;
	    	m_pageCount = m_pageTotal;
    	}
    }


    /**
     * Update status
     *
     * @param status
     * @param indicator
     */
    public void updateStatus(String status, StatusIndicator indicator)
    {
    	Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
		    	ObservableList<Node> nodes = statusText.getChildren();

				Text time = new Text();
	    		time.setId("status-time");		// Defined in CSS
				time.setText(new SimpleDateFormat("HH.mm.ss: ").format(new java.util.Date()));

		    	Text txt = new Text();
		    	txt.setText(status + SpeedGuide.NEWLINE);

		    	switch (indicator)
		    	{
			    	case REQUEST:
			    		txt.setId("status-request");			// Defined in CSS
			    		break;
			    	case RESPONSE_SUCCESS:
			    		txt.setId("status-response-success");	// Defined in CSS
			    		break;
			    	case RESPONSE_ERROR:
			    		txt.setId("status-response-error");		// Defined in CSS
			    		break;
		    	}

		    	nodes.add(0, txt);
		    	nodes.add(0, time);
			}
		});
    }
}
