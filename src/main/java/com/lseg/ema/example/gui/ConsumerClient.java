package com.lseg.ema.example.gui;

import java.util.ArrayList;
import java.util.List;

import com.lseg.ema.example.gui.SpeedGuide.StatusIndicator;
import com.lseg.ema.example.gui.view.SpeedGuideViewController;
import com.refinitiv.ema.access.AckMsg;
import com.refinitiv.ema.access.Data;
import com.refinitiv.ema.access.DataType;
import com.refinitiv.ema.access.DataType.DataTypes;
import com.refinitiv.ema.access.EmaFactory;
import com.refinitiv.ema.access.FieldEntry;
import com.refinitiv.ema.access.FieldList;
import com.refinitiv.ema.access.GenericMsg;
import com.refinitiv.ema.access.Msg;
import com.refinitiv.ema.access.OmmConsumerClient;
import com.refinitiv.ema.access.OmmConsumerEvent;
import com.refinitiv.ema.access.RefreshMsg;
import com.refinitiv.ema.access.RmtesBuffer;
import com.refinitiv.ema.access.StatusMsg;
import com.refinitiv.ema.access.UpdateMsg;

public class ConsumerClient implements OmmConsumerClient {
    private RmtesBuffer _rmtesBuffer = EmaFactory.createRmtesBuffer();
	private String _compoundPage = "";
	private int _compoundPageTotalCount =0;
	private int _compoundPageCount =0;
	private boolean _isCompoundPage = false;
	private List <String> _compoundPageRicList = new ArrayList<String>();
	private String _compoundPageRIC ="";
	private String _previousRicInChain ="";
	private boolean m_debug = false;
	private SpeedGuideViewController m_viewController;
	private SpeedGuideConsumer m_consumer;

	public void setConsumer(SpeedGuideConsumer consumer) {
		m_consumer = consumer;
	}

	public void setViewController(SpeedGuideViewController viewController) {
		m_viewController = viewController;
	}

	public void setDebug(boolean debug) {
		m_debug = debug;
	}

	/**
	 * onRefreshMsg
	 * Handles REFRESH messages
	 */

	public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event)
	{
		String item = (refreshMsg.hasName() ? refreshMsg.name() : "<not set>");
		String service = (refreshMsg.hasServiceName() ? refreshMsg.serviceName() : "<not set>");

		String status = "Item: " + item	+ " / Service: " + service;

		if (m_debug )
		{
			System.out.println("REFRESH");
			System.out.println("Item Name: " + item);
			System.out.println("Service Name: " + service);
			System.out.println("State: " + refreshMsg.state());
		}

		status += " [State: " + refreshMsg.state() + "]";
		m_viewController.updateStatus(status, StatusIndicator.RESPONSE_SUCCESS);

		if (DataType.DataTypes.FIELD_LIST == refreshMsg.payload().dataType())
			decodeRefresh(refreshMsg.payload().fieldList(), item);

		if (m_debug) System.out.println();
	}

	/**
	 * onUpdateMsg
	 * Handles the UPDATE Messages
	 */
	public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event)
	{
		System.out.println("Updates not yet supported");
	}

	public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event)
	{
		String item = (statusMsg.hasName() ? statusMsg.name() : "<not set>");
		String service = (statusMsg.hasServiceName() ? statusMsg.serviceName() : "<not set>");

		String status = "Item: " + item + " / Service: " + service;

		if (m_debug)
		{
			System.out.println("STATUS MSG");
			System.out.println("Item Name: " + item);
			System.out.println("Service Name: " + service);
		}

		if (statusMsg.hasState()) {
			status += " [State: " +statusMsg.state() + "]";
			if (m_debug) System.out.println("State: " +statusMsg.state());
		}

		if (m_debug) System.out.println();
		m_viewController.updateStatus(status, StatusIndicator.RESPONSE_ERROR, item);
	}

	public void onGenericMsg(GenericMsg genericMsg, OmmConsumerEvent consumerEvent){}
	public void onAckMsg(AckMsg ackMsg, OmmConsumerEvent consumerEvent){}
	public void onAllMsg(Msg msg, OmmConsumerEvent consumerEvent){}


	/**
	 * Decode REFRESH Messages
	 *
	 * @param fieldList
	 * @param ric
	 */
	void decodeRefresh(FieldList fieldList, String ric)
	{
		String pageStr = "";
		String quoteStr = "";
		boolean _isPage = false;
		int width = 21;
		String fieldDisplay;

		long pref_disp = 0, recordtype = 0, rdndisplay = 0;
		List <String> compoundPageRicList = new ArrayList<String>();

		for (FieldEntry fieldEntry : fieldList)
		{
			fieldDisplay = String.format("%-"+width+"s", fieldEntry.name() + " (" + fieldEntry.fieldId() + "):");

			if (m_debug) System.out.print("Fid: " + fieldEntry.fieldId() + " Name = " + fieldEntry.name() + " DataType: " +
												   DataType.asString(fieldEntry.load().dataType()) + " Value: ");

			if (Data.DataCode.BLANK == fieldEntry.code())
			{
				if (m_debug) System.out.println(" blank");
			}
			else
			{
				switch (fieldEntry.fieldId())
				{
					case 215 : // Page record rows (14X64)
					case 216 :
					case 217 :
					case 218 :
					case 219 :
					case 220 :
					case 221 :
					case 222 :
					case 223 :
					case 224 :
					case 225 :
					case 226 :
					case 227 :
					case 228 :
					case 315 : // Page record rows (25x80)
					case 316 :
					case 317 :
					case 318 :
					case 319 :
					case 320 :
					case 321 :
					case 322 :
					case 323 :
					case 324 :
					case 325 :
					case 326 :
					case 327 :
					case 328 :
					case 329 :
					case 330 :
					case 331 :
					case 332 :
					case 333 :
					case 334 :
					case 335 :
					case 336 :
					case 337 :
					case 338 :
					case 339 :
						_isPage = true;
						if (fieldEntry.loadType() == DataTypes.RMTES) {
							String s = (_rmtesBuffer.apply(fieldEntry.rmtes())).toString();
							pageStr += s + SpeedGuide.NEWLINE;
							if (m_debug) System.out.println(s);
						}
						break;
					case 240 :  // These are the RIC list of the Chain
					case 241 :
					case 242 :
					case 243 :
					case 244 :
					case 245 :
					case 246 :
					case 247 :
					case 248 :
					case 249 :
					case 250 :
					case 251 :
					case 252 :
					case 253 :

					case 800 :  // These are the RIC list of the Chain
					case 801 :
					case 802 :
					case 803 :
					case 804 :
					case 805 :
					case 806 :
					case 807 :
					case 808 :
					case 809 :
					case 810 :
					case 811 :
					case 812 :
					case 813 :
					case 814 :

						if (fieldEntry.loadType() == DataTypes.ASCII) {
							String chainRic = fieldEntry.ascii().toString();
							quoteStr += fieldDisplay + "<" + chainRic + ">" + SpeedGuide.NEWLINE;
							if (m_debug) System.out.println(chainRic);
							compoundPageRicList.add(chainRic);
						}
						break;
					case 815 :
					case 1081 :
					case 238 :
						if (fieldEntry.loadType() == DataTypes.ASCII) {
							String s = fieldEntry.ascii().toString();
							quoteStr += fieldDisplay + "<" + s + ">" + SpeedGuide.NEWLINE;
							if (m_debug) System.out.println(s);
						}
						break;
					case 6:
					case 11:
					case 12:
					case 13:
					case 21:
					case 22:
					case 25:
					case 56:
						if (fieldEntry.loadType() == DataTypes.REAL) {
							double last = fieldEntry.real().asDouble();
							if (m_debug) System.out.println(last);
							quoteStr += fieldDisplay + last + SpeedGuide.NEWLINE;
						}
						break;
					case 239 :// Fid: 239 Name = REF_COUNT (CHAIN) DataType: UInt Value: 2
						if (fieldEntry.loadType() == DataTypes.UINT) {
							long ref_count = fieldEntry.uintValue();
							if (m_debug) System.out.println("REF_COUNT =" + fieldEntry.uintValue());
							quoteStr += fieldDisplay + ref_count + SpeedGuide.NEWLINE;
						}
						break;
					case 1080 :// Fid: 1080 Name = PREF_DISP DataType: UInt Value: 1291
						if (fieldEntry.loadType() == DataTypes.UINT) {
							pref_disp = fieldEntry.uintValue();
							if (m_debug) System.out.println("PREF_DISP =" + fieldEntry.uintValue());
							quoteStr += fieldDisplay + pref_disp + SpeedGuide.NEWLINE;
						}
						break;
					case 259 :// Fid: 259 Name = RECORDTYPE DataType: UInt Value: RECORDTYPE =234
						if (fieldEntry.loadType() == DataTypes.UINT) {
							recordtype = fieldEntry.uintValue();
							if (m_debug) System.out.println("RECORDTYPE =" + fieldEntry.uintValue());
							quoteStr += fieldDisplay + recordtype + SpeedGuide.NEWLINE;
						}
						break;
					case 2 :// RDNDISPLAY DataType: UInt Value: 230
						if (fieldEntry.loadType() == DataTypes.UINT) {
							rdndisplay = fieldEntry.uintValue();
							if (m_debug) System.out.println("RDNDISPLAY =" + fieldEntry.uintValue());
							quoteStr += fieldDisplay + rdndisplay + SpeedGuide.NEWLINE;
						}
						break;

					case 3 :// DSPLY_NAME
						if (fieldEntry.loadType() == DataTypes.RMTES) {
							String dsply_name=  (_rmtesBuffer.apply(fieldEntry.rmtes())).toString();
							pageStr += fieldEntry.name() + ": "+ dsply_name+SpeedGuide.NEWLINE;
							if (m_debug) System.out.println( "dsply_name = "+ dsply_name);
							quoteStr += fieldDisplay + dsply_name + SpeedGuide.NEWLINE;
						}
						break;
					default :

						switch (fieldEntry.loadType())
						{
						case DataTypes.RMTES :
							String s = (_rmtesBuffer.apply(fieldEntry.rmtes())).toString();
							if (m_debug)
							{
								System.out.print( " DataType: " + DataType.asString(fieldEntry.load().dataType()) + " Value: ");
								System.out.println(s);
							}
							quoteStr += fieldDisplay + s + SpeedGuide.NEWLINE;
							break;
						case DataTypes.REAL :
							if (m_debug) System.out.println(fieldEntry.real().asDouble());
							quoteStr += fieldDisplay + fieldEntry.real().asDouble() + SpeedGuide.NEWLINE;
							break;
						case DataTypes.DATE :
							if (m_debug) System.out.println(fieldEntry.date().day() + " / " + fieldEntry.date().month() + " / " + fieldEntry.date().year());
							quoteStr += fieldDisplay + fieldEntry.date().day() + " / " + fieldEntry.date().month() + " / " + fieldEntry.date().year() + SpeedGuide.NEWLINE;
							break;
						case DataTypes.TIME :
							if (m_debug) System.out.println(fieldEntry.time().hour() + ":" + fieldEntry.time().minute() + ":" + fieldEntry.time().second() + ":" + fieldEntry.time().millisecond());
							quoteStr += fieldDisplay + fieldEntry.time().hour() + ":" + fieldEntry.time().minute() + ":" +
													   fieldEntry.time().second() + ":" + fieldEntry.time().millisecond() + SpeedGuide.NEWLINE;
							break;
						case DataTypes.INT :
							if (m_debug) System.out.println(fieldEntry.intValue());
							quoteStr += fieldDisplay + fieldEntry.intValue() + SpeedGuide.NEWLINE;
							break;
						case DataTypes.UINT :
							quoteStr += fieldDisplay + fieldEntry.uintValue() + SpeedGuide.NEWLINE;
							if (m_debug) System.out.println(fieldEntry.uintValue());
							break;
						case DataTypes.ASCII :
							if (m_debug) System.out.println(fieldEntry.ascii());
							quoteStr += fieldDisplay + fieldEntry.ascii() + SpeedGuide.NEWLINE;
							break;
						case DataTypes.ENUM :
							if (m_debug) System.out.println(fieldEntry.enumValue());
							quoteStr += fieldDisplay + fieldEntry.enumValue() + SpeedGuide.NEWLINE;
							break;
						case DataTypes.ERROR :
							if (m_debug) System.out.println("(" + fieldEntry.error().errorCodeAsString() + ")");
							break;
						default :
							if (m_debug) System.out.println();
							break;
						}

						break;
				}
			}

		}  // END - for loop - FieldEntry

		// Check if compound Page, if so subscribe to all RICs in chain and concatenate Pages
		//if ( pref_disp == 1291 && recordtype == 234 && rdndisplay == 230 ) {
		if ( pref_disp == 1291 || pref_disp == 3221)
		{
			_isCompoundPage = true;
			_compoundPageCount = 0;
			_compoundPage ="";
			_compoundPageTotalCount = compoundPageRicList.size();
			_compoundPageRIC = ric;

			_compoundPageRicList = new ArrayList<String>(compoundPageRicList);
			if ( _compoundPageCount < _compoundPageTotalCount )
			{
				String compRic = compoundPageRicList.get(_compoundPageCount).trim();
				_previousRicInChain = compRic;
				if (m_debug) System.out.println("Subscribing to compound page Ric="+compRic);
				m_consumer.subscribe(compRic);
			}
			pageStr ="";
		}
		if ( _compoundPageCount < _compoundPageTotalCount ) {
			if (_previousRicInChain.equals(ric)) {
				String compRic = _compoundPageRicList.get(_compoundPageCount).trim();
				_previousRicInChain = compRic;
				if (m_debug) System.out.println("Subscribing to compound page Ric="+compRic);
				m_consumer.subscribe(compRic);
			}
		}

		/**
		 * Fid: 2 Name = RDNDISPLAY DataType: UInt Value: 151
		 * Fid: 259 Name = RECORDTYPE DataType: UInt Value: RECORDTYPE =234
		 * Fid: 5357 Name = CONTEXT_ID DataType: Real Value: 2704.0
		 * Fid: 6401 Name = DDS_DSO_ID DataType: UInt Value: 4118
		 */

		/**
		 * Fid: 2 Name = RDNDISPLAY DataType: UInt Value: RDNDISPLAY =151
		 * Fid: 259 Name = RECORDTYPE DataType: UInt Value: RECORDTYPE =226
		 */

		if ( _isCompoundPage )
		{
			_compoundPage += pageStr;
			_compoundPageCount ++;
			if (m_debug) System.out.println("_compoundPageTotalCount =" + _compoundPageTotalCount + " _compoundPageCount=" +_compoundPageCount);

			if (_compoundPageCount == (_compoundPageTotalCount+1) ) {
				m_viewController.updateTxtArea( _compoundPageRIC, _compoundPage );
				_compoundPageTotalCount = 0;
				_compoundPageCount = 0;
				_isCompoundPage = false;
				_compoundPageRicList.clear();
			}
		} else {

			//if ((recordtype == 226) || (recordtype == 234) || (recordtype == 249) || (recordtype == 201) || (recordtype == 217) || (recordtype == 25) || (recordtype == 41) || (recordtype == 89) )
			if ( _isPage )
				m_viewController.updateTxtArea( ric, pageStr );
			else
				m_viewController.updateTxtArea( ric, quoteStr );
		}
    }
}
