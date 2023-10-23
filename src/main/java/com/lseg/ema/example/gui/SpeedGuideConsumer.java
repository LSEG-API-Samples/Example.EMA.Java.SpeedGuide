package com.lseg.ema.example.gui;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.lseg.ema.example.gui.SpeedGuide.StatusIndicator;
import com.lseg.ema.example.gui.view.SpeedGuideViewController;
import com.lseg.ema.example.gui.view.SpeedGuideConnection.RTO_V1Connection;
import com.lseg.ema.example.gui.view.SpeedGuideConnection.RTO_V2Connection;
import com.lseg.ema.example.gui.view.SpeedGuideConnection.ADSConnection;
import com.refinitiv.ema.access.AckMsg;
import com.refinitiv.ema.access.ElementEntry;
import com.refinitiv.ema.access.ElementList;
import com.refinitiv.ema.access.EmaFactory;
import com.refinitiv.ema.access.FilterEntry;
import com.refinitiv.ema.access.FilterList;
import com.refinitiv.ema.access.GenericMsg;
import com.refinitiv.ema.access.Map;
import com.refinitiv.ema.access.MapEntry;
import com.refinitiv.ema.access.Msg;
import com.refinitiv.ema.access.OmmConsumer;
import com.refinitiv.ema.access.OmmConsumerClient;
import com.refinitiv.ema.access.OmmConsumerConfig;
import com.refinitiv.ema.access.OmmConsumerErrorClient;
import com.refinitiv.ema.access.OmmConsumerEvent;
import com.refinitiv.ema.access.OmmException;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.ReqMsg;
import com.refinitiv.ema.access.ServiceEndpointDiscovery;
import com.refinitiv.ema.access.ServiceEndpointDiscoveryClient;
import com.refinitiv.ema.access.ServiceEndpointDiscoveryEvent;
import com.refinitiv.ema.access.ServiceEndpointDiscoveryInfo;
import com.refinitiv.ema.access.ServiceEndpointDiscoveryOption;
import com.refinitiv.ema.access.ServiceEndpointDiscoveryResp;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;
import com.refinitiv.ema.access.DataType.DataTypes;
import com.refinitiv.ema.rdm.EmaRdm;

/**
 * The SpeedGuideConsumer class
 *
 */
public class SpeedGuideConsumer implements Runnable
{
	public static final String DEFAULT_REGION = "<default>";
	public ConsumerClient _consumerClient = new ConsumerClient();
	protected OmmConsumer _consumer = null;
	private boolean _launchConsumer = false;

	private String _region = DEFAULT_REGION;
	private String _service;
	
	// ADS connection parameters
	private ADSConnection _adsConnection;
	
	// RTO V1 connection parameters
	private RTO_V1Connection _rto_v1_connection;

	// RTO V2 connection parameters
	private RTO_V2Connection _rto_v2_connection;
	
	private boolean _debug = false;
	private StatusLogHandler m_statusLogHandler;

	private SpeedGuideViewController m_viewController;

	public void setViewController(SpeedGuideViewController viewController) {
		m_viewController = viewController;
		m_statusLogHandler.setViewController(viewController);
		_consumerClient.setViewController(viewController);
		m_viewController.addRegion(_region);			// Default Region as defined by EMA
	}

	public SpeedGuideConsumer()	{
		setLogHandler();
		_consumerClient.setConsumer(this);
	}

	public void setLogHandler()
	{
		final Logger parentLogger = Logger.getAnonymousLogger().getParent();
		m_statusLogHandler = new StatusLogHandler();
		parentLogger.addHandler(m_statusLogHandler);
	}

	public void setDebug(boolean debug)
	{
		_debug = debug;
		_consumerClient.setDebug(debug);

		if (!debug)
		{
			final Logger parentLogger = Logger.getAnonymousLogger().getParent();

			// Disable console logger if debug mode not turned on
			for (Handler handler : parentLogger.getHandlers())
			{
				if ( handler instanceof ConsoleHandler )
					parentLogger.removeHandler(handler);
			}
		}
	}

	public boolean isConnectionRTO()
	{
		return _rto_v1_connection != null || _rto_v2_connection!= null;
	}

	public void defineADSConsumer(ADSConnection connectionParams)
	{
		_adsConnection = connectionParams;

		String userName = (connectionParams._user.isEmpty() ? "<Desktop Login>" : "user");
        if (_debug) System.out.println("Connecting to ADS @ host:[" + 
									connectionParams._host + "] service: [" + 
									connectionParams._service == null ? "<default" : connectionParams._service + 
									"] User: [" +
									userName +"] applicationID: [" + 
									connectionParams._appId + "] position: [" + 
									connectionParams._position + "]");

		_rto_v1_connection = null;
		_rto_v2_connection = null;
        _launchConsumer = true;
	}
	
	public void defineRTO_V1Consumer(RTO_V1Connection connectionParams)
	{
		_rto_v1_connection = connectionParams;
		
        if (_debug) 
        {
        	System.out.println("Connecting to Real-Time -- Optimized @ machineID:[" + 
									connectionParams._machineId + "] password: [" + 
									connectionParams._password + "] AppKey: [" + 
									connectionParams._appKey + "] service: [" + 
									connectionParams._service == null ? "<default>" : connectionParams._service);
        	System.out.println("Using keystore file: " + connectionParams._keystoreFile);
        }
        
		_adsConnection = null;
		_rto_v2_connection = null;
        _launchConsumer = true;
	}

	public void defineRTO_V2Consumer(RTO_V2Connection connectionParams)
	{
		_rto_v2_connection = connectionParams;
		
        if (_debug) 
        {
        	System.out.println("Connecting to Real-Time -- Optimized @ clientID:[" + connectionParams._clientId + 
									"] clientSecret: [" + connectionParams._clientSecret + "] service: [" +
									connectionParams._service == null ? "<default>" : connectionParams._service);
        	System.out.println("Using keystore file: " + connectionParams._keystoreFile);
        }
        
		_adsConnection = null;
		_rto_v1_connection = null;
        _launchConsumer = true;
	}

	public void defineNewRegion()
	{
		// We simply notify our run loop to attempt to connect - this is because we selected a new region
		_launchConsumer = true;
	}

	public void setRegion(String region)
	{
		if (region != null) {
			_region = region.trim();
			m_viewController.addRegion(_region);
		}
	}

	public String getRegion()
	{
		return(_region);
	}

	public void setService(String service)
	{
		_service = service.trim();
	}

	public String getService()
	{
		return(_service);
	}
	
	private void connectConsumer()
	{
		if (_adsConnection != null)
			connectADS();
		else if (_rto_v1_connection != null || _rto_v2_connection != null)
			connectRTO();
	}

	private void connectADS()
	{
		_launchConsumer = false;

		String connectStr = "Attempting to connect ADS: [" + _adsConnection._host + "]";
		m_viewController.setConnection(false, true, "Connection to ADS in progress...");
		m_viewController.updateStatus(connectStr, StatusIndicator.REQUEST);
		OmmConsumerConfig config = EmaFactory.createOmmConsumerConfig().host(_adsConnection._host);

		// We have to set the user/appId/position in this fashion because a blank value may not default correctly.
		if ( !_adsConnection._user.isEmpty() ) config.username(_adsConnection._user);
		if ( !_adsConnection._appId.isEmpty() ) config.applicationId(_adsConnection._appId);
		if ( !_adsConnection._position.isEmpty() ) config.position(_adsConnection._position);
		if ( !_adsConnection._service.isEmpty() ) _service = _adsConnection._service;

		// Register Consumer
		registerConsumer(config);
	}
	
	private void connectRTO()
	{
		_launchConsumer = false;

		// Determine if something is already running...
		if (_consumer != null) {
			_consumer.uninitialize();
			_consumer = null;
		}

		String connectStr = "Attempting to connect Real-Time -- Optimized";
		m_viewController.setConnection(false, true, "Connection to Real-Time -- Optimized in progress...");
		m_viewController.updateStatus(connectStr, StatusIndicator.REQUEST);
		
		// Configuration
		// Because we are supporting 2 RTO Authentication mechanisms, we'll need to prioritize which one to use
		// if both are populated with data.
		OmmConsumerConfig config;
		if (_rto_v2_connection != null) {
			config = EmaFactory.createOmmConsumerConfig().clientId(_rto_v2_connection._clientId)
														 .clientSecret(_rto_v2_connection._clientSecret);
		}
		else {
			config = EmaFactory.createOmmConsumerConfig().username(_rto_v1_connection._machineId)
														 .password(_rto_v1_connection._password)
														 .clientId(_rto_v1_connection._appKey);
		}

		Map configDb = EmaFactory.createMap();
		
		// Service discovery - we query ERT for the available services
		ServiceEndpointDiscovery serviceDiscovery = EmaFactory.createServiceEndpointDiscovery();

		ServiceEndpointDiscoveryOption options = EmaFactory.createServiceEndpointDiscoveryOption();

		String keyStoreFile;
		String keyStorePasswd;
		if (_rto_v2_connection != null) {
			options.clientId(_rto_v2_connection._clientId)
				   .clientSecret(_rto_v2_connection._clientSecret);
			keyStoreFile = _rto_v2_connection._keystoreFile;
			keyStorePasswd = _rto_v2_connection._keystorePasswd;
			if ( !_rto_v2_connection._service.isEmpty() ) _service = _rto_v2_connection._service;
		}
		else {
			options.username(_rto_v1_connection._machineId)
				   .password(_rto_v1_connection._password)
				   .clientId(_rto_v1_connection._appKey);
			keyStoreFile = _rto_v1_connection._keystoreFile;
			keyStorePasswd = _rto_v1_connection._keystorePasswd;
			if ( !_rto_v1_connection._service.isEmpty() ) _service = _rto_v1_connection._service;
		}

		// Capture service discovery events
		serviceDiscovery.registerClient(
							options.transport(ServiceEndpointDiscoveryOption.TransportProtocol.TCP),
							//.proxyHostName(proxyHostName)
							//.proxyPort(proxyPort)
							//.proxyUserName(proxyUserName)
							//.proxyPassword(proxyPassword)
							//.proxyDomain(proxyDomain)
							//.proxyKRB5ConfigFile(proxyKrb5Configfile)
				new ServiceEndpointDiscoveryClient() {
					public void onSuccess(ServiceEndpointDiscoveryResp serviceEndpointResp, ServiceEndpointDiscoveryEvent event)
					{
						if (_debug)
							System.out.println("Service Discovery:\n" + serviceEndpointResp); // dump service discovery endpoints
						
						for(int index = 0; index < serviceEndpointResp.serviceEndpointInfoList().size(); index++)
						{
							ServiceEndpointDiscoveryInfo location = serviceEndpointResp.serviceEndpointInfoList().get(index);
							m_viewController.addRegion(getRegion(location.locationList()));
						}		
						m_viewController.setRTOConnection(true);
					}

					private String getRegion(List<String> locationList) {
						// Just need to retrieve the 1st element to map region
						String region = locationList.get(0);
						if (region.matches(".*[a-z]$"))
							return region.substring(0, region.length()-1);
						else
							return region;
					}

					public void onError(String errorText, ServiceEndpointDiscoveryEvent event)
					{
						m_viewController.updateStatus("Failed to query service discovery. [" + errorText + "]", StatusIndicator.RESPONSE_ERROR);
						m_viewController.setConnection(false, false, "Connection Failed.");
					}
				});
		
		// Create an in-memory configuration (only if we were able to connect and retrieve and endpoint)
		createProgramaticConfig(configDb);
		config.config(configDb);
		config.consumerName("Consumer_1");
		
		// Define keystore files used for encryption
		config.tunnelingKeyStoreFile(keyStoreFile);
		config.tunnelingKeyStorePasswd(keyStorePasswd);
		
		// Register Consumer
		registerConsumer(config);
	}
	
	// registerConsumer
	// Creates our OMM Consumer to capture directory and market data requests
	private void registerConsumer(OmmConsumerConfig config)
	{
		// Create our consumer of market data	
		_consumer = EmaFactory.createOmmConsumer(config,
				new OmmConsumerErrorClient() {
					public void onInvalidHandle(long handle, String text) {
						m_viewController.updateStatus("OnInvalidHandle: " + text, StatusIndicator.RESPONSE_ERROR);
					}

					public void onInvalidUsage(String text) {
						if (m_viewController.isConnecting()) {
							String msg = "Connection Failed. " + text;
							m_viewController.setConnection(false, false, msg);
						}
					}
				});

		ReqMsg reqMsg = EmaFactory.createReqMsg();
		
		// Register interest in the Directory information to capture the list of Services for this connection
		_consumer.registerClient(reqMsg.domainType(EmaRdm.MMT_DIRECTORY),
				new OmmConsumerClient() {
					public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent){}
					public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent){}
					public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent){}
					public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent consumerEvent) {
						decode(refreshMsg.payload().map());
					}
					public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent consumerEvent){}
					public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent consumerEvent){}

					void decode(ElementList elementList)
					{
						for (ElementEntry elementEntry : elementList)
						{
							if ( elementEntry.name().equals("Name") )
							{
								String service = elementEntry.ascii().toString();
								
								// Set the default SERVICE based on the 1st one found - usually ELEKTRON_DD
								if (_service == null)
									_service = service;
									
								m_viewController.addService(service);
								m_viewController.updateStatus("Discovered Service: " + service, StatusIndicator.RESPONSE_SUCCESS);
								break;
							}
						}
					}

					void decode(Map map)
					{
						for(MapEntry mapEntry : map)
						{
							switch (mapEntry.loadType())
							{
								case DataTypes.FILTER_LIST :
									decode(mapEntry.filterList());
									break;
							}
						}
					}

					void decode(FilterList filterList)
					{
						for(FilterEntry filterEntry : filterList)
						{
							switch (filterEntry.loadType())
							{
								case DataTypes.ELEMENT_LIST :
									decode(filterEntry.elementList());
									break;
								case DataTypes.MAP :
									decode(filterEntry.map());
									break;
							}
						}
					}
				});
		_consumer.registerClient(reqMsg.domainType(EmaRdm.MMT_LOGIN),
					new OmmConsumerClient() {
						public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent){}
						public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent){}
						public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent) {}
						public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent consumerEvent)
						{
							m_viewController.updateStatus(refreshMsg.state().statusText(), StatusIndicator.RESPONSE_SUCCESS);
							m_viewController.setConnection(true, false, "");
							subscribe("REFINITIV");
						}
						public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent consumerEvent){}
						public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent consumerEvent){}
				});
	}
	
	private void createProgramaticConfig(Map configDb)
	{
		Map elementMap = EmaFactory.createMap();
		ElementList elementList = EmaFactory.createElementList();
		ElementList innerElementList = EmaFactory.createElementList();
		
		innerElementList.add(EmaFactory.createElementEntry().ascii("Channel", "Channel_1"));
		
		elementMap.add(EmaFactory.createMapEntry().keyAscii("Consumer_1", MapEntry.MapAction.ADD, innerElementList));
		innerElementList.clear();
		
		elementList.add(EmaFactory.createElementEntry().map("ConsumerList", elementMap));
		elementMap.clear();
		
		configDb.add(EmaFactory.createMapEntry().keyAscii("ConsumerGroup", MapEntry.MapAction.ADD, elementList));
		elementList.clear();
		
		innerElementList.add(EmaFactory.createElementEntry().ascii("ChannelType", "ChannelType::RSSL_ENCRYPTED"));

		if ( !_region.equals(DEFAULT_REGION) )
			innerElementList.add(EmaFactory.createElementEntry().ascii("Location", _region));

		innerElementList.add(EmaFactory.createElementEntry().intValue("EnableSessionManagement", 1));
		innerElementList.add(EmaFactory.createElementEntry().intValue("CompressionThreshold", 300));
		
		elementMap.add(EmaFactory.createMapEntry().keyAscii("Channel_1", MapEntry.MapAction.ADD, innerElementList));
		innerElementList.clear();
		
		elementList.add(EmaFactory.createElementEntry().map("ChannelList", elementMap));
		elementMap.clear();
		
		configDb.add(EmaFactory.createMapEntry().keyAscii("ChannelGroup", MapEntry.MapAction.ADD, elementList));
	}

	/**
	 * Subscribe to RIC
	 * @param ric
	 */

	public void subscribe(String ric) {
		try {
			if (!ric.isEmpty())
			{
				if (_debug) System.out.println("Registering interest in item: " + ric);
					_consumer.registerClient(EmaFactory.createReqMsg().serviceName(_service)
																	  .name(ric)
																	  .interestAfterRefresh(false),
													_consumerClient);
			}
		} catch (OmmException excp) {
			m_viewController.updateStatus(excp.getMessage(), StatusIndicator.RESPONSE_ERROR);
		}
	}

    public void run()
    {
    	while(true)
        {
        	long time = (m_viewController.isConnected() ? 1000 : 10);
            try {
            	if (_launchConsumer)
               		connectConsumer();

                Thread.sleep(time);
            } catch(InterruptedException ie){}
        }
    }
}
