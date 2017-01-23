package com.thomsonreuters.ema.example.gui.SpeedGuide.view;

import com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuideConsumer;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
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
	class Connection
	{
		public void setConnection(String host, String service, String user)
		{
			_host = host==null ? "" : host.trim();
			_service = service==null ? "" : service.trim();
			_user = user==null ? "" : user.trim();
		}

		public String _host;
		public String _service;
		public String _user;
	}

	Stage m_dialog = new Stage();

	// Controls defined within the FXML configuration
	@FXML private AnchorPane	layout;
	@FXML private TextField		host;
	@FXML private TextField		service;
	@FXML private TextField		user;
	@FXML private Button		connect;

	// Connection/registration parameters
	Connection		m_connection = new Connection();

	// EMA consumer
	SpeedGuideConsumer	m_consumer;

	public void initialize(String hostname, String serviceName, String user, SpeedGuideConsumer consumer) {
		m_connection.setConnection(hostname, serviceName, user);
		m_consumer = consumer;

		// Define the main viewing scene,
		Scene scene = new Scene(layout, layout.getPrefWidth(), layout.getPrefHeight());

		// Assign to our main stage and show the application to the end user
		m_dialog.setTitle("Elektron Connection Values");
		m_dialog.setScene(scene);
		m_dialog.initModality(Modality.APPLICATION_MODAL);
		m_dialog.initStyle(StageStyle.UTILITY);
		m_dialog.setResizable(false);

		connect.disableProperty().bind(Bindings.isEmpty(host.textProperty()).or(Bindings.isEmpty(service.textProperty())));
	}

	@FXML
	private void clickedConnect() {
    	m_dialog.close();
    	m_connection.setConnection(host.getText(), service.getText(), user.getText());
    	m_consumer.defineConsumer(m_connection._host, m_connection._service, m_connection._user);
	}

	@FXML
	private void onKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !connect.isDisabled())
			clickedConnect();
	}

	public void connect()
	{
		if ( m_connection._host.isEmpty() || m_connection._service.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineConsumer(m_connection._host, m_connection._service, m_connection._user);
	}

	public void connectionDialog()
    {
		host.setText(m_connection._host);
		service.setText(m_connection._service);
		user.setText(m_connection._user);

		m_dialog.showAndWait();
    }
}
