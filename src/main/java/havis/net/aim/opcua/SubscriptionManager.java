package havis.net.aim.opcua;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import havis.device.rf.common.util.RFUtils;
import havis.net.aim.opcua.Constants.EventType;
import havis.net.aim.opcua.Constants.Param;
import havis.net.aim.xsd.AntennaNameIdPair;
import havis.net.aim.xsd.DeviceStatusEnumeration;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.ScanData;
import havis.opcua.message.MessageHandler;

public class SubscriptionManager implements ParamChangedListener, RfidEventListener {
	private static final Logger log = Logger.getLogger(SubscriptionManager.class.getName());	
	private List<Param> paramSubscriptions;
	private List<EventType> eventSubscriptions;
	private MessageHandler mHdl;
	
	public SubscriptionManager(MessageHandler messageHandler) {
		this.mHdl = messageHandler;
		this.paramSubscriptions = new ArrayList<>();
		this.eventSubscriptions = new ArrayList<>();
	}

	public void addSubscription(Param param, Object currentValue) {
		if (!this.paramSubscriptions.contains(param)) {			
			this.paramSubscriptions.add(param);
			log.log(Level.FINE, "Subscribed parameter {0}.", param);
		} else log.log(Level.FINE, "Parameter {0} already subscribed.", param);
		
		log.log(Level.FINE, "Sending notification for parameter {0} with value {1}", new Object[] { param.name, currentValue });
		Map<String, Object> notifyMap = new HashMap<>();		
		notifyMap.put(param.name, currentValue);
		mHdl.notify(notifyMap);								
	}
	
	public void removeSubscription(Param param) {
		if (this.paramSubscriptions.remove(param))
			log.log(Level.FINE, "Unsubscribed parameter {0}.", param);
	}
	
	public void addSubscription(EventType event) {
		if (!this.eventSubscriptions.contains(event)) {
			this.eventSubscriptions.add(event);		
			log.log(Level.FINE, "Subscribed event {0}.", event);	
			
		} else log.log(Level.FINE, "Event {0} already subscribed.", event);
	}
	
	public void removeSubscription(EventType event) {
		if (this.eventSubscriptions.remove(event))
			log.log(Level.FINE, "Unsubscribed event {0}.", event);
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public void paramChanged(Object source, Param param, Object newValue) {		
		if (!paramSubscriptions.contains(param)) return;
		
		log.log(Level.FINE, "Sending notification for change of parameter {0} to new value {1}", new Object[] { param.name, newValue });
		
		Map<String, Object> notifyMap = new HashMap<>();		
		switch (param) {
			case ANTENNA_NAMES:
				notifyMap.put(param.name, Serializer.serialize((List<AntennaNameIdPair>)newValue));
				mHdl.notify(notifyMap);
				break;
				
			case DEVICE_NAME:
			case DEVICE_INFO:
			case AUTOID_MODEL_VERSION:
			case MANUFACTURER:
			case MODEL: 
			case SOFTWARE_REVISION:
			case DEVICE_MANUAL:  
			case DEVICE_REVISION: 
			case HARDWARE_REVISION:
			case SERIAL_NUMBER: 				
				notifyMap.put(param.name, (String)newValue);
				mHdl.notify(notifyMap);
				break;
				
			case DEVICE_STATUS:
				notifyMap.put(param.name, ((DeviceStatusEnumeration)newValue).ordinal());				
				mHdl.notify(notifyMap);
				break;
				
			case LAST_SCAN_DATA:				
				notifyMap.put(param.name, Serializer.serialize((ScanData)newValue));
				mHdl.notify(notifyMap);
				break;
				
			case REVISION_COUNTER:
				notifyMap.put(param.name, new Integer( (int)newValue ));
				mHdl.notify(notifyMap);
				break;
				
			default:
				break;							
		}
	}
	
	@Override
	public void rfidEventOccured(Object source, EventType event, Date timeStamp, Map<String, Object> eventArgs) {
		if (!eventSubscriptions.contains(event)) return;
		
		switch (event) {
			case RFID_SCAN_EVENT:
				
			String deviceName = (String) eventArgs.get(Environment.EVENT_RFID_SCAN_EVENT_PARAM_DEVICE_NAME);
			RfidScanResult scanRes = (RfidScanResult) eventArgs
					.get(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT);

			String eventId = Environment.EVENT_RFID_SCAN_EVENT_TYPE;
			String paramId = Environment.INSTANCE_NAME;
			int severity = Environment.EVENT_RFID_SCAN_EVENT_SEVERITY;
			String message = deviceName + ":" + RFUtils.bytesToHex(scanRes.getScanData().getEpc().getUid()).toLowerCase();

			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_DEVICE_NAME, deviceName);
			paramMap.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT, Serializer.serialize(scanRes));

			log.log(Level.FINE,
				"Sending event: '{' eventType={0}, paramId={1}, timeStamp={2}, severity={3}, message={4}, args={5} '}'",
				new Object[] { eventId, paramId, timeStamp.getTime(), severity, message, paramMap });
			mHdl.event(eventId, paramId, timeStamp, severity, message, paramMap);
				
			break;
		}		
	}
	
	public void removeAllSubscriptions() {
		this.paramSubscriptions.clear();
		this.eventSubscriptions.clear();
		log.log(Level.FINE, "Unsubscribed all parameters and events.");
	}

}
