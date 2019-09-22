package com.thomsonreuters.ema.example.gui.SpeedGuide;

import java.awt.SplashScreen;

import com.thomsonreuters.ema.example.gui.SpeedGuide.view.SpeedGuideConnection;
import com.thomsonreuters.ema.example.gui.SpeedGuide.view.SpeedGuideViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


// SpeedGuide
//
// Main class driving the application.  The SpeedGuide class represents our entry point for our UI
// and launching the EMA Consumer thread to manage all market data activity.  The SpeedGuide utilizes
// the FXML specification to drive the UI components.  All FXML definitions are launched at startup within
// this class.
public class SpeedGuide extends Application
{
	final String HOST_PARAM = "host";
	final String SERVICE_PARAM = "service";
	final String USER_PARAM = "user";
	final String APPID_PARAM = "appid";
	final String POSITION_PARAM = "position";

	public enum StatusIndicator
	{
		REQUEST,
		RESPONSE_SUCCESS,
		RESPONSE_ERROR
	}

	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String VER_CODE = "2.1";
    public SpeedGuideConsumer m_consumer = new SpeedGuideConsumer();
    private boolean m_debug = false;

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			final SplashScreen splash = SplashScreen.getSplashScreen();

	        // Determine if we passed anything on the cmd line
	        parseCmdLine();

	        // Define our controllers
	        SpeedGuideViewController viewController = loadFXMLController("view/SpeedGuideView.fxml");
	        SpeedGuideConnection connectionController = loadFXMLController("view/ConnectionDialog.fxml");

			// Wire up our Model/View/Controllers
			viewController.setConnectionViewController(connectionController);
			viewController.setDebug(m_debug);
			viewController.defineControlBindings(m_consumer);

			// Notify our consumer some setup information
	        m_consumer.setDebug(m_debug);
	        m_consumer.setViewController(viewController);

			// Define the main viewing scene,
	        AnchorPane layout = viewController.getLayout();
			Scene scene = new Scene(layout, layout.getPrefWidth(), layout.getPrefHeight());

			// Prevent the user from resizing the window too small
			primaryStage.setMinHeight(layout.getMinHeight());
			primaryStage.setMinWidth(layout.getMinWidth());

			// Assign to our main stage and show the application to the end user
			primaryStage.setTitle("Speed Guide");
			primaryStage.setScene(scene);
			primaryStage.show();

			// Set up our EMA Consumer and launch a thread to run...
	        Thread t = new Thread(m_consumer);
	        t.start();

	    	Application.Parameters params = getParameters();
	    	
	    	connectionController.initialize(params.getNamed().get(HOST_PARAM),
	    						  			params.getNamed().get(SERVICE_PARAM),
	    						  			params.getNamed().get(USER_PARAM),
	    						  			params.getNamed().get(APPID_PARAM),
	    						  			params.getNamed().get(POSITION_PARAM),
	    						  			m_consumer);

	        // Attempt to Connect into Elektron
			if ( splash != null )
				splash.close();
	        connectionController.connect();
		} catch  (Exception e) {
			System.out.print("Exception in Application Start: ");
			e.printStackTrace();
			stop();
		}
	}


	private <T> T loadFXMLController(String resource)
	{
        try {
			// Load our view (from an fxml layout)
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(resource));
			loader.load();
			return(loader.getController());
        } catch (Exception e) {
			System.out.print("Exception in loading FXML resource: " + resource + ": ");
			e.printStackTrace();
			stop();
		}
		return null;
	}

    private void parseCmdLine() throws Exception
    {
    	Application.Parameters params = getParameters();
    	if ( params.getRaw().contains("--h") || params.getRaw().contains("--help") ||
    		 params.getNamed().containsKey("h") || params.getNamed().containsKey("help"))
    	{
    		System.out.println(NEWLINE+"Syntax:"+NEWLINE);
    		System.out.println("  > java -jar SpeedGuide.jar [options]  or"+NEWLINE);
    		System.out.println("  > SpeedGuide.exe [options] <Windows only>"+NEWLINE);
    		System.out.println("Options:"+NEWLINE);
    		System.out.println("  --host=hostname:port    Server address/hostname and port of your Market Data server.");
    		System.out.println("                          Syntax: <host/ip>:<port>.  Eg: elektron:14002 or 192.168.1.1:14002");
			System.out.println("  --service=serviceName   Service Name providing market data content.");
			System.out.println("                          Eg: ELEKTRON_AD.");
			System.out.println("  --user=userName         User name required if authentication is enabled on server.");
			System.out.println("                          Note: if no user name is provided, the utility will use your desktop login");
			System.out.println("  --appid=ApplicationId   DACS Application ID if authentication is enabled on server.");
			System.out.println("                          Application ID has no default.");
			System.out.println("  --position=Position     DACS Position if authentication is enabled on server.");
			System.out.println("                          Position has no default.");			
			System.out.println("  --d[ebug]               Debug Mode.  Display verbose messages to the console");
			System.out.println("  --h[elp]                Prints this screen"+NEWLINE);
			System.out.println("If neither the --host nor --service is not provided, the utility will prompt the user to enter these values."+NEWLINE);
			System.out.println("Example:");
			System.out.println("  > SpeedGuide.exe --host=elektron:14002 --service=ELEKTRON_AD --user=testuser --appid=256 --position=127.0.0.1");
    		stop();
    	}

    	m_debug = params.getRaw().contains("--d") || params.getRaw().contains("--debug") ||
    			 params.getNamed().containsKey("d") || params.getNamed().containsKey("debug");
    }

    @Override
    public void stop() {
        System.exit(0);
    }

	public static void main(String[] args) throws Exception {
		System.out.println("Java Version: " + System.getProperty("java.runtime.version") + " (" + System.getProperty("sun.arch.data.model") + "-bit)");
		System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
		
		launch(args);
	}
}
