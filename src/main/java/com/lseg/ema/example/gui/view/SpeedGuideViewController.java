package com.lseg.ema.example.gui.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.lseg.ema.example.gui.SpeedGuide;
import com.lseg.ema.example.gui.SpeedGuideConsumer;
import com.lseg.ema.example.gui.SpeedGuide.StatusIndicator;

public class SpeedGuideViewController implements Initializable
{
	// Controls defined within the FXML configuration
	@FXML private AnchorPane		layout;
	@FXML private Label 			version;
	@FXML private Button			home;
	@FXML private Button			previous;
	@FXML private Button			next;
	@FXML private TextField			ric;
	@FXML private Button			submit;
	@FXML private HBox 				regionsHBox;
	@FXML private ComboBox<String>	regions;
	@FXML private HBox				servicesHBox;
	@FXML private ComboBox<String>	services;
	@FXML private HBox				connectHBox;
	@FXML private Button			connect;
	@FXML private TextArea			textArea;
	@FXML private ScrollPane 		statusPane;
	@FXML private TextFlow			statusText;

    private List<String> m_listOfPages = new ArrayList<String>();
    private List<String> m_listOfRICs = new ArrayList<String>();

    // Index is 1-based (pages 1..n) where n=pageTotal
    private  IntegerProperty m_pageIndex = new SimpleIntegerProperty(0);
    private	IntegerProperty m_pageTotal = new SimpleIntegerProperty(0);

    // Connection status
	private BooleanProperty m_connecting = new SimpleBooleanProperty(false);
	private BooleanProperty m_connected = new SimpleBooleanProperty(false);

	// RTO-enabled
	private BooleanProperty m_rtoEnabled = new SimpleBooleanProperty(false);

	private boolean  m_debug = false;
	private SpeedGuideConsumer m_consumer;
	private SpeedGuideConnection m_connection;

	private ObservableList<String> m_regions = FXCollections.observableArrayList();
    private ObservableList<String> m_services = FXCollections.observableArrayList();

	@Override		// Method called by the FXMLLoader when initialization is done
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		version.setText(version.getText() + SpeedGuide.VER_CODE);
	}

	public AnchorPane getLayout() {
		return(layout);
	}

	public void defineControlBindings(SpeedGuideConsumer consumer) {
		m_consumer = consumer;

		home.disableProperty().bind(Bindings.equal(m_pageTotal, 0));
        previous.disableProperty().bind(Bindings.lessThanOrEqual(m_pageIndex, 1));
        next.disableProperty().bind(Bindings.equal(m_pageIndex, m_pageTotal));
        ric.disableProperty().bind(Bindings.when(m_connected).then(false).otherwise(true));
        submit.disableProperty().bind(Bindings.when(m_connected).then(false).otherwise(true));

        // The region selection has the following states.
        //		Enabled: 	When we are disconnected and need to manually select a different region
        //		Disabled:	When we are in the process of trying to connect
        //		Visible:	When we have no endpoint information.
        //		Invisible:	When we are connected.	
		regionsHBox.visibleProperty().bind(m_rtoEnabled);
        regions.disableProperty().bind(m_connecting);

		// Determine what to display within the "RTO Regions" combo box when the control becomes visible
        regionsHBox.visibleProperty().addListener(new ChangeListener<Boolean>() {
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
		        			regions.setItems(m_regions);
		        			if (m_regions.contains(m_consumer.getRegion()))
		        				regions.setValue(m_consumer.getRegion());
		        			else
		        				regions.setPromptText(m_consumer.getRegion());
    	        		}
    	        		else
    	        			m_regions.clear();
    				}
    			});
        	}
        });		

        // The connection button has the following states.
        //		Enabled: 	When we are disconnected and need to manually connect
        //		Disabled:	When we are in the process of trying to connect
        //		Visible:	When we are disconnected.
        //		Invisible:	When we are connected.
		connectHBox.visibleProperty().bind(Bindings.when(m_connected).then(false).otherwise(true));
        connect.disableProperty().bind(Bindings.when(m_connecting).then(true).otherwise(false));

		servicesHBox.visibleProperty().bind(m_connected);

        // Determine what to display within the "Available Services" combo box when the control becomes visible
        servicesHBox.visibleProperty().addListener(new ChangeListener<Boolean>() {
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

	public void setConnection(boolean connected, boolean connecting, String pageStr) {
		// This will avoid a InvalidState Exception to ensure we are within the JavaFX thread
		Platform.runLater(new Runnable()
		{
			@Override
			public void run() {
				updateTxtArea("", pageStr);
				m_connected.set(connected);
				m_connecting.set(connecting);
			}
		});
	}

	public void setRTOConnection(boolean rtoConnection) {
		m_rtoEnabled.set(rtoConnection);
	}

	public boolean isConnected() {
		return(m_connected.get());
	}

	public boolean isConnecting() {
		return(m_connecting.get());
	}

	public void setConnectionViewController(SpeedGuideConnection connectionViewController) {
		m_connection = connectionViewController;
	}

	public void setDebug(boolean debug) {
		m_debug = debug;
	}

	public void addRegion(String region) {
		if (!m_regions.contains(region))
			m_regions.add(region);
	}

	public void addService(String service) {
		m_services.add(service);
	}

	@FXML
	private void clickedHome() {
    	String pageStr = m_listOfPages.get(0);
    	String item = m_listOfRICs.get(0);

    	textArea.clear();
    	textArea.setText(pageStr);
    	ric.setText(item);

    	// Reset
    	m_pageIndex.set(1);
    	if (m_debug) System.out.println("pageIndex=" + m_pageIndex.get() +" pageTotal=" + m_pageTotal.get());
	}

	@FXML
	private void clickedPrevious() {

    	int pageIndex = m_pageIndex.get();

    	// Decrement
    	pageIndex--;
    	m_pageIndex.set(pageIndex);

        String pageStr = m_listOfPages.get(pageIndex-1);
        String item = m_listOfRICs.get(pageIndex-1);
        textArea.clear();
        textArea.setText(pageStr);
        ric.setText(item);
    	if (m_debug) System.out.println("pageIndex=" + m_pageIndex.get() +" pageTotal=" + m_pageTotal.get());
	}

	@FXML
	private void clickedNext() {

    	int pageIndex = m_pageIndex.get();

    	String pageStr = m_listOfPages.get(pageIndex);
    	String item = m_listOfRICs.get(pageIndex);
    	m_pageIndex.set(pageIndex+1);

    	textArea.clear();
    	textArea.setText(pageStr);
    	ric.setText(item);
    	if (m_debug) System.out.println("pageIndex=" + m_pageIndex.get() +" pageTotal=" + m_pageTotal.get());
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
	private void selectedRegion() {
		String selectedItem = regions.getSelectionModel().getSelectedItem();
		if ( selectedItem != null) {
			if (!selectedItem.equals(m_consumer.getRegion())) {
				m_consumer.setRegion(selectedItem);
				m_consumer.defineNewRegion();
			}
		}
	}

	@FXML
	private void selectedService() {
		String selectedItem = services.getSelectionModel().getSelectedItem();
		if ( selectedItem != null) {
	    	m_consumer.setService(selectedItem);
	   		m_consumer.subscribe(ric.getText().trim());
		}
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

			if (cpos > opos || opos >= 0) {
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

    public void updateTxtArea( String item, String pageStr )
    {
		// This will avoid a InvalidState Exception to ensure we are within the JavaFX thread
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
		    	ric.setText(item);
		    	textArea.setText(pageStr);

		    	if ( !item.isEmpty()) {
		    		// before adding the new page, check
		    		int pageTotal = m_pageTotal.get();

			    	m_listOfPages.add(pageStr);
			    	m_listOfRICs.add(item);

			    	pageTotal++;
			    	m_pageTotal.set(pageTotal);
			    	m_pageIndex.set(pageTotal);
			    	if (m_debug) System.out.println("pageIndex=" + m_pageIndex.get() +" pageTotal=" + pageTotal);
		    	}
			}
		});
    }


    /**
     * Update status
     *
     * @param status
     * @param indicator
     */
    public void updateStatus(String status, StatusIndicator indicator)
    {
    	updateStatus(status, indicator, "");
    }

    public void updateStatus(String status, StatusIndicator indicator, String item)
    {
		// This will avoid a InvalidState Exception to ensure we are within the JavaFX thread
    	Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				if ( !item.isEmpty() )
			    	ric.setText(item);

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
						txt.setFill(Color.GREEN);               
			    		break;
			    	case RESPONSE_ERROR:
			    		txt.setId("status-response-error");		// Defined in CSS
						txt.setFill(Color.RED);					
			    		break;
		    	}

		    	nodes.add(0, txt);
		    	nodes.add(0, time);
			}
		});
    }
}
