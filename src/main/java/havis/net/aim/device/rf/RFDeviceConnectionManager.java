package havis.net.aim.device.rf;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import havis.device.rf.RFConsumer;
import havis.device.rf.RFDevice;
import havis.device.rf.tag.TagData;
import havis.device.rf.tag.operation.TagOperation;

public class RFDeviceConnectionManager extends ConnectionManager<Integer> implements RFConsumer {

	private static final Logger log = Logger.getLogger(RFDeviceConnectionManager.class.getName());
	private static final int CONNECT_TIMEOUT = 200;
	private final int connection = 0;
	private RFDevice rfDevice;

	RFDeviceConnectionManager(RFDevice rfDevice) {
		this.rfDevice = rfDevice;
	}

	public RFDevice getRfDevice() {
		return rfDevice;
	}

	@Override
	protected Integer open() throws Exception {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Opening connection to RF device");
		}
		rfDevice.openConnection(this, CONNECT_TIMEOUT);
		return connection;
	}

	@Override
	protected void close(Integer connection) throws Exception {
		rfDevice.closeConnection();
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Closed connection to RF device");
		}
	}

	// IOConsumer interface

	@Override
	public void connectionAttempted() {
		try {
			requestClosing(connection);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot request the closing of the RF connection", e);
		}
	}

	@Override
	public void keepAlive() {
	}

	@Override
	public List<TagOperation> getOperations(TagData arg0) {
		return null;
	}

}
