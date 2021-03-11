package havis.net.aim.device.rf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import havis.device.rf.RFConsumer;
import havis.device.rf.RFDevice;
import mockit.Mocked;
import mockit.Verifications;

public class RFDeviceConnectionManagerTest {

	@Test
	public void test(@Mocked final RFDevice rfDevice) throws Exception {
		RFDeviceConnectionManager m = new RFDeviceConnectionManager(rfDevice);
		assertEquals(rfDevice, m.getRfDevice());

		// open the connection
		Integer connection = m.acquire();
		new Verifications() {
			{
				rfDevice.openConnection(withInstanceOf(RFConsumer.class), anyInt);
				times = 1;
			}
		};

		// acquire the opened connection
		connection = m.acquire();
		new Verifications() {
			{
				rfDevice.openConnection(withInstanceOf(RFConsumer.class), anyInt);
				times = 1;
			}
		};

		// release connection for both calls: the connection is NOT closed
		m.release(connection);
		m.release(connection);
		new Verifications() {
			{
				rfDevice.closeConnection();
				times = 0;
			}
		};

		// request closing: the connection is closed directly
		m.requestClosing(connection);
		new Verifications() {
			{
				rfDevice.closeConnection();
				times = 1;
			}
		};

		// open connection and request closing of the connection via event
		// "connectionAttempted": the connection is NOT closed
		connection = m.acquire();
		m.connectionAttempted();
		new Verifications() {
			{
				rfDevice.closeConnection();
				times = 1;
			}
		};

		// release connection: the connection is closed
		m.release(connection);
		new Verifications() {
			{
				rfDevice.closeConnection();
				times = 2;
			}
		};
	}
}
