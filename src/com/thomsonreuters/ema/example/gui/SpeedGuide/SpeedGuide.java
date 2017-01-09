package com.thomsonreuters.ema.example.gui.SpeedGuide;

import com.thomsonreuters.ema.example.gui.SpeedGuide.view.SpeedGuideConnection;
import com.thomsonreuters.ema.example.gui.SpeedGuide.view.SpeedGuideViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class SpeedGuide extends Application
{
	public enum StatusIndicator
	{
		REQUEST,
		RESPONSE_SUCCESS,
		RESPONSE_ERROR
	}

	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String VER_CODE = "2.0";
    public SpeedGuideConsumer m_consumer = new SpeedGuideConsumer();
	private SpeedGuideConnection m_connection = new SpeedGuideConnection();
    private boolean m_debug = false;

	@Override
	public void start(Stage primaryStage) throws Exception {

        // Determine if we passed anything on the cmd line
        parseCmdLine();

        FXMLLoader loader = null;
        AnchorPane layout = null;

        try {
			// Load our view (from an fxml layout)
			loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("view/SpeedGuideView.fxml"));
			layout = loader.load();
        } catch (Exception e) {
			System.out.println("Exception in Start loading FXML resource: " + e);
			e.printStackTrace();
			stop();
		}

		// Wire up our Model/View/Controller
		SpeedGuideViewController viewController = loader.<SpeedGuideViewController>getController();
		viewController.setConnection(m_connection);
		viewController.setDebug(m_debug);
		viewController.defineControlBindings(m_consumer);

		// Notify our consumer some setup information
        m_consumer.setDebug(m_debug);
        m_consumer.setViewController(viewController);

		// Define the main viewing scene,
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

        // Attempt to Connect into Elektron
        m_connection.connect();
	}

    @Override
    public void stop() {
        System.exit(0);
    }

    private void parseCmdLine() throws Exception
    {
    	final String HOST_PARAM = "host";
    	final String SERVICE_PARAM = "service";
    	final String USER_PARAM = "user";

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
			System.out.println("  --d[ebug]               Debug Mode.  Display verbose messages to the console");
			System.out.println("  --h[elp]                Prints this screen"+NEWLINE);
			System.out.println("If neither the --host nor --service is not provided, the utility will prompt the user to enter these values."+NEWLINE);
			System.out.println("Example:");
			System.out.println("  > SpeedGuide.exe --host=elektron:14002 --service=ELEKTRON_AD -user=testuser");
    		stop();
    	}

    	m_debug = params.getRaw().contains("--d") || params.getRaw().contains("--debug") ||
    			 params.getNamed().containsKey("d") || params.getNamed().containsKey("debug");

    	m_connection.initialize(params.getNamed().get(HOST_PARAM),
    							params.getNamed().get(SERVICE_PARAM),
    							params.getNamed().get(USER_PARAM),
    							m_consumer);
    }

	public static void main(String[] args) {
		System.out.println("Java Ver: " + System.getProperty("java.specification.version") + " (" + System.getProperty("sun.arch.data.model") + "-bit)");
		System.out.println("JavaFX Ver: " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());

		launch(args);
	}
}
