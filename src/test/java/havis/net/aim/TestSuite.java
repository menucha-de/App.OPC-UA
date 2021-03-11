package havis.net.aim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ //
		havis.net.aim.device.rf.AimRfidReaderDeviceTest.class, //
		havis.net.aim.device.rf.InventoryThreadTest.class, //
		havis.net.aim.device.rf.DataTypeConverterTest.class, //
		havis.net.aim.device.rf.IODeviceConnectionManagerTest.class, //
		havis.net.aim.device.rf.MapperTest.class, //
		havis.net.aim.device.rf.RFDeviceConnectionManagerTest.class, //
		havis.net.aim.opcua.AimDataProviderTest.class, //
		havis.net.aim.opcua.ConfigurationManagerTest.class, //
		havis.net.aim.opcua.SerializerTest.class //
})

public class TestSuite {
}