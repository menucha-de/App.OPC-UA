package havis.net.aim.opcua;

public interface RfidDeviceEventListener extends RfidEventListener {
	void scanAsyncFinished(Object source);	
}
