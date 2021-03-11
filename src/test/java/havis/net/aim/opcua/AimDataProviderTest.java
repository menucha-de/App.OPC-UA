package havis.net.aim.opcua;

import static mockit.Deencapsulation.getField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import havis.net.aim.device.rf.RfidReaderDevice;
import havis.net.aim.opcua.Constants.Param;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.CodeTypesType;
import havis.net.aim.xsd.RfidLockOperationEnumeration;
import havis.net.aim.xsd.RfidLockRegionEnumeration;
import havis.net.aim.xsd.RfidPasswordTypeEnumeration;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanSettings;
import havis.net.aim.xsd.TagTypeEnumeration;
import havis.net.aim.xsd.TagTypesType;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.exception.InvalidParameterException;
import havis.opcua.message.exception.NoSuchParameterException;
import havis.opcua.message.exception.ParameterException;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

public class AimDataProviderTest {

	@Mocked
	RfidReaderDevice rfidReaderDevice;
	@Mocked
	MessageHandler messageHandler;
	@Mocked
	SubscriptionManager subscriptionManager;

	@Test
	public void testRfidDataProvider() {
		final AimDataProvider rdp = new AimDataProvider(rfidReaderDevice, messageHandler);

		final MessageHandler mHdl = rdp.getMessageHandler();
		final RfidReaderDevice rfidDev = rdp.getRfidDevice();
		final SubscriptionManager subMgr = rdp.getSubscriptionManager();

		new Verifications() {
			{
				assertEquals(rfidReaderDevice, getField(rdp, "rfidReaderDevice"));
				assertEquals(messageHandler, getField(rdp, "messageHandler"));
				SubscriptionManager newSubMgr = new SubscriptionManager(messageHandler);
				rfidReaderDevice.addParamChangedListener(newSubMgr);
				rfidReaderDevice.addRfidEventListener(newSubMgr);

				assertEquals(getField(rdp, "messageHandler"), mHdl);
				assertEquals(getField(rdp, "rfidReaderDevice"), rfidDev);
				assertEquals(getField(rdp, "subscriptionManager"), subMgr);
			}
		};
	}

	@Test
	public void testRead() throws ParameterException {
		final AimDataProvider rdp = new AimDataProvider(rfidReaderDevice, messageHandler);
		try {
			rdp.read("foo");
			fail("Exception expected.");
		} catch (ParameterException e) {
		}

		rdp.read(Environment.VARIABLE_NAME_ANTENNA_NAMES);
		new Verifications() {
			{
				rfidReaderDevice.getAntennaNames();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_AUTOID_MODEL_VERSION);
		new Verifications() {
			{
				rfidReaderDevice.getAutoIdModelVersion();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_DEVICE_INFO);
		new Verifications() {
			{
				rfidReaderDevice.getDeviceInfo();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_DEVICE_MANUAL);
		new Verifications() {
			{
				rfidReaderDevice.getDeviceManual();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_DEVICE_NAME);
		new Verifications() {
			{
				rfidReaderDevice.getDeviceName();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_DEVICE_REVISION);
		new Verifications() {
			{
				rfidReaderDevice.getDeviceRevision();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_DEVICE_STATUS);
		new Verifications() {
			{
				rfidReaderDevice.getDeviceStatus();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_HARDWARE_REVISION);
		new Verifications() {
			{
				rfidReaderDevice.getHardwareRevision();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_LAST_SCAN_DATA);
		new Verifications() {
			{
				rfidReaderDevice.getLastScanData();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_MANUFACTURER);
		new Verifications() {
			{
				rfidReaderDevice.getManufacturer();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_MODEL);
		new Verifications() {
			{
				rfidReaderDevice.getModel();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_REVISION_COUNTER);
		new Verifications() {
			{
				rfidReaderDevice.getRevisionCounter();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_SERIAL_NUMBER);
		new Verifications() {
			{
				rfidReaderDevice.getSerialNumber();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_SOFTWARE_REVISION);
		new Verifications() {
			{
				rfidReaderDevice.getSoftwareRevision();
			}
		};

		rdp.read(Environment.VARIABLE_NAME_CODE_TYPES);
		new Verifications() {
			{
				rfidReaderDevice.getCodeTypes();
				times = 1;
			}
		};

		Object r = rdp.read(Environment.VARIABLE_NAME_CODE_TYPES_ENUM_STRINGS);
		Assert.assertArrayEquals(new String[] { "EPC" }, (String[]) r);

		rdp.read(Environment.VARIABLE_NAME_TAG_TYPES);
		new Verifications() {
			{
				rfidReaderDevice.getTagTypes();
				times = 1;
			}
		};

		r = rdp.read(Environment.VARIABLE_NAME_TAG_TYPES_ENUM_STRINGS);
		Assert.assertArrayEquals(new String[] { "EPC Class1 Gen2 V1" }, (String[]) r);

		rdp.read(Environment.VARIABLE_NAME_RF_POWER);
		new Verifications() {
			{
				rfidReaderDevice.getRfPower();
				times = 1;
			}
		};

		rdp.read(Environment.VARIABLE_NAME_MIN_RSSI);
		new Verifications() {
			{
				rfidReaderDevice.getMinRssi();
				times = 1;
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWrite() throws ParameterException {
		final AimDataProvider rdp = new AimDataProvider(rfidReaderDevice, messageHandler);

		/* non-existent param 'foo' */
		try {
			rdp.write("foo", "bar");
			fail("Exception expected.");
		} catch (NoSuchParameterException e) {
		}

		/* AntennaNames */
		Map<String, Object> ant1 = new HashMap<>();
		ant1.put("AntennaId", 1);
		ant1.put("AntennaName", "Antenna 1");

		Map<String, Object> ant2 = new HashMap<>();
		ant2.put("AntennaId", 2);
		ant2.put("AntennaName", "Antenna 2");

		rdp.write(Environment.VARIABLE_NAME_ANTENNA_NAMES, Arrays.asList(ant1, ant2).toArray());
		new Verifications() {
			{
				rfidReaderDevice.setAntennaNames(withInstanceOf(List.class));
			}
		};

		/* AutoIdModelVersion */
		rdp.write(Environment.VARIABLE_NAME_AUTOID_MODEL_VERSION, "AutoIdModelVersion:TEST");
		new Verifications() {
			{
				rfidReaderDevice.setAutoIdModelVersion("AutoIdModelVersion:TEST");
			}
		};

		/* DeviceInfo */
		rdp.write(Environment.VARIABLE_NAME_DEVICE_INFO, "DeviceInfo:TEST");
		new Verifications() {
			{
				rfidReaderDevice.setDeviceInfo("DeviceInfo:TEST");
			}
		};

		/* DeviceInfo */
		rdp.write(Environment.VARIABLE_NAME_DEVICE_NAME, "DeviceName:TEST");
		new Verifications() {
			{
				rfidReaderDevice.setDeviceName("DeviceName:TEST");
			}
		};

		/* Manufacturer (write-only) */
		try {
			rdp.write(Environment.VARIABLE_NAME_MANUFACTURER, "ACME");
			fail("InvalidParameterException expected");
		} catch (InvalidParameterException ex) {
		}

		/* CodeTypes */
		rdp.write(Environment.VARIABLE_NAME_CODE_TYPES, new Long[] { 0L });
		new Verifications() {
			{
				CodeTypesType codeTypes;
				rfidReaderDevice.setCodeTypes(codeTypes = withCapture());
				times = 1;
				assertEquals(1, codeTypes.getCodeType().size());
				assertEquals(CodeTypeEnumeration.EPC, codeTypes.getCodeType().get(0));
			}
		};

		try {
			// use an invalid enum index
			rdp.write(Environment.VARIABLE_NAME_CODE_TYPES, new Long[] { 1L });
			fail("InvalidParameterException expected");
		} catch (InvalidParameterException e) {
			Assert.assertTrue(e.getCause() instanceof DeserializerException);
		}

		/* Code Type Enum Strings (write-only) */
		try {
			rdp.write(Environment.VARIABLE_NAME_CODE_TYPES_ENUM_STRINGS, "a");
			fail("InvalidParameterException expected");
		} catch (InvalidParameterException ex) {
			Assert.assertTrue(ex.getMessage().contains("not writable"));
		}

		/* TagTypes */
		rdp.write(Environment.VARIABLE_NAME_TAG_TYPES, new Long[] { 0L });
		new Verifications() {
			{
				TagTypesType tagTypes;
				rfidReaderDevice.setTagTypes(tagTypes = withCapture());
				times = 1;
				assertEquals(1, tagTypes.getTagType().size());
				assertEquals(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1, tagTypes.getTagType().get(0));
			}
		};

		try {
			// use an invalid enum index
			rdp.write(Environment.VARIABLE_NAME_TAG_TYPES, new Long[] { 1L });
			fail("InvalidParameterException expected");
		} catch (InvalidParameterException e) {
			Assert.assertTrue(e.getCause() instanceof DeserializerException);
		}

		/* Tag Type Enum Strings (write-only) */
		try {
			rdp.write(Environment.VARIABLE_NAME_TAG_TYPES_ENUM_STRINGS, "a");
			fail("InvalidParameterException expected");
		} catch (InvalidParameterException ex) {
			Assert.assertTrue(ex.getMessage().contains("not writable"));
		}

		/* RF Power */
		rdp.write(Environment.VARIABLE_NAME_RF_POWER, (byte) 3);
		new Verifications() {
			{
				rfidReaderDevice.setRfPower((byte) 3);
			}
		};

		/* Min RSSI */
		rdp.write(Environment.VARIABLE_NAME_MIN_RSSI, 4);
		new Verifications() {
			{
				rfidReaderDevice.setMinRssi(4);
			}
		};
	}

	@Test
	public void testSubscribe() throws ParameterException {
		final AimDataProvider rdp = new AimDataProvider(rfidReaderDevice, messageHandler);
		new NonStrictExpectations(rdp) {
			{
				rdp.read(anyString);
				result = "bar";
			}
		};

		try {
			rdp.subscribe("foo");
			fail("NoSuchParameterException expected.");
		} catch (NoSuchParameterException e) {
		}

		rdp.subscribe(Environment.VARIABLE_NAME_DEVICE_STATUS);
		new Verifications() {
			{

				Param p = null;
				Object v = null;

				subscriptionManager.addSubscription(p = withCapture(), v = withCapture());
				assertEquals("bar", v);
				assertEquals(Environment.VARIABLE_NAME_DEVICE_STATUS, p.name);

			}
		};
	}

	@Test
	public void testUnsubscibe() throws ParameterException {
		final AimDataProvider rdp = new AimDataProvider(rfidReaderDevice, messageHandler);
		try {
			rdp.unsubscribe("foo");
			fail("NoSuchParameterException expected.");
		} catch (NoSuchParameterException e) {
		}

		rdp.unsubscribe(Environment.VARIABLE_NAME_DEVICE_STATUS);
		new Verifications() {
			{
				Param p = null;
				subscriptionManager.removeSubscription(p = withCapture());
				assertEquals(Environment.VARIABLE_NAME_DEVICE_STATUS, p.name);
			}
		};
	}

	@Test
	public void testCall() throws ParameterException {
		final AimDataProvider rdp = new AimDataProvider(rfidReaderDevice, messageHandler);

		try {
			rdp.call("foo", null, new Object[] {});
			fail("Exception expected");
		} catch (NoSuchParameterException e) {

		}

		rdp.call(Environment.METHOD_NAME_SCAN, null, new Object[] { new HashMap<>() });
		new Verifications() {
			{
				rfidReaderDevice.scan(withInstanceOf(ScanSettings.class));
			}
		};

		rdp.call(Environment.METHOD_NAME_SCAN_START, null, new Object[] { new HashMap<>() });
		new Verifications() {
			{
				rfidReaderDevice.scan(withInstanceOf(ScanSettings.class));
			}
		};

		rdp.call(Environment.METHOD_NAME_SCAN_STOP, null, new Object[] {});
		new Verifications() {
			{
				rfidReaderDevice.scanStart(withInstanceOf(ScanSettings.class));
			}
		};

		rdp.call(Environment.METHOD_NAME_READ, null,
				new Object[] { new HashMap<>(), CodeTypeEnumeration.EPC.name(), 0, 1L, 32L, new Byte[] {} });
		new Verifications() {
			{
				rfidReaderDevice.readTag(withInstanceOf(ScanData.class), CodeTypeEnumeration.EPC, 0, 1L, 32L,
						withInstanceOf(byte[].class));
			}
		};

		rdp.call(Environment.METHOD_NAME_WRITE, null,
				new Object[] { new HashMap<>(), CodeTypeEnumeration.EPC.name(), 0, 1L, new Byte[] {}, new Byte[] {} });
		new Verifications() {
			{
				rfidReaderDevice.writeTag(withInstanceOf(ScanData.class), CodeTypeEnumeration.EPC, 0, 1L,
						withInstanceOf(byte[].class), withInstanceOf(byte[].class));
			}
		};

		rdp.call(Environment.METHOD_NAME_LOCK, null,
				new Object[] { new HashMap<>(), CodeTypeEnumeration.EPC.name(), new Byte[] {}, 0, 1, 0L, 0L });
		new Verifications() {
			{
				rfidReaderDevice.lockTag(withInstanceOf(ScanData.class), CodeTypeEnumeration.EPC,
						withInstanceOf(byte[].class), withInstanceOf(RfidLockRegionEnumeration.class),
						withInstanceOf(RfidLockOperationEnumeration.class), 0L, 0L);
			}
		};

		rdp.call(Environment.METHOD_NAME_KILL, null,
				new Object[] { new HashMap<>(), CodeTypeEnumeration.EPC.name(), new Byte[] {} });
		new Verifications() {
			{
				rfidReaderDevice.killTag(withInstanceOf(ScanData.class), CodeTypeEnumeration.EPC,
						withInstanceOf(byte[].class));
			}
		};

		rdp.call(Environment.METHOD_NAME_SET_PASSWORD, null,
				new Object[] { new HashMap<>(), CodeTypeEnumeration.EPC.name(), 0, new Byte[] {}, new Byte[] {} });
		new Verifications() {
			{
				rfidReaderDevice.setTagPassword(withInstanceOf(ScanData.class), CodeTypeEnumeration.EPC,
						withInstanceOf(RfidPasswordTypeEnumeration.class), withInstanceOf(byte[].class),
						withInstanceOf(byte[].class));
			}
		};

	}
}
