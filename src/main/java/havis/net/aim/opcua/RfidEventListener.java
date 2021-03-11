package havis.net.aim.opcua;

import java.util.Date;
import java.util.Map;
import havis.net.aim.opcua.Constants.EventType;

public interface RfidEventListener {
	void rfidEventOccured(Object source, EventType event, Date timeStamp, Map<String, Object> eventArgs);
}
