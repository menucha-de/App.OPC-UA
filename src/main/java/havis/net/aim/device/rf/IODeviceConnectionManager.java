package havis.net.aim.device.rf;

import java.util.logging.Level;
import java.util.logging.Logger;

import havis.device.io.IOConsumer;
import havis.device.io.IODevice;
import havis.device.io.StateEvent;

public class IODeviceConnectionManager extends ConnectionManager<Integer> implements IOConsumer {

	private static final Logger log = Logger.getLogger(IODeviceConnectionManager.class.getName());
	private static final int CONNECT_TIMEOUT = 200;
	private final int connection = 0;
	private IODevice ioDevice;

	IODeviceConnectionManager(IODevice ioDevice) {
		this.ioDevice = ioDevice;
	}

	public IODevice getIODevice() {
		return ioDevice;
	}

	@Override
	protected Integer open() throws Exception {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Opening connection to IO device");
		}
		ioDevice.openConnection(this, CONNECT_TIMEOUT);
		return connection;
	}

	@Override
	protected void close(Integer connection) throws Exception {
		ioDevice.closeConnection();
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Closed connection to IO device");
		}
	}

	// IOConsumer interface

	@Override
	public void connectionAttempted() {
		try {
			requestClosing(connection);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot request the closing of the IO connection", e);
		}
	}

	@Override
	public void keepAlive() {
	}

	@Override
	public void stateChanged(StateEvent event) {

	}
}
