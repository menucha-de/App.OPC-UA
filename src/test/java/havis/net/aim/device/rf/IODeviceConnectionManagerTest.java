package havis.net.aim.device.rf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import havis.device.io.IOConsumer;
import havis.device.io.IODevice;
import mockit.Mocked;
import mockit.Verifications;

public class IODeviceConnectionManagerTest {

	@Test
	public void test(@Mocked final IODevice ioDevice) throws Exception {
		IODeviceConnectionManager m = new IODeviceConnectionManager(ioDevice);
		assertEquals(ioDevice, m.getIODevice());

		// open the connection
		Integer connection = m.acquire();
		new Verifications() {
			{
				ioDevice.openConnection(withInstanceOf(IOConsumer.class), anyInt);
				times = 1;
			}
		};

		// acquire the opened connection
		connection = m.acquire();
		new Verifications() {
			{
				ioDevice.openConnection(withInstanceOf(IOConsumer.class), anyInt);
				times = 1;
			}
		};

		// release connection for both calls: the connection is NOT closed
		m.release(connection);
		m.release(connection);
		new Verifications() {
			{
				ioDevice.closeConnection();
				times = 0;
			}
		};

		// request closing: the connection is closed directly
		m.requestClosing(connection);
		new Verifications() {
			{
				ioDevice.closeConnection();
				times = 1;
			}
		};

		// open connection and request closing of the connection via event
		// "connectionAttempted": the connection is NOT closed
		connection = m.acquire();
		m.connectionAttempted();
		new Verifications() {
			{
				ioDevice.closeConnection();
				times = 1;
			}
		};

		// release connection: the connection is closed
		m.release(connection);
		new Verifications() {
			{
				ioDevice.closeConnection();
				times = 2;
			}
		};
	}
}
