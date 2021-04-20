package com.refinitiv.ema.example.gui.SpeedGuide.view;

import com.refinitiv.ema.example.gui.SpeedGuide.SpeedGuideConsumer;

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

	public class ElektronConnection
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
	
	public class ERTConnection
	{
		// Command-line parameter names
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
		public String _keystoreFile = "keystore.jks";	// Within running directory
		public String _keystorePasswd = "Welcome1";
	}

	Stage m_dialog = new Stage();

	// Controls defined within the FXML configuration
	@FXML private TabPane		layout;
	
	// Elektron Properties
	@FXML private TextField		f_host;
	@FXML private TextField		f_service;
	@FXML private TextField		f_user;
	@FXML private TextField		f_appId;
	@FXML private TextField		f_position;
	@FXML private Button		f_elektron_connectButton;
	
	// Real-Time -- Optimized Properties
	@FXML private TextField		f_machineId;
	@FXML private TextField		f_password;
	@FXML private TextField		f_appKey;
	@FXML private Button		f_ert_connectButton;

	// Connection/registration parameters
	ElektronConnection			m_elektronConnection = new ElektronConnection();
	ERTConnection				m_ertConnection = new ERTConnection();

	// EMA consumer
	SpeedGuideConsumer	m_consumer;

	public void initialize(Application.Parameters cmdLineParams, SpeedGuideConsumer consumer) 
	{
		m_elektronConnection.setConnection(cmdLineParams);
		m_ertConnection.setConnection(cmdLineParams);
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
		f_elektron_connectButton.disableProperty().bind(Bindings.isEmpty(f_host.textProperty()).or(Bindings.isEmpty(f_service.textProperty())));
		f_ert_connectButton.disableProperty().bind(Bindings.isEmpty(f_machineId.textProperty()).or(Bindings.isEmpty(f_password.textProperty())
																							   .or(Bindings.isEmpty(f_appKey.textProperty()))));
	}

	@FXML
	private void clickedElektronConnect() {
    	m_dialog.close();
    	m_elektronConnection.setConnection(f_host.getText(), f_service.getText(), f_user.getText(), f_appId.getText(), f_position.getText());
    	m_consumer.defineElektronConsumer(m_elektronConnection);
	}
	
	@FXML
	private void clickedERTConnect() {
    	m_dialog.close();
    	m_ertConnection.setConnection(f_machineId.getText(), f_password.getText(), f_appKey.getText());
    	m_consumer.defineERTConsumer(m_ertConnection);
	}

	@FXML
	private void onElektronKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !f_elektron_connectButton.isDisabled())
			clickedElektronConnect();
	}
	
	@FXML
	private void onERTKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !f_ert_connectButton.isDisabled())
			clickedERTConnect();
	}

	// Connect
	// Make an attempt to connect to a streaming service.  Based on the parameters chosen, we select the appropriate
	// server, i.e. Elektron or ERT in Cloud.
	public void connect()
	{
		if (IsElektron())
			elektron_connect();
		else
			ert_connect();
	}
	
	private boolean IsElektron()
	{
		if ( !m_elektronConnection._host.isEmpty() || !m_elektronConnection._service.isEmpty())
			return true;
		else if ( !m_ertConnection._machineId.isEmpty() || !m_ertConnection._appKey.isEmpty() || !m_ertConnection._password.isEmpty() )
			return false;
		else
			return true;
	}
	
	private void elektron_connect()
	{
		if ( m_elektronConnection._host.isEmpty() || m_elektronConnection._service.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineElektronConsumer(m_elektronConnection);
	}
	
	private void ert_connect()
	{
		if ( m_ertConnection._machineId.isEmpty() || m_ertConnection._password.isEmpty() || m_ertConnection._appKey.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineERTConsumer(m_ertConnection);
	}

	// Display the connection Dialog - index:0 (Elektron), index:1 (ERT in Cloud)
	public void connectionDialog()
    {
		int index = IsElektron() ? 0 : 1;
		
		// Elektron Properties
		f_host.setText(m_elektronConnection._host);
		f_service.setText(m_elektronConnection._service);
		f_user.setText(m_elektronConnection._user);
		f_appId.setText(m_elektronConnection._appId);
		f_position.setText(m_elektronConnection._position);

		// ERT Properties
		f_machineId.setText(m_ertConnection._machineId);
		f_password.setText(m_ertConnection._password);
		f_appKey.setText(m_ertConnection._appKey);
		
		SingleSelectionModel<Tab> selectionModel = layout.getSelectionModel();
		selectionModel.clearAndSelect(index);
		m_dialog.showAndWait();
    }
}
