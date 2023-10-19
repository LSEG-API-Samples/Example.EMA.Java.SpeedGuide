package com.lseg.ema.example.gui.view;

import com.lseg.ema.example.gui.SpeedGuideConsumer;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// SpeedGuideConnection
//
// Connection View Controller for connection dialog.  This view controller is created within the
// SpeedGuide.java class upon loading of the associated FXML UI components.  The loading references
// this definition and thus automatically creates an instance of the view controller.
//
public class SpeedGuideConnection
{
	// Real-Time -- Optimized commandline parameters

	public class ADSConnection
	{
		// Command-line parameter names
		private static final String HOST_PARAM = "host";
		private static final String SERVICE_PARAM = "service";
		private static final String USER_PARAM = "user";
		private static final String APPID_PARAM = "appid";
		private static final String POSITION_PARAM = "position";
		
		public void setConnection(Application.Parameters cmdLineParams)
		{
			setConnection(cmdLineParams.getNamed().get(HOST_PARAM),
						  cmdLineParams.getNamed().get(SERVICE_PARAM),
						  cmdLineParams.getNamed().get(USER_PARAM),
						  cmdLineParams.getNamed().get(APPID_PARAM),
						  cmdLineParams.getNamed().get(POSITION_PARAM));
		}
		
		public void setConnection(String host, String service, String user, String appId, String position)
		{
			if ( host != null) _host = host.trim();
			if ( service != null) _service = service.trim();
			if ( user != null) _user = user.trim();
			if ( appId != null) _appId = appId.trim();
			if ( position != null) _position = position.trim();
		}

		public String _host = "";
		public String _service = "";
		public String _user = "";
		public String _appId = "";
		public String _position = "";
	}
	
	public class RTO_V1Connection
	{
		// *********************************
		// Command-line parameter names
		// *********************************

		// V1 (Grant Password)
		private static final String MACHINEID_PARAM = "machineId";
		private static final String PASSWORD_PARAM = "password";
		private static final String APPKEY_PARAM = "appKey";

		private static final String KEYSTORE_PARAM = "keyStore";
		private static final String KEYSTOREPASSWD_PARAM = "keyStorePasswd";
		
		public void setConnection(Application.Parameters cmdLineParams)
		{
			setConnection(cmdLineParams.getNamed().get(MACHINEID_PARAM),
					  	  cmdLineParams.getNamed().get(PASSWORD_PARAM),
					  	  cmdLineParams.getNamed().get(APPKEY_PARAM));
			
			String value = cmdLineParams.getNamed().get(KEYSTORE_PARAM);
			if (value != null) _keystoreFile = value.trim();
			
			value = cmdLineParams.getNamed().get(KEYSTOREPASSWD_PARAM);
			if (value != null) _keystorePasswd = value.trim();
		}
		
		public void setConnection(String machineId, String password, String appKey)
		{
			if (machineId != null) _machineId = machineId.trim();
			if (password != null) _password = password.trim();
			if (appKey != null) _appKey = appKey.trim();
		}

		public String _machineId = "";
		public String _password = "";
		public String _appKey = "";
		public String _keystoreFile = "keystore.jks";	// Default location within the package
		public String _keystorePasswd = "Welcome1";
	}

	public class RTO_V2Connection
	{
		// *********************************
		// Command-line parameter names
		// *********************************

		// V2 (Client Credentials)
		private static final String CLIENTID_PARAM = "clientId";
		private static final String CLIENT_SECRET_PARAM = "clientSecret";

		private static final String KEYSTORE_PARAM = "keyStore";
		private static final String KEYSTOREPASSWD_PARAM = "keyStorePasswd";
		
		public void setConnection(Application.Parameters cmdLineParams)
		{
			setConnection(cmdLineParams.getNamed().get(CLIENTID_PARAM),
					  	  cmdLineParams.getNamed().get(CLIENT_SECRET_PARAM));
			
			String value = cmdLineParams.getNamed().get(KEYSTORE_PARAM);
			if (value != null) _keystoreFile = value.trim();
			
			value = cmdLineParams.getNamed().get(KEYSTOREPASSWD_PARAM);
			if (value != null) _keystorePasswd = value.trim();
		}
		
		public void setConnection(String clientId, String clientSecret)
		{
			if (clientId != null) _clientId = clientId.trim();
			if (clientSecret != null) _clientSecret = clientSecret.trim();
		}

		public String _clientId = "";
		public String _clientSecret = "";
		public String _keystoreFile = "keystore.jks";	// Within running directory
		public String _keystorePasswd = "Welcome1";
	}

	Stage m_dialog = new Stage();

	// Controls defined within the FXML configuration
	@FXML private TabPane		layout;
	
	// ADS Properties
	@FXML private TextField		f_host;
	@FXML private TextField		f_service;
	@FXML private TextField		f_user;
	@FXML private TextField		f_appId;
	@FXML private TextField		f_position;
	@FXML private Button		f_ads_connectButton;
	
	// Real-Time -- Optimized V1 Properties
	@FXML private TextField		f_machineId;
	@FXML private TextField		f_password;
	@FXML private TextField		f_appKey;
	@FXML private Button		f_rto_v1_connectButton;

	// Real-Time -- Optimized V2 Properties
	@FXML private TextField		f_clientId;
	@FXML private TextField		f_clientSecret;
	@FXML private Button		f_rto_v2_connectButton;

	// Connection/registration parameters
	ADSConnection				m_adsConnection = new ADSConnection();
	RTO_V1Connection			m_rto_v1_Connection = new RTO_V1Connection();
	RTO_V2Connection			m_rto_v2_Connection = new RTO_V2Connection();

	// EMA consumer
	SpeedGuideConsumer	m_consumer;

	public void initialize(Application.Parameters cmdLineParams, SpeedGuideConsumer consumer) 
	{
		m_adsConnection.setConnection(cmdLineParams);
		m_rto_v1_Connection.setConnection(cmdLineParams);
		m_rto_v2_Connection.setConnection(cmdLineParams);
		m_consumer = consumer;

		// Define the main viewing scene,
		Scene scene = new Scene(layout, layout.getPrefWidth(), layout.getPrefHeight());

		// Assign to our main stage and show the application to the end user
		m_dialog.setTitle("Connection Values");
		m_dialog.setScene(scene);
		m_dialog.initModality(Modality.APPLICATION_MODAL);
		m_dialog.initStyle(StageStyle.UTILITY);
		m_dialog.setResizable(false);
		
		// Bind properties
		f_ads_connectButton.disableProperty().bind(Bindings.isEmpty(f_host.textProperty())
											 .or(Bindings.isEmpty(f_service.textProperty())));
		f_rto_v1_connectButton.disableProperty().bind(Bindings.isEmpty(f_machineId.textProperty())
												.or(Bindings.isEmpty(f_password.textProperty())
												.or(Bindings.isEmpty(f_appKey.textProperty()))));
		f_rto_v2_connectButton.disableProperty().bind(Bindings.isEmpty(f_clientId.textProperty())
												.or(Bindings.isEmpty(f_clientSecret.textProperty())));												
	}

	@FXML
	private void clickedADSConnect() {
    	m_dialog.close();
    	m_adsConnection.setConnection(f_host.getText(), f_service.getText(), f_user.getText(), f_appId.getText(), f_position.getText());
    	m_consumer.defineADSConsumer(m_adsConnection);
	}
	
	@FXML
	private void clickedRTO_V1Connect() {
    	m_dialog.close();
    	m_rto_v1_Connection.setConnection(f_machineId.getText(), f_password.getText(), f_appKey.getText());
    	m_consumer.defineRTO_V1Consumer(m_rto_v1_Connection);
	}

	@FXML
	private void clickedRTO_V2Connect() {
    	m_dialog.close();
    	m_rto_v2_Connection.setConnection(f_clientId.getText(), f_clientSecret.getText());
    	m_consumer.defineRTO_V2Consumer(m_rto_v2_Connection);
	}

	@FXML
	private void onADSKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !f_ads_connectButton.isDisabled())
			clickedADSConnect();
	}
	
	@FXML
	private void onRTO_V1KeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !f_rto_v1_connectButton.isDisabled())
			clickedRTO_V1Connect();
	}

	@FXML
	private void onRTO_V2KeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !f_rto_v2_connectButton.isDisabled())
			clickedRTO_V2Connect();
	}

	// Connect
	// Make an attempt to connect to a streaming service.  Based on the parameters chosen, we select the appropriate
	// server, i.e. ADS or RTO (RealTime Optimized).
	public void connect()
	{
		switch (getConnectionSelection()) {
			case 0:
				ads_connect();
				break;
			case 1:
				rto_v1_connect();
				break;
			case 2:
				rto_v2_connect();
				break;
			default:
				ads_connect();
				break;
		}
	}
	
	private int getConnectionSelection()
	{
		if ( !m_adsConnection._host.isEmpty() || !m_adsConnection._service.isEmpty())
			return 0;
		else if ( !m_rto_v1_Connection._machineId.isEmpty() || !m_rto_v1_Connection._appKey.isEmpty() || !m_rto_v1_Connection._password.isEmpty() )
			return 1;			
		else if ( !m_rto_v2_Connection._clientId.isEmpty() || !m_rto_v2_Connection._clientSecret.isEmpty())
			return 2;
		else
			return 0;
	}
	
	private void ads_connect()
	{
		if ( m_adsConnection._host.isEmpty() || m_adsConnection._service.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineADSConsumer(m_adsConnection);
	}
	
	private void rto_v1_connect()
	{
		if ( m_rto_v1_Connection._machineId.isEmpty() || m_rto_v1_Connection._password.isEmpty() || m_rto_v1_Connection._appKey.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineRTO_V1Consumer(m_rto_v1_Connection);
	}

	private void rto_v2_connect()
	{
		if ( m_rto_v2_Connection._clientId.isEmpty() || m_rto_v2_Connection._clientSecret.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineRTO_V2Consumer(m_rto_v2_Connection);
	}

	// Display the connection Dialog - index:0 (ADS), index:1 (RTO V2 Authentication), index:2 (RTO V1 Authentication)
	public void connectionDialog()
    {
		int index = getConnectionSelection();
		
		// ADS Properties
		f_host.setText(m_adsConnection._host);
		f_service.setText(m_adsConnection._service);
		f_user.setText(m_adsConnection._user);
		f_appId.setText(m_adsConnection._appId);
		f_position.setText(m_adsConnection._position);

		// RTO V1 Properties
		f_machineId.setText(m_rto_v1_Connection._machineId);
		f_password.setText(m_rto_v1_Connection._password);
		f_appKey.setText(m_rto_v1_Connection._appKey);

		// RTO V2 Properties
		f_clientId.setText(m_rto_v2_Connection._clientId);
		f_clientSecret.setText(m_rto_v2_Connection._clientSecret);
		
		SingleSelectionModel<Tab> selectionModel = layout.getSelectionModel();
		selectionModel.clearAndSelect(index);
		m_dialog.showAndWait();
    }
}
