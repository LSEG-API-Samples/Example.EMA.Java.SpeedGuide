package com.thomsonreuters.ema.example.gui.SpeedGuide.view;

import com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuide;
import com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuideConsumer;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

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

	private Dialog<Connection> m_dialog = new Dialog<>();

	// Connection/registration parameters
	Connection		m_connection = new Connection();

	// EMA consumer
	SpeedGuideConsumer	m_consumer;

	// Connection parameters
	private TextField m_host = new TextField();
	private TextField m_service = new TextField();
	private TextField m_user = new TextField();


	public SpeedGuideConnection() {
    	m_dialog.setTitle("Elektron Connection Values");
    	m_dialog.setHeaderText("Elektron/TREP Connection parameters:");
    	m_dialog.setResizable(false);

    	Label label1 = new Label("Host: ");
    	Label desc1 = new Label("Required. Hostname/IP of Elektron server." + SpeedGuide.NEWLINE + "Syntax: hostname:port.  Eg: elektron:14002");
    	Label label2 = new Label("Service: ");
    	Label desc2 = new Label("Required. Market Data service." + SpeedGuide.NEWLINE + "Eg: ELEKTRON_DD");
    	Label label3 = new Label("User: ");
    	Label desc3 = new Label("Optional. Login username." + SpeedGuide.NEWLINE + "Default: desktop login");

    	GridPane grid = new GridPane();
    	grid.setGridLinesVisible(false);
    	grid.setVgap(10);
    	grid.setHgap(10);
    	grid.setPadding(new Insets(10,60,10,10));
    	grid.add(label1, 1, 1);
    	grid.add(m_host, 2, 1);
    	grid.add(desc1, 3, 1);
    	grid.add(label2, 1, 2);
    	grid.add(m_service, 2, 2);
    	grid.add(desc2, 3, 2);
    	grid.add(label3, 1, 3);
    	grid.add(m_user, 2, 3);
    	grid.add(desc3, 3, 3);
    	m_dialog.getDialogPane().setContent(grid);

   		ButtonType buttonTypeOk = new ButtonType("Connect", ButtonData.OK_DONE);
   		m_dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
   		m_dialog.getDialogPane().lookupButton(buttonTypeOk);

   		// Disable the button when either the host (text1) or service (text2) are not defined
   		m_dialog.getDialogPane().lookupButton(buttonTypeOk).disableProperty().bind(
   				Bindings.isEmpty(m_host.textProperty()).or(Bindings.isEmpty(m_service.textProperty())));

    	m_dialog.setResultConverter(new Callback<ButtonType, Connection>()
    	{
    	    @Override
    	    public Connection call(ButtonType b)
    	    {
    	        if (b == buttonTypeOk)
    	        {
    	        	m_connection.setConnection(m_host.getText(), m_service.getText(), m_user.getText());
    	        	return(m_connection);
    	        }

    	        return null;
    	    }
    	});
	}

	public void initialize(String host, String service, String user, SpeedGuideConsumer consumer) {
		m_connection.setConnection(host, service, user);
		m_consumer = consumer;
	}

	public void connect()
	{
		if ( m_connection._host.isEmpty() || m_connection._service.isEmpty() )
    		connectionDialog();
    	else
    		m_consumer.defineConsumer(m_connection._host, m_connection._service, m_connection._user);
	}

	public boolean connectionDialog()
    {
		m_host.setText(m_connection._host);
		m_service.setText(m_connection._service);
		m_user.setText(m_connection._user);

    	boolean connect = m_dialog.showAndWait().isPresent();

    	if ( connect )
    		m_consumer.defineConsumer(m_connection._host, m_connection._service, m_connection._user);

    	return(connect);
    }
}
