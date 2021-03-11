package havis.net.aim.device.rf;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import havis.device.io.Direction;
import havis.device.io.IOConfiguration;
import havis.device.io.IODevice;
import havis.device.io.State;
import havis.device.io.Type;
import havis.device.rf.RFConsumer;
import havis.device.rf.RFDevice;
import havis.device.rf.capabilities.Capabilities;
import havis.device.rf.capabilities.CapabilityType;
import havis.device.rf.capabilities.RegulatoryCapabilities;
import havis.device.rf.capabilities.TransmitPowerTable;
import havis.device.rf.capabilities.TransmitPowerTableEntry;
import havis.device.rf.common.util.RFUtils;
import havis.device.rf.configuration.AntennaConfiguration;
import havis.device.rf.configuration.Configuration;
import havis.device.rf.configuration.ConfigurationType;
import havis.device.rf.configuration.InventorySettings;
import havis.device.rf.configuration.RssiFilter;
import havis.device.rf.exception.CommunicationException;
import havis.device.rf.exception.ConnectionException;
import havis.device.rf.exception.ImplementationException;
import havis.device.rf.exception.ParameterException;
import havis.device.rf.tag.Filter;
import havis.device.rf.tag.TagData;
import havis.device.rf.tag.operation.KillOperation;
import havis.device.rf.tag.operation.LockOperation;
import havis.device.rf.tag.operation.LockOperation.Field;
import havis.device.rf.tag.operation.LockOperation.Privilege;
import havis.device.rf.tag.operation.ReadOperation;
import havis.device.rf.tag.operation.TagOperation;
import havis.device.rf.tag.operation.WriteOperation;
import havis.device.rf.tag.result.KillResult;
import havis.device.rf.tag.result.LockResult;
import havis.device.rf.tag.result.ReadResult;
import havis.device.rf.tag.result.WriteResult;
import havis.net.aim.opcua.ConfigurationManager;
import havis.net.aim.opcua.Constants.EventType;
import havis.net.aim.opcua.Constants.Param;
import havis.net.aim.opcua.Environment;
import havis.net.aim.opcua.ParamChangedListener;
import havis.net.aim.opcua.RfidEventListener;
import havis.net.aim.xsd.AntennaNameIdPair;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.DeviceStatusEnumeration;
import havis.net.aim.xsd.ReadResultPair;
import havis.net.aim.xsd.RfidLockOperationEnumeration;
import havis.net.aim.xsd.RfidLockRegionEnumeration;
import havis.net.aim.xsd.RfidPasswordTypeEnumeration;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.RfidScanResultPair;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanDataEpc;
import havis.net.aim.xsd.ScanSettings;
import havis.net.aim.xsd.TagTypeEnumeration;
import havis.opcua.message.exception.ApplicationException;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

public class AimRfidReaderDeviceTest {

	@Mocked
	ConfigurationManager configurationManager;
	@Mocked
	RFDevice rfDevice;
	@Mocked
	InventoryThread inventoryThread;
	@Mocked
	ParamChangedListener paramChangedListener;
	@Mocked
	RfidEventListener rfidEventListener;

	interface IOGetCommand<T> {
		T get() throws ApplicationException;
	}

	interface IOSetCommand<T> {
		Param getParam();

		void set(T value) throws ApplicationException;
	}

	@Test
	public void testRfr300RfDevice() throws Exception {

		AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		assertEquals(configurationManager, getField(rfr300, "configurationManager"));

		List<AntennaNameIdPair> ant0 = getField(rfr300, "antennaNames");
		assertEquals(1, ant0.size());
		assertEquals(Environment.VARIABLE_VAL_ANTENNA_ID, ant0.get(0).getAntennaId());
		assertEquals(Environment.VARIABLE_VAL_ANTENNA_NAME, ant0.get(0).getAntennaName());

		assertEquals(Environment.VARIABLE_VAL_AUTOID_MODEL_VERSION, getField(rfr300, "autoIdModelVersion"));
		assertEquals(Environment.VARIABLE_VAL_DEVICE_INFO, getField(rfr300, "deviceInfo"));
		assertEquals(Environment.VARIABLE_VAL_DEVICE_MANUAL, getField(rfr300, "deviceManual"));
		assertEquals(Environment.VARIABLE_VAL_DEVICE_NAME, getField(rfr300, "deviceName"));
		assertEquals(Environment.VARIABLE_VAL_DEVICE_REVISION, getField(rfr300, "deviceRevision"));
		assertEquals(Environment.VARIABLE_VAL_DEVICE_STATUS, getField(rfr300, "deviceStatus"));
		assertEquals(Environment.VARIABLE_VAL_HARDWARE_REVISION, getField(rfr300, "hardwareRevision"));
		assertEquals(Environment.VARIABLE_VAL_SOFTWARE_REVISION, getField(rfr300, "softwareRevision"));
		assertEquals(Environment.VARIABLE_VAL_MANUFACTURER, getField(rfr300, "manufacturer"));
		assertEquals(Environment.VARIABLE_VAL_MODEL, getField(rfr300, "model"));
		assertEquals(Environment.VARIABLE_VAL_REVISION_COUNTER, getField(rfr300, "revisionCounter"));
		assertEquals(Environment.VARIABLE_VAL_SERIAL_NUMBER, getField(rfr300, "serialNumber"));

		assertEquals(Environment.VARIABLE_VAL_CODE_TYPES.length, rfr300.getCodeTypes().getCodeType().size());
		int index = Environment.VARIABLE_VAL_CODE_TYPES[0].intValue();
		assertEquals(CodeTypeEnumeration.EPC.toString(), rfr300.getCodeTypes().getCodeType().get(index).toString());

		assertEquals(1, Environment.VARIABLE_VAL_CODE_TYPES_ENUM_STRINGS.length);
		assertEquals(CodeTypeEnumeration.EPC.toString(),
				Environment.VARIABLE_VAL_CODE_TYPES_ENUM_STRINGS[0].toString());

		assertEquals(Environment.VARIABLE_VAL_TAG_TYPES.length, rfr300.getTagTypes().getTagType().size());
		index = Environment.VARIABLE_VAL_TAG_TYPES[0].intValue();
		assertEquals(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.toString(),
				rfr300.getTagTypes().getTagType().get(index).toString());

		assertEquals(1, Environment.VARIABLE_VAL_TAG_TYPES_ENUM_STRINGS.length);
		assertEquals(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.toString(),
				Environment.VARIABLE_VAL_TAG_TYPES_ENUM_STRINGS[0].toString());
	}

	@Test
	public void testGetRfPower(@Mocked final Logger log,
			@Mocked final RFDeviceConnectionManager rfDeviceConnectionManager) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		// set antennas
		List<AntennaNameIdPair> antennas = new ArrayList<>();
		AntennaNameIdPair antenna = new AntennaNameIdPair();
		antenna.setAntennaId(1);
		antennas.add(antenna);

		AntennaNameIdPair antenna2 = new AntennaNameIdPair();
		antenna2.setAntennaId(2);
		antennas.add(antenna2);

		rfr300.setAntennaNames(antennas);

		// set transmit power table
		final List<Capabilities> caps = new ArrayList<>();
		RegulatoryCapabilities cap = new RegulatoryCapabilities();
		TransmitPowerTable tpt = new TransmitPowerTable();

		TransmitPowerTableEntry tpte = new TransmitPowerTableEntry();
		tpte.setIndex((short) 2);
		tpte.setTransmitPower((short) 3);
		tpt.getEntryList().add(tpte);

		TransmitPowerTableEntry tpte2 = new TransmitPowerTableEntry();
		tpte2.setIndex((short) 4);
		tpte2.setTransmitPower((short) 5);
		tpt.getEntryList().add(tpte2);

		cap.setTransmitPowerTable(tpt);
		caps.add(cap);

		// set antenna configurations
		final List<Configuration> confs = new ArrayList<>();
		final AntennaConfiguration conf = new AntennaConfiguration();
		conf.setId((short) antenna.getAntennaId());
		conf.setTransmitPower(tpte.getIndex());
		confs.add(conf);

		final AntennaConfiguration conf2 = new AntennaConfiguration();
		conf2.setId((short) antenna2.getAntennaId());
		conf2.setTransmitPower(tpte.getIndex());
		confs.add(conf2);

		// an antenna configuration which must be ignored
		confs.add(new AntennaConfiguration());

		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				rfDeviceConnectionManager.getRfDevice();
				result = rfDevice;

				rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
				result = caps;

				rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, conf.getId() /* antennaId */,
						(short) 0, (short) 0);
				result = confs;

				rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, conf2.getId() /* antennaId */,
						(short) 0, (short) 0);
				result = confs;
			}
		};

		// valid transmit power
		assertEquals(tpte.getTransmitPower(), rfr300.getRfPower());
		assertEquals(DeviceStatusEnumeration.IDLE, rfr300.getDeviceStatus());

		// unknown transmit power index (throws directly an
		// ApplicationException)
		tpte.setIndex((short) (tpte.getIndex() - 1));
		try {
			assertEquals(tpte.getTransmitPower(), rfr300.getRfPower());
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
		tpte.setIndex((short) (tpte.getIndex() + 1));

		// two antennas with different transmit powers
		conf.setTransmitPower(tpte2.getIndex());
		try {
			assertEquals(tpte2.getTransmitPower(), rfr300.getRfPower());
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		conf.setTransmitPower(tpte.getIndex());

		// unknown antenna
		antenna.setAntennaId(3);
		antenna2.setAntennaId(4);
		try {
			assertEquals(tpte.getTransmitPower(), rfr300.getRfPower());
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		antenna.setAntennaId(1);
		antenna2.setAntennaId(2);

		// without antennas
		rfr300.setAntennaNames(new ArrayList<AntennaNameIdPair>());
		try {
			assertEquals(tpte.getTransmitPower(), rfr300.getRfPower());
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}

		// without regulatory capabilities
		caps.clear();
		try {
			assertEquals(tpte.getTransmitPower(), rfr300.getRfPower());
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// closing connection fails
		new Expectations() {
			{
				rfDeviceConnectionManager.release(anyInt);
				result = new Exception("huhu");
			}
		};
		confs.add(conf);
		try {
			rfr300.getRfPower();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// opening connection fails
		new Expectations() {
			{
				rfDeviceConnectionManager.acquire();
				result = new Exception("huhu");
			}
		};
		try {
			rfr300.getRfPower();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testSetRfPower(@Mocked final Logger log) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		final List<Param> changedParams = new ArrayList<>();
		final List<Object> changedValues = new ArrayList<>();
		rfr300.addParamChangedListener(new ParamChangedListener() {

			@Override
			public void paramChanged(Object source, Param param, Object newValue) {
				changedParams.add(param);
				changedValues.add(newValue);
			}
		});

		// set antennas
		List<AntennaNameIdPair> antennas = new ArrayList<>();
		AntennaNameIdPair antenna = new AntennaNameIdPair();
		antenna.setAntennaId(1);
		antennas.add(antenna);

		AntennaNameIdPair antenna2 = new AntennaNameIdPair();
		antenna2.setAntennaId(2);
		antennas.add(antenna2);

		rfr300.setAntennaNames(antennas);

		// set transmit power table
		final List<Capabilities> caps = new ArrayList<>();
		RegulatoryCapabilities cap = new RegulatoryCapabilities();
		TransmitPowerTable tpt = new TransmitPowerTable();

		final TransmitPowerTableEntry tpte = new TransmitPowerTableEntry();
		tpte.setIndex((short) 2);
		tpte.setTransmitPower((short) 3);
		tpt.getEntryList().add(tpte);

		cap.setTransmitPowerTable(tpt);
		caps.add(cap);

		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
				result = caps;
			}
		};

		changedParams.clear();
		changedValues.clear();
		rfr300.setRfPower((byte) tpte.getTransmitPower());
		new Verifications() {
			{
				// configuration is set for all antennas
				List<List<Configuration>> l = new ArrayList<>();
				rfDevice.setConfiguration(withCapture(l));
				times = 2;

				List<Configuration> l1 = l.get(0);
				assertEquals(1, l1.size());
				AntennaConfiguration antennaConf = (AntennaConfiguration) l1.get(0);
				assertEquals(tpte.getIndex(), antennaConf.getTransmitPower().shortValue());

				l1 = l.get(1);
				assertEquals(1, l1.size());
				antennaConf = (AntennaConfiguration) l1.get(0);
				assertEquals(tpte.getIndex(), antennaConf.getTransmitPower().shortValue());
			}
		};
		assertEquals(3, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(Param.RF_POWER.toString(), changedParams.get(1).toString());
		assertEquals((byte) tpte.getTransmitPower(), (byte) changedValues.get(1));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(2).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(2));

		// unknown transmit power index (throws directly an
		// ApplicationException)
		changedParams.clear();
		changedValues.clear();
		try {
			rfr300.setRfPower((byte) 10);
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(2, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(1).toString());
		assertEquals(DeviceStatusEnumeration.ERROR, (DeviceStatusEnumeration) changedValues.get(1));

		// without antennas
		rfr300.setAntennaNames(new ArrayList<AntennaNameIdPair>());
		changedParams.clear();
		changedValues.clear();
		rfr300.setRfPower((byte) tpte.getTransmitPower());
		assertEquals(2, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(1).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(1));

		// unexpected exception
		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = new Exception("huhu");
			}
		};
		try {
			rfr300.setRfPower((byte) tpte.getTransmitPower());
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testGetMinRssi(@Mocked final Logger log,
			@Mocked final RFDeviceConnectionManager rfDeviceConnectionManager) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		final List<Configuration> confs = new ArrayList<>();
		InventorySettings conf = new InventorySettings();
		RssiFilter rssiFilter = new RssiFilter();
		rssiFilter.setMinRssi((short) 3);
		rssiFilter.setMaxRssi((short) 4);
		conf.setRssiFilter(rssiFilter);
		confs.add(conf);

		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				rfDeviceConnectionManager.getRfDevice();
				result = rfDevice;

				rfDevice.getConfiguration(ConfigurationType.INVENTORY_SETTINGS, anyShort /* antennaId */, (short) 0,
						(short) 0);
				result = confs;
			}
		};

		// valid transmit power
		assertEquals(rssiFilter.getMinRssi(), rfr300.getMinRssi());
		assertEquals(DeviceStatusEnumeration.IDLE, rfr300.getDeviceStatus());

		// missing configuration (throws directly an ApplicationException)
		confs.clear();
		try {
			rfr300.getMinRssi();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// closing connection fails
		new Expectations() {
			{
				rfDeviceConnectionManager.release(anyInt);
				result = new Exception("huhu");
			}
		};
		confs.add(conf);
		try {
			rfr300.getMinRssi();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// opening connection fails
		new Expectations() {
			{
				rfDeviceConnectionManager.acquire();
				result = new Exception("huhu");
			}
		};
		try {
			rfr300.getMinRssi();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testSetMinRssi(@Mocked final Logger log) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		final List<Param> changedParams = new ArrayList<>();
		final List<Object> changedValues = new ArrayList<>();
		rfr300.addParamChangedListener(new ParamChangedListener() {

			@Override
			public void paramChanged(Object source, Param param, Object newValue) {
				changedParams.add(param);
				changedValues.add(newValue);
			}
		});

		final List<Configuration> confs = new ArrayList<>();
		InventorySettings conf = new InventorySettings();
		RssiFilter rssiFilter = new RssiFilter();
		rssiFilter.setMinRssi((short) 3);
		rssiFilter.setMaxRssi((short) 4);
		conf.setRssiFilter(rssiFilter);
		confs.add(conf);

		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				rfDevice.getConfiguration(ConfigurationType.INVENTORY_SETTINGS, anyShort /* antennaId */, (short) 0,
						(short) 0);
				result = confs;
			}
		};

		changedParams.clear();
		changedValues.clear();
		rfr300.setMinRssi(2);
		assertEquals(3, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(Param.MIN_RSSI.toString(), changedParams.get(1).toString());
		assertEquals(2, (int) changedValues.get(1));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(2).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(2));

		new Verifications() {
			{
				List<Configuration> l = new ArrayList<>();
				rfDevice.setConfiguration(l = withCapture());
				times = 1;
				assertEquals(1, l.size());
				assertEquals(2, ((InventorySettings) l.get(0)).getRssiFilter().getMinRssi());
			}
		};

		// missing configuration (throws directly an ApplicationException)
		confs.clear();
		try {
			rfr300.setMinRssi(2);
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// unexpected exception
		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = new Exception("huhu");
			}
		};
		confs.add(conf);
		try {
			rfr300.setMinRssi(2);
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testGetIOState(@Mocked final Logger log, @Mocked final IODevice ioDevice,
			@Mocked final IODeviceConnectionManager ioDeviceConnectionManager) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, ioDevice, configurationManager);

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getHs1();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getHs2();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getHs3();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getHs4();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getLs1();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getLs2();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getSws1Swd1();
			}
		});

		checkGetIOState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIOState>() {

			@Override
			public HaIOState get() throws ApplicationException {
				return rfr300.getSws2Swd2();
			}
		});
	}

	private void checkGetIOState(final Logger log, final IODevice ioDevice,
			final IODeviceConnectionManager ioDeviceConnectionManager, AimRfidReaderDevice rfr300,
			IOGetCommand<HaIOState> rfr300Command) throws Exception {
		final List<havis.device.io.Configuration> confs = new ArrayList<>();
		final IOConfiguration conf = new IOConfiguration();
		conf.setId((short) 1);
		conf.setState(State.HIGH);
		confs.add(conf);
		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				ioDeviceConnectionManager.getIODevice();
				result = ioDevice;

				ioDevice.getConfiguration(Type.IO, anyShort /* port */);
				result = confs;

				// reset expectations
				ioDeviceConnectionManager.acquire();
				result = 0;
				ioDeviceConnectionManager.release(anyInt);
				result = null;
			}
		};

		// valid IO state
		assertEquals(HaIOState.HIGH, rfr300Command.get());
		assertEquals(DeviceStatusEnumeration.IDLE, rfr300.getDeviceStatus());
		conf.setState(State.LOW);
		assertEquals(HaIOState.LOW, rfr300Command.get());
		conf.setState(State.UNKNOWN);
		assertEquals(HaIOState.UNKNOWN, rfr300Command.get());

		// missing configuration (throws directly an ApplicationException)
		confs.clear();
		try {
			rfr300Command.get();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// closing connection fails
		new Expectations() {
			{
				ioDeviceConnectionManager.release(anyInt);
				result = new Exception("huhu");
			}
		};
		confs.add(conf);
		try {
			rfr300Command.get();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// opening connection fails
		new Expectations() {
			{
				ioDeviceConnectionManager.acquire();
				result = new Exception("huhu");
			}
		};
		try {
			rfr300Command.get();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testGetIODirection(@Mocked final Logger log, @Mocked final IODevice ioDevice,
			@Mocked final IODeviceConnectionManager ioDeviceConnectionManager) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, ioDevice, configurationManager);

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getHs1Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getHs2Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getHs3Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getHs4Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getLs1Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getLs2Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getSws1Swd1Direction();
			}
		});

		checkGetIODirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOGetCommand<HaIODirection>() {

			@Override
			public HaIODirection get() throws ApplicationException {
				return rfr300.getSws2Swd2Direction();
			}
		});
	}

	private void checkGetIODirection(final Logger log, final IODevice ioDevice,
			final IODeviceConnectionManager ioDeviceConnectionManager, AimRfidReaderDevice rfr300,
			IOGetCommand<HaIODirection> rfr300Command) throws Exception {
		final List<havis.device.io.Configuration> confs = new ArrayList<>();
		final IOConfiguration conf = new IOConfiguration();
		conf.setDirection(Direction.INPUT);
		confs.add(conf);
		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				ioDeviceConnectionManager.getIODevice();
				result = ioDevice;

				ioDevice.getConfiguration(Type.IO, anyShort);
				result = confs;

				// reset expectations
				ioDeviceConnectionManager.acquire();
				result = 0;
				ioDeviceConnectionManager.release(anyInt);
				result = null;
			}
		};

		// valid IO state
		assertEquals(HaIODirection.INPUT, rfr300Command.get());
		assertEquals(DeviceStatusEnumeration.IDLE, rfr300.getDeviceStatus());
		conf.setDirection(Direction.OUTPUT);
		assertEquals(HaIODirection.OUTPUT, rfr300Command.get());

		// missing configuration (throws directly an ApplicationException)
		confs.clear();
		try {
			rfr300Command.get();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		// unexpected exceptions
		new Expectations() {
			{
				ioDeviceConnectionManager.release(anyInt);
				result = new Exception("huhu");
			}
		};
		confs.add(conf);
		try {
			rfr300Command.get();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		new Expectations() {
			{
				ioDeviceConnectionManager.acquire();
				result = new Exception("huhu");
			}
		};
		try {
			rfr300Command.get();
			fail();
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testSetState(@Mocked final Logger log, @Mocked final IODevice ioDevice,
			@Mocked final IODeviceConnectionManager ioDeviceConnectionManager) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, ioDevice, configurationManager);

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.HS1;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setHs1(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.HS2;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setHs2(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.HS3;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setHs3(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.HS4;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setHs4(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.SWS1_SWD1;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setSws1Swd1(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.SWS2_SWD2;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setSws2Swd2(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.LS1;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setLs1(value);
			}
		});

		checkSetState(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIOState>() {

			@Override
			public Param getParam() {
				return Param.LS2;
			}

			@Override
			public void set(HaIOState value) throws ApplicationException {
				rfr300.setLs2(value);
			}
		});
	}

	private void checkSetState(final Logger log, final IODevice ioDevice,
			final IODeviceConnectionManager ioDeviceConnectionManager, AimRfidReaderDevice rfr300,
			IOSetCommand<HaIOState> rfr300Command) throws Exception {
		final List<Param> changedParams = new ArrayList<>();
		final List<Object> changedValues = new ArrayList<>();
		rfr300.addParamChangedListener(new ParamChangedListener() {

			@Override
			public void paramChanged(Object source, Param param, Object newValue) {
				changedParams.add(param);
				changedValues.add(newValue);
			}
		});

		final List<havis.device.io.Configuration> confs = new ArrayList<>();
		final IOConfiguration conf = new IOConfiguration();
		conf.setState(State.LOW);
		conf.setDirection(Direction.OUTPUT);
		confs.add(conf);
		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				ioDeviceConnectionManager.getIODevice();
				result = ioDevice;

				ioDevice.getConfiguration(Type.IO, anyShort /* port */);
				result = confs;
			}
		};

		// valid states
		changedParams.clear();
		changedValues.clear();
		rfr300Command.set(HaIOState.HIGH);
		assertEquals(3, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(rfr300Command.getParam().toString(), changedParams.get(1).toString());
		assertEquals(HaIOState.HIGH.value, (int) changedValues.get(1));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(2).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(2));

		new Verifications() {
			{
				List<havis.device.io.Configuration> l = new ArrayList<>();
				ioDevice.setConfiguration(l = withCapture());
				times = 1;
				assertEquals(1, l.size());
				assertEquals(State.HIGH, ((IOConfiguration) l.get(0)).getState());
			}
		};

		changedParams.clear();
		changedValues.clear();
		rfr300Command.set(HaIOState.LOW);
		assertEquals(3, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(rfr300Command.getParam().toString(), changedParams.get(1).toString());
		assertEquals(HaIOState.LOW.value, (int) changedValues.get(1));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(2).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(2));

		new Verifications() {
			{
				List<havis.device.io.Configuration> l = new ArrayList<>();
				ioDevice.setConfiguration(l = withCapture());
				assertEquals(1, l.size());
				assertEquals(State.LOW, ((IOConfiguration) l.get(0)).getState());
			}
		};

		// try to write to input port (an ApplicationException is thrown
		// directly)
		conf.setDirection(Direction.INPUT);
		changedParams.clear();
		changedValues.clear();
		try {
			rfr300Command.set(HaIOState.HIGH);
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@Test
	public void testSetDirection(@Mocked final Logger log, @Mocked final IODevice ioDevice,
			@Mocked final IODeviceConnectionManager ioDeviceConnectionManager) throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, ioDevice, configurationManager);

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.HS1_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setHs1Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.HS2_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setHs2Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.HS3_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setHs3Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.HS4_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setHs4Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.LS1_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setLs1Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.LS2_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setLs2Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.SWS1_SWD1_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setSws1Swd1Direction(value);
			}
		});

		checkSetDirection(log, ioDevice, ioDeviceConnectionManager, rfr300, new IOSetCommand<HaIODirection>() {

			@Override
			public Param getParam() {
				return Param.SWS2_SWD2_DIRECTION;
			}

			@Override
			public void set(HaIODirection value) throws ApplicationException {
				rfr300.setSws2Swd2Direction(value);
			}
		});
	}

	private void checkSetDirection(final Logger log, final IODevice ioDevice,
			final IODeviceConnectionManager ioDeviceConnectionManager, AimRfidReaderDevice rfr300,
			IOSetCommand<HaIODirection> rfr300Command) throws Exception {
		final List<Param> changedParams = new ArrayList<>();
		final List<Object> changedValues = new ArrayList<>();
		rfr300.addParamChangedListener(new ParamChangedListener() {

			@Override
			public void paramChanged(Object source, Param param, Object newValue) {
				changedParams.add(param);
				changedValues.add(newValue);
			}
		});

		final List<havis.device.io.Configuration> confs = new ArrayList<>();
		final IOConfiguration conf = new IOConfiguration();
		conf.setDirection(Direction.OUTPUT);
		confs.add(conf);
		new Expectations() {
			{
				log.isLoggable(withInstanceOf(Level.class));
				result = true;

				ioDeviceConnectionManager.getIODevice();
				result = ioDevice;

				ioDevice.getConfiguration(Type.IO, anyShort /* port */);
				result = confs;
			}
		};

		// valid states
		changedParams.clear();
		changedValues.clear();
		rfr300Command.set(HaIODirection.INPUT);
		assertEquals(3, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(rfr300Command.getParam().toString(), changedParams.get(1).toString());
		assertEquals(HaIODirection.INPUT.value, (int) changedValues.get(1));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(2).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(2));

		new Verifications() {
			{
				List<havis.device.io.Configuration> l = new ArrayList<>();
				ioDevice.setConfiguration(l = withCapture());
				times = 1;
				assertEquals(1, l.size());
				assertEquals(Direction.INPUT, ((IOConfiguration) l.get(0)).getDirection());
			}
		};

		changedParams.clear();
		changedValues.clear();
		rfr300Command.set(HaIODirection.OUTPUT);
		assertEquals(3, changedParams.size());
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(0).toString());
		assertEquals(DeviceStatusEnumeration.BUSY, (DeviceStatusEnumeration) changedValues.get(0));
		assertEquals(rfr300Command.getParam().toString(), changedParams.get(1).toString());
		assertEquals(HaIODirection.OUTPUT.value, (int) changedValues.get(1));
		assertEquals(Param.DEVICE_STATUS.toString(), changedParams.get(2).toString());
		assertEquals(DeviceStatusEnumeration.IDLE, (DeviceStatusEnumeration) changedValues.get(2));

		new Verifications() {
			{
				List<havis.device.io.Configuration> l = new ArrayList<>();
				ioDevice.setConfiguration(l = withCapture());
				assertEquals(1, l.size());
				assertEquals(Direction.OUTPUT, ((IOConfiguration) l.get(0)).getDirection());
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testScan() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		setField(rfr300, "invThread", inventoryThread);
		try {
			rfr300.scan(new ScanSettings());
			fail("Exception expected.");
		} catch (ApplicationException aex) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, aex.getErrorCode());
		}

		setField(rfr300, "invThread", null);
		try {
			rfr300.scan(new ScanSettings());
			fail("Exception expected.");
		} catch (ApplicationException aex) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_ARGUMENT, aex.getErrorCode());
		}

		new Expectations() {
			{
				rfDevice.openConnection(withInstanceOf(RFConsumer.class), anyInt);
				result = new ConnectionException();
			}
		};
		final ScanSettings scanSettings = new ScanSettings();
		scanSettings.setDataAvailable(true);
		RfidScanResultPair srp0 = rfr300.scan(scanSettings);
		assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, srp0.getStatus());
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
		new Verifications() {
			{
				inventoryThread.startInventory(scanSettings, withInstanceOf(Lock.class), withInstanceOf(List.class),
						rfr300);
				times = 0;
			}
		};

		new Expectations() {
			{
				rfDevice.openConnection(withInstanceOf(RFConsumer.class), anyInt);
				result = null;

				inventoryThread.getError();
				result = new CommunicationException("expected exception");
			}
		};
		final RfidScanResultPair srp1 = rfr300.scan(scanSettings);
		assertEquals(AutoIdOperationStatusEnumeration.RF_COMMUNICATION_ERROR, srp1.getStatus());
		new Verifications() {
			{
				inventoryThread.startInventory(scanSettings, withInstanceOf(Lock.class), withInstanceOf(List.class),
						rfr300);
				times = 1;
			}
		};

		new Expectations() {
			{
				inventoryThread.getError();
				result = null;
			}
		};
		final RfidScanResultPair srp2 = rfr300.scan(scanSettings);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, srp2.getStatus());
		new Verifications() {
			{
				inventoryThread.startInventory(scanSettings, withInstanceOf(Lock.class), withInstanceOf(List.class),
						rfr300);
				times = 1;
			}
		};

		final RfidScanResult scanResult = new RfidScanResult();
		new Expectations() {
			{
				inventoryThread.startInventory(scanSettings, withInstanceOf(Lock.class), withInstanceOf(List.class),
						rfr300);
				result = Arrays.asList(scanResult);
			}
		};
		rfr300.addRfidEventListener(new RfidEventListener() {
			int count = 0;

			@Override
			public void rfidEventOccured(Object source, EventType event, Date timeStamp,
					Map<String, Object> eventArgs) {
				try {
					// exactly one event with the scan result is received
					if (count++ > 0) {
						fail();
					}
					assertSame(source, rfr300);
					assertEquals(EventType.RFID_SCAN_EVENT, event);
					assertEquals(eventArgs.get(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT), scanResult);
				} catch (Throwable e) {
					e.printStackTrace();
					throw e;
				}
			}
		});
		final RfidScanResultPair srp3 = rfr300.scan(scanSettings);
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, srp3.getStatus());
		assertEquals(1, srp3.getResults().size());
		new Verifications() {
			{
				inventoryThread.startInventory(scanSettings, withInstanceOf(Lock.class), withInstanceOf(List.class),
						rfr300);
				times = 1;
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testScanStart() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		setField(rfr300, "invThread", inventoryThread);
		try {
			rfr300.scanStart(new ScanSettings());
			fail("Exception expected.");
		} catch (ApplicationException aex) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, aex.getErrorCode());
		}

		setField(rfr300, "invThread", null);

		new NonStrictExpectations() {
			{
				rfDevice.openConnection(withInstanceOf(RFConsumer.class), anyInt);
				result = new ConnectionException();
			}
		};
		final ScanSettings scanSettings = new ScanSettings();
		scanSettings.setDataAvailable(true);
		final AutoIdOperationStatusEnumeration srp0 = rfr300.scanStart(scanSettings);
		new Verifications() {
			{
				assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, srp0);
				assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
			}
		};
		setField(rfr300, "invThread", null);

		new NonStrictExpectations() {
			{
				rfDevice.openConnection(withInstanceOf(RFConsumer.class), anyInt);
				result = null;

				inventoryThread.startInventoryAsync(scanSettings, withInstanceOf(List.class), rfr300, rfr300);
				result = new CommunicationException("expected exception");
			}
		};

		final AutoIdOperationStatusEnumeration srp1 = rfr300.scanStart(scanSettings);
		new Verifications() {
			{
				inventoryThread.startInventoryAsync(scanSettings, withInstanceOf(List.class), rfr300, rfr300);
				assertEquals(AutoIdOperationStatusEnumeration.RF_COMMUNICATION_ERROR, srp1);
			}
		};
		setField(rfr300, "invThread", null);

		new NonStrictExpectations() {
			{
				inventoryThread.startInventoryAsync(scanSettings, withInstanceOf(List.class), rfr300, rfr300);
				result = null;
			}
		};
		final AutoIdOperationStatusEnumeration srp2 = rfr300.scanStart(scanSettings);
		new Verifications() {
			{
				inventoryThread.startInventoryAsync(scanSettings, withInstanceOf(List.class), rfr300, rfr300);
				assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, srp2);
			}
		};
	}

	@Test
	public void testScanStop() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		try {
			rfr300.scanStop();
			fail("Exception expected");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.ERR_CODE_OPC_INVALID_STATE, e.getErrorCode());
		}

		setField(rfr300, "invThread", inventoryThread);

		/* Thread to send signal to scanAsyncStopped condition */
		new Thread(new Runnable() {
			@Override
			public void run() {
				Lock lock = getField(rfr300, "scanLock");
				Condition scanAsyncStopped = getField(rfr300, "scanAsyncStopped");
				try {
					Thread.sleep(100);
					lock.lock();
					scanAsyncStopped.signal();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}).start();

		boolean result = rfr300.scanStop();
		assertEquals(true, result);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testReadTag(@Mocked final ReadOperation rdOp, @Mocked final InventoryThread invThread,
			@Mocked final RFDeviceConnectionManager rfDeviceConnectionManager) throws Exception {
		final String epc = "300833B2DDD9004433221100";

		AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		ReadResultPair rrp = null;
		ScanData identifier = new ScanData();

		/*
		 * test: readTag call with non-existent region (memory bank) exp.: result is
		 * REGION_NOT_FOUND_ERROR
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, -1, 0, 0, null);
		assertEquals(AutoIdOperationStatusEnumeration.REGION_NOT_FOUND_ERROR, rrp.getStatus());

		/*
		 * test: readTag call with 32 bit long password exp.: password is used as-is
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 0,
				new byte[] { 0x11, 0x22, 0x33, 0x44 });
		new Verifications() {
			{
				int psw = 0;
				rdOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
			}
		};

		/*
		 * test: readTag call with 16 bit long password exp.: two 0x00-bytes are
		 * appended to the password
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 0, new byte[] { 0x11, 0x22 });
		new Verifications() {
			{
				int psw = 0;
				rdOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x00, 0x00 }), psw);
			}
		};

		/*
		 * test: readTag call with 48 bit long password exp.: password is truncated to
		 * 32 bit
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 0,
				new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 });
		new Verifications() {
			{
				int psw = 0;
				rdOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
			}
		};

		/*
		 * test: readTag call even offset and even length exp.: length and offset are
		 * divided by 2 to be converted into words
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, /* offset */4, /* length */8, null);
		new Verifications() {
			{
				short off = 0;
				short len = 0;
				rdOp.setLength(len = withCapture());
				rdOp.setOffset(off = withCapture());
				assertEquals(2, off);
				assertEquals(4, len);
			}
		};

		/*
		 * test: readTag call odd offset and even length exp.: length is increased by
		 * one byte and divided by 2 (=5 words) offset is decreased by one byte and
		 * divided by 2 (=2 words)
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, /* offset */5, /* length */8, null);
		new Verifications() {
			{
				short off = 0;
				short len = 0;
				rdOp.setLength(len = withCapture());
				rdOp.setOffset(off = withCapture());
				assertEquals(2, off);
				assertEquals(5, len);
			}
		};

		/*
		 * test: readTag call even offset and odd length exp.: length is increased by
		 * one byte and divided by 2 (=5 words) offset is divided by 2 (=2 words)
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, /* offset */4, /* length */9, null);
		new Verifications() {
			{
				short off = 0;
				short len = 0;
				rdOp.setLength(len = withCapture());
				rdOp.setOffset(off = withCapture());
				assertEquals(2, off);
				assertEquals(5, len);
			}
		};

		/*
		 * test: readTag call odd offset and odd length exp.: length is increased by one
		 * byte and divided by 2 (=5 words) offset is decreased by one byte and divided
		 * by 2 (=2 words)
		 */
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, /* offset */5, /* length */9, null);
		new Verifications() {
			{
				short off = 0;
				short len = 0;
				rdOp.setLength(len = withCapture());
				rdOp.setOffset(off = withCapture());
				assertEquals(2, off);
				assertEquals(5, len);
			}
		};

		final List<TagData> tags = new ArrayList<>();
		TagData td = new TagData();
		td.setEpc(RFUtils.hexToBytes(epc));
		td.setCrc((short) 0);
		td.setPc((short) 0);
		td.setRssi(-62);

		final ReadResult rdRes = new ReadResult();
		rdRes.setOperationId("");
		rdRes.setResult(ReadResult.Result.SUCCESS);
		td.getResultList().add(rdRes);
		tags.add(td);

		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};

		/*
		 * test: readTag call with empty identifier exp.: result is NO_IDENTIFIER
		 */
		identifier.setEpc(new ScanDataEpc());
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, rrp.getStatus());

		/*
		 * test: readTag call with incomplete identifier exp: result is NO_IDENTIFIER
		 */
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc.substring(0, 6)));
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, rrp.getStatus());

		// set complete identifier
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));

		int length = 0;
		int offset = 0;

		/* read result word to byte conversion tests */
		class ReadAddressRange {
			short wOffset;
			short wLength;
		}
		final ReadAddressRange rar = new ReadAddressRange();
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new Delegate<List>() {
					@SuppressWarnings("unused")
					public List<TagData> execute(List<Short> antennas, List<Filter> filters,
							List<TagOperation> operations) {
						String hexStr = epc.substring(rar.wOffset * 4, 4 * (rar.wOffset + rar.wLength));
						rdRes.setReadData(RFUtils.hexToBytes(hexStr));
						return tags;
					}
				};
			}
		};

		/* EPC: 300833B2DDD9004433221100 */

		/*
		 * test: readTag call with even offset (4) and even length (6) exp.: result is 6
		 * bytes with offset 4 of EPC: DDD900443322
		 */
		offset = 4;
		length = 6;
		rar.wOffset = (short) (offset / 2);
		rar.wLength = (short) (length / 2);
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, offset, length, null);
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, rrp.getStatus());
		assertEquals("DDD900443322", RFUtils.bytesToHex(rrp.getResultData()));

		/*
		 * test: readTag call with odd offset (5) and even length(4) exp.: result is 4
		 * bytes with offset 5 of EPC: D9004433
		 */
		offset = 5;
		length = 4;
		rar.wOffset = (short) ((offset - 1) / 2);
		rar.wLength = (short) (length / 2 + 1);
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, offset, length, null);
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, rrp.getStatus());
		assertEquals("D9004433", RFUtils.bytesToHex(rrp.getResultData()));

		/*
		 * test: readTag call with even offset (4) and odd length(5) exp.: result is 4
		 * bytes with offset 5 of EPC: DDD9004433
		 */
		offset = 4;
		length = 5;
		rar.wOffset = (short) (offset / 2);
		rar.wLength = (short) ((length + 1) / 2);
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, offset, length, null);
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, rrp.getStatus());
		assertEquals("DDD9004433", RFUtils.bytesToHex(rrp.getResultData()));

		/*
		 * test: readTag call with odd offset (3) and odd length(5) exp.: result is 5
		 * bytes with offset 3 of EPC: B2DDD90044
		 */
		offset = 3;
		length = 5;
		rar.wOffset = (short) ((offset - 1) / 2);
		rar.wLength = (short) ((length + 1) / 2);
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, offset, length, null);
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, rrp.getStatus());
		assertEquals("B2DDD90044", RFUtils.bytesToHex(rrp.getResultData()));

		/*
		 * test: readTag call with RFID backend returning more than one tag exp.: result
		 * is MULTIPLE_IDENTIFIERS
		 */
		tags.add(td);
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.MULTIPLE_IDENTIFIERS, rrp.getStatus());

		/*
		 * test: readTag call with RFID backend returning no tags exp.: result is
		 * NO_IDENTIFIER
		 */
		tags.clear();
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, rrp.getStatus());

		/*
		 * test: readTag call with RFID backend returning tag with empty result list
		 * exp.: result is MISC_ERROR_TOTAL
		 */
		tags.add(td);
		td.getResultList().clear();
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, rrp.getStatus());

		/*
		 * test: readTag call with RFID backend throwing ParameterException exp.: result
		 * is MISC_ERROR_TOTAL
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new ParameterException("expected exception");
			}
		};
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, rrp.getStatus());

		/*
		 * test: readTag call with RFID backend throwing ImplementationException exp.:
		 * result is DEVICE_NOT_READY, device status is ERROR
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new ImplementationException("expected exception");
			}
		};
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, rrp.getStatus());
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		/*
		 * test: readTag call with RFID backend throwing Exception during connect exp.:
		 * result is DEVICE_NOT_READY, device status is ERROR
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;

				rfDeviceConnectionManager.acquire();
				result = new ConnectionException("Expected exception");
			}
		};
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, rrp.getStatus());
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());

		/*
		 * test: readTag call with existing inventory thread to be suspended / resumed
		 * exp.: suspendInventory() and resumeInventory() methods of invThread are
		 * called
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;

				rfDeviceConnectionManager.acquire();
				result = 0;

				invThread.suspendInventory();
				result = true;
			}
		};

		setField(rfr300, "invThread", invThread);
		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		new Verifications() {
			{
				invThread.suspendInventory();
				invThread.resumeInventory();
			}
		};

		/*
		 * test: readTag call with existing inventory thread returning false upon
		 * suspend attempt exp.: readTag method return DEVICE_NOT_READY
		 */
		new NonStrictExpectations() {
			{
				invThread.suspendInventory();
				result = false;
			}
		};

		rrp = rfr300.readTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 0, 4, null);
		assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, rrp.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWriteTag(@Mocked final WriteOperation wrOp, @Mocked final InventoryThread invThread)
			throws Exception {
		final String epc = "300833B2DDD9004433221100";

		AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		AutoIdOperationStatusEnumeration wr = null;
		ScanData identifier = new ScanData();

		/*
		 * test: writeTag call with non-existent region (memory bank) exp.: result is
		 * REGION_NOT_FOUND_ERROR
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, -1, 0, new byte[] { 0x11, 0x22, 0x33, 0x44 }, null);
		assertEquals(AutoIdOperationStatusEnumeration.REGION_NOT_FOUND_ERROR, wr);

		/*
		 * test: writeTag call with odd offset exp.: result is NOT_SUPPORTED_BY_DEVICE
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 3,
				new byte[] { 0x11, 0x22, 0x33, 0x44 }, null);
		assertEquals(AutoIdOperationStatusEnumeration.NOT_SUPPORTED_BY_DEVICE, wr);

		/*
		 * test: writeTag call with odd number of bytes exp.: result is
		 * NOT_SUPPORTED_BY_DEVICE
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4,
				new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55 }, null);
		assertEquals(AutoIdOperationStatusEnumeration.NOT_SUPPORTED_BY_DEVICE, wr);

		/*
		 * test: writeTag call with even offset and event number of bytes exp.: data and
		 * offset is set in write operation, result is NO_IDENTIFIER (since identifier
		 * is still empty)
		 */
		final byte[] data = new byte[] { 0x11, 0x22, 0x33, 0x44 };
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);

		new Verifications() {
			{
				byte[] data2 = null;
				wrOp.setBank(RFUtils.BANK_EPC);
				wrOp.setOffset((short) 2);
				wrOp.setData(data2 = withCapture());
				assertArrayEquals(data, data2);
			}
		};

		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, wr);

		/*
		 * test: writeTag call with 32 bit long password exp.: password is used as-is
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data,
				new byte[] { 0x11, 0x22, 0x33, 0x44 });
		new Verifications() {
			{
				int psw = 0;
				wrOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
			}
		};

		/*
		 * test: writeTag call with 16 bit long password exp.: two 0x00-bytes are
		 * appended to the password
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, new byte[] { 0x11, 0x22 });
		new Verifications() {
			{
				int psw = 0;
				wrOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x00, 0x00 }), psw);
			}
		};

		/*
		 * test: writeTag call with 48 bit long password exp.: password is truncated to
		 * 32 bit
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data,
				new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 });
		new Verifications() {
			{
				int psw = 0;
				wrOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
			}
		};
		identifier.setEpc(new ScanDataEpc());
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));

		/*
		 * test: writeTag call with no tag in the field exp.: result is NO_IDENTIFIER
		 */
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, wr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};

		final List<TagData> tags = new ArrayList<>();
		TagData td = new TagData();
		td.setEpc(RFUtils.hexToBytes(epc));
		td.setCrc((short) 0);
		td.setPc((short) 0);
		td.setRssi(-62);

		final WriteResult wrRes = new WriteResult();
		wrRes.setWordsWritten((short) (data.length / 4));
		wrRes.setResult(WriteResult.Result.SUCCESS);

		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};

		/*
		 * test: writeTag call with multiple tags in the field exp.: result is
		 * MULTIPLE_IDENTIFIER
		 */
		tags.add(td);
		tags.add(td);
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);
		assertEquals(AutoIdOperationStatusEnumeration.MULTIPLE_IDENTIFIERS, wr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};
		tags.remove(0);

		/*
		 * test: writeTag call with incomplete identifier exp.: result is NO_IDENTIFIER
		 */

		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc.substring(0, 8)));
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, wr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};

		/*
		 * test: writeTag call with complete identifier exp.: result is SUCCESS
		 */
		td.getResultList().add(wrRes);
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				times = 2;
			}
		};
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, wr);

		/*
		 * test: writeTag call with RFID backend throwing a parameter exception exp.:
		 * result is MISC_ERROR_TOTAL, device status is IDLE
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new ParameterException("expected exception");
			}
		};
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, wr);
		assertEquals(DeviceStatusEnumeration.IDLE, rfr300.getDeviceStatus());

		/*
		 * test: writeTag call with RFID backend throwing an implementation exception
		 * exp.: result is DEVICE_NOT_READY, device status is ERROR
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new ImplementationException("expected exception");
			}
		};
		wr = rfr300.writeTag(identifier, CodeTypeEnumeration.EPC, RFUtils.BANK_EPC, 4, data, null);
		assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, wr);
		assertEquals(DeviceStatusEnumeration.ERROR, rfr300.getDeviceStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLockTag(@Mocked final LockOperation lOp) throws Exception {
		final String epc = "300833B2DDD9004433221100";

		AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		ScanData identifier = new ScanData();
		AutoIdOperationStatusEnumeration lr = null;

		/*
		 * test: lockTag call with 32 bit long password exp.: password is used as-is,
		 * field and privilege are set in lock operation
		 */
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.ACCESS, RfidLockOperationEnumeration.LOCK, 0, 0);

		new Verifications() {
			{
				int psw = 0;
				lOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
				lOp.setField(Field.ACCESS_PASSWORD);
				lOp.setPrivilege(Privilege.LOCK);
			}
		};

		/*
		 * test: lockTag call with 16 bit long password exp.: two 0x00-bytes are
		 * appended to the password, field and privilege are set in lock operation
		 */
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22 },
				RfidLockRegionEnumeration.EPC, RfidLockOperationEnumeration.PERMANENTLOCK, 0, 0);

		new Verifications() {
			{
				int psw = 0;
				lOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x00, 0x00 }), psw);
				lOp.setField(Field.EPC_MEMORY);
				lOp.setPrivilege(Privilege.PERMALOCK);
			}
		};

		/*
		 * test: lockTag call with 48 bit long password exp.: password is truncated to
		 * 32 bit, field and privilege are set in lock operation
		 */
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 },
				RfidLockRegionEnumeration.KILL, RfidLockOperationEnumeration.PERMANENTUNLOCK, 0, 0);

		new Verifications() {
			{
				int psw = 0;
				lOp.setPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
				lOp.setField(Field.KILL_PASSWORD);
				lOp.setPrivilege(Privilege.PERMAUNLOCK);
			}
		};

		/*
		 * test: lockTag call with empty identifier exp.: result is NO_IDENTIFIER
		 */
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.TID, RfidLockOperationEnumeration.UNLOCK, 0, 0);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, lr);

		/*
		 * test: killTag call with no tag in the field exp.: result is NO_IDENTIFIER
		 */
		identifier.setEpc(new ScanDataEpc());
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.USER, RfidLockOperationEnumeration.LOCK, 0, 0);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, lr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};

		final List<TagData> tags = new ArrayList<>();
		TagData td = new TagData();
		td.setEpc(RFUtils.hexToBytes(epc));
		td.setCrc((short) 0);
		td.setPc((short) 0);
		td.setRssi(-62);

		final LockResult lkRes = new LockResult();
		lkRes.setResult(LockResult.Result.SUCCESS);

		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};

		/*
		 * test: lockTag call with multiple tags in the field exp.: result is
		 * MULTIPLE_IDENTIFIER
		 */
		tags.add(td);
		tags.add(td);
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.ACCESS, RfidLockOperationEnumeration.LOCK, 0, 0);
		assertEquals(AutoIdOperationStatusEnumeration.MULTIPLE_IDENTIFIERS, lr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};
		tags.remove(0);

		/*
		 * test: lockTag call with incomplete identifier exp.: result is NO_IDENTIFIER
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc.substring(0, 8)));
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.ACCESS, RfidLockOperationEnumeration.LOCK, 0, 0);
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, lr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};

		/*
		 * test: lockTag call with complete identifier exp.: result is SUCCESS
		 */
		td.getResultList().add(lkRes);
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.ACCESS, RfidLockOperationEnumeration.LOCK, 0, 0);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				times = 2;
			}
		};
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, lr);

		/*
		 * test: lockTag call with RFID backend INCORRECT_PASSWORD_ERROR exp.: result is
		 * PERMISSION_ERROR
		 */
		lkRes.setResult(LockResult.Result.INCORRECT_PASSWORD_ERROR);
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		lr = rfr300.lockTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 },
				RfidLockRegionEnumeration.ACCESS, RfidLockOperationEnumeration.LOCK, 0, 0);

		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				times = 2;
			}
		};

		assertEquals(AutoIdOperationStatusEnumeration.PERMISSON_ERROR, lr);
	}

	@Test
	public void testSetTagPassword(@Mocked final WriteOperation wrOp) throws Exception {

		final String epc = "300833B2DDD9004433221100";

		AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		AutoIdOperationStatusEnumeration res = null;
		ScanData identifier = new ScanData();
		identifier = new ScanData();
		identifier.setEpc(new ScanDataEpc());
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));

		final List<TagData> tags = new ArrayList<>();
		TagData td = new TagData();
		td.setEpc(RFUtils.hexToBytes(epc));
		td.setCrc((short) 0);
		td.setPc((short) 0);
		td.setRssi(-62);
		tags.add(td);

		res = rfr300.setTagPassword(identifier, CodeTypeEnumeration.EPC, RfidPasswordTypeEnumeration.READ,
				new byte[] {}, new byte[] {});
		assertEquals(AutoIdOperationStatusEnumeration.NOT_SUPPORTED_BY_DEVICE, res);

		res = rfr300.setTagPassword(identifier, CodeTypeEnumeration.EPC, RfidPasswordTypeEnumeration.WRITE,
				new byte[] {}, new byte[] {});
		assertEquals(AutoIdOperationStatusEnumeration.NOT_SUPPORTED_BY_DEVICE, res);

		res = rfr300.setTagPassword(identifier, CodeTypeEnumeration.EPC, RfidPasswordTypeEnumeration.KILL,
				new byte[] {}, new byte[] { 0x11, 0x22 });
		new Verifications() {
			{
				wrOp.setData(new byte[] { 0x11, 0x22, 0x00, 0x00 });
				wrOp.setBank(RFUtils.BANK_PSW);
				wrOp.setOffset((short) 0);
			}
		};

		res = rfr300.setTagPassword(identifier, CodeTypeEnumeration.EPC, RfidPasswordTypeEnumeration.ACCESS,
				new byte[] {}, new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 });
		new Verifications() {
			{
				wrOp.setData(new byte[] { 0x11, 0x22, 0x33, 0x44 });
				wrOp.setBank(RFUtils.BANK_PSW);
				wrOp.setOffset((short) 2);
			}
		};

		res = rfr300.setTagPassword(identifier, CodeTypeEnumeration.EPC, RfidPasswordTypeEnumeration.KILL,
				new byte[] {}, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		new Verifications() {
			{
				wrOp.setData(new byte[] { 0x11, 0x22, 0x33, 0x44 });
				wrOp.setBank(RFUtils.BANK_PSW);
				wrOp.setOffset((short) 0);
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testKillTag(@Mocked final KillOperation kOp) throws Exception {
		final String epc = "300833B2DDD9004433221100";

		AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		ScanData identifier = new ScanData();
		AutoIdOperationStatusEnumeration kr = null;

		/*
		 * test: killTag call with no kill password exp.: result is PERMISSION_ERROR
		 */
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, null);
		assertEquals(AutoIdOperationStatusEnumeration.PERMISSON_ERROR, kr);

		/*
		 * test: killTag call with 32 bit long password exp.: password is used as-is
		 */
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		new Verifications() {
			{
				int psw = 0;
				kOp.setKillPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
			}
		};

		/*
		 * test: killTag call with 16 bit long password exp.: two 0x00-bytes are
		 * appended to the password
		 */
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22 });
		new Verifications() {
			{
				int psw = 0;
				kOp.setKillPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x00, 0x00 }), psw);
			}
		};

		/*
		 * test: killTag call with 48 bit long password exp.: password is truncated to
		 * 32 bit
		 */
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 });
		new Verifications() {
			{
				int psw = 0;
				kOp.setKillPassword(psw = withCapture());
				assertEquals(RFUtils.bytesToInt(new byte[] { 0x11, 0x22, 0x33, 0x44 }), psw);
			}
		};

		/*
		 * test: killTag call with empty identifier exp.: result is NO_IDENTIFIER
		 */
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, kr);

		/*
		 * test: killTag call with no tag in the field exp.: result is NO_IDENTIFIER
		 */
		identifier.setEpc(new ScanDataEpc());
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, kr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};

		final List<TagData> tags = new ArrayList<>();
		TagData td = new TagData();
		td.setEpc(RFUtils.hexToBytes(epc));
		td.setCrc((short) 0);
		td.setPc((short) 0);
		td.setRssi(-62);

		final KillResult klRes = new KillResult();
		klRes.setResult(KillResult.Result.SUCCESS);

		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};

		/*
		 * test: killTag call with multiple tags in the field exp.: result is
		 * MULTIPLE_IDENTIFIER
		 */
		tags.add(td);
		tags.add(td);
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		assertEquals(AutoIdOperationStatusEnumeration.MULTIPLE_IDENTIFIERS, kr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};
		tags.remove(0);

		/*
		 * test: killTag call with incomplete identifier exp.: result is NO_IDENTIFIER
		 */
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc.substring(0, 8)));
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		assertEquals(AutoIdOperationStatusEnumeration.NO_IDENTIFIER, kr);
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
			}
		};

		/*
		 * test: killTag call with complete identifier exp.: result is SUCCESS
		 */
		td.getResultList().add(klRes);
		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = tags;
			}
		};
		identifier.getEpc().setUid(RFUtils.hexToBytes(epc));
		kr = rfr300.killTag(identifier, CodeTypeEnumeration.EPC, new byte[] { 0x11, 0x22, 0x33, 0x44 });
		new Verifications() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				times = 2;
			}
		};
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, kr);
	}

	@Test
	public void testSetAntennaNames() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);

		List<AntennaNameIdPair> list0 = new ArrayList<>();
		AntennaNameIdPair anip0 = new AntennaNameIdPair();
		anip0.setAntennaId(Environment.VARIABLE_VAL_ANTENNA_ID);
		anip0.setAntennaName(Environment.VARIABLE_VAL_ANTENNA_NAME);
		list0.add(anip0);

		setField(rfr300, "antennaNames", list0);

		final List<AntennaNameIdPair> list1 = new ArrayList<>();
		AntennaNameIdPair anip1 = new AntennaNameIdPair();
		anip1.setAntennaId(1);
		anip1.setAntennaName("Antenna 1");

		AntennaNameIdPair anip2 = new AntennaNameIdPair();
		anip2.setAntennaId(2);
		anip2.setAntennaName("Antenna 2");

		list1.add(anip1);
		list1.add(anip2);

		rfr300.setAntennaNames(list1);

		new Verifications() {
			{

				List<AntennaNameIdPair> list2 = getField(rfr300, "antennaNames");

				assertEquals(list1.size(), list2.size());
				assertEquals(list1.get(0).getAntennaId(), list2.get(0).getAntennaId());
				assertEquals(list1.get(0).getAntennaName(), list2.get(0).getAntennaName());
				assertEquals(list1.get(1).getAntennaId(), list2.get(1).getAntennaId());
				assertEquals(list1.get(1).getAntennaName(), list2.get(1).getAntennaName());

				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.ANTENNA_NAMES),
						withSameInstance(list1));

			}
		};
	}

	@Test
	public void testScanAsyncFinished() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		setField(rfr300, "invThread", inventoryThread);
		final Lock lock = getField(rfr300, "scanLock");
		final Condition scanAsyncStopped = getField(rfr300, "scanAsyncStopped");

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.lock();
					scanAsyncStopped.await();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		});

		t.start();
		assert (t.isAlive());
		rfr300.scanAsyncFinished(this);
		Thread.sleep(10);
		assert (!t.isAlive());
		assertNull(getField(rfr300, "invThread"));

	}

	@Test
	public void testLastScanDataChanged() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		ScanData scanData = new ScanData();
		rfr300.lastScanDataChanged(this, null, scanData);
		assertEquals(scanData, getField(rfr300, "lastScanData"));
	}

	@Test
	public void testRfidEventOccured() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addRfidEventListener(rfidEventListener);

		final Map<String, Object> eventArgs = new HashMap<>();
		eventArgs.put("foo", "bar");
		rfr300.rfidEventOccured(this, EventType.RFID_SCAN_EVENT, new Date(), eventArgs);

		new Verifications() {
			{
				rfidEventListener.rfidEventOccured(any, EventType.RFID_SCAN_EVENT, withInstanceOf(Date.class),
						eventArgs);
				assertEquals(2, eventArgs.size());
			}
		};

	}

	@Test
	public void testSetRevisionCounter() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setRevisionCounter(10);

		new Verifications() {
			{
				assertEquals(10, getField(rfr300, "revisionCounter"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.REVISION_COUNTER),
						10);
			}
		};
	}

	@Test
	public void testAddRemoveParamChangedListener() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		List<ParamChangedListener> paramChangedListeners = getField(rfr300, "paramChangedListeners");
		assertEquals(0, paramChangedListeners.size());

		rfr300.addParamChangedListener(paramChangedListener);
		assertEquals(1, paramChangedListeners.size());
		assertEquals(paramChangedListener, paramChangedListeners.get(0));

		rfr300.removeParamChangedListener(paramChangedListener);
		assertEquals(0, paramChangedListeners.size());
	}

	@Test
	public void testAddRemoveRfidEventListener() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		List<RfidEventListener> rfidEventListeners = getField(rfr300, "rfidEventListeners");
		assertEquals(0, rfidEventListeners.size());

		rfr300.addRfidEventListener(rfidEventListener);
		assertEquals(1, rfidEventListeners.size());
		assertEquals(rfidEventListener, rfidEventListeners.get(0));

		rfr300.removeRfidEventListener(rfidEventListener);
		assertEquals(0, rfidEventListeners.size());
	}

	@Test
	public void testSetAutoIdModelVersion() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setAutoIdModelVersion("2.0");

		new Verifications() {
			{
				assertEquals("2.0", getField(rfr300, "autoIdModelVersion"));
				paramChangedListener.paramChanged(withSameInstance(rfr300),
						withSameInstance(Param.AUTOID_MODEL_VERSION), "2.0");
			}
		};
	}

	@Test
	public void testSetDeviceInfo() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setDeviceInfo("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "deviceInfo"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.DEVICE_INFO), "foo");
			}
		};
	}

	@Test
	public void testSetDeviceManual() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setDeviceManual("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "deviceManual"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.DEVICE_MANUAL),
						"foo");
			}
		};
	}

	@Test
	public void testSetDeviceName() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setDeviceName("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "deviceName"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.DEVICE_NAME), "foo");
			}
		};
	}

	@Test
	public void testSetDeviceRevision() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setDeviceRevision("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "deviceRevision"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.DEVICE_REVISION),
						"foo");
			}
		};
	}

	@Test
	public void testSetDeviceStatus() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setDeviceStatus(DeviceStatusEnumeration.BUSY);

		new Verifications() {
			{
				assertEquals(DeviceStatusEnumeration.BUSY, getField(rfr300, "deviceStatus"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.DEVICE_STATUS),
						DeviceStatusEnumeration.BUSY);
			}
		};
	}

	@Test
	public void testSetHardwareRevision() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setHardwareRevision("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "hardwareRevision"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.HARDWARE_REVISION),
						"foo");
			}
		};
	}

	@Test
	public void testSetLastScanData() throws Exception {
		final ScanData scanData = new ScanData();

		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setLastScanData(scanData);

		new Verifications() {
			{
				assertEquals(scanData, getField(rfr300, "lastScanData"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.LAST_SCAN_DATA),
						scanData);
			}
		};
	}

	@Test
	public void testSetManufacturer() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setManufacturer("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "manufacturer"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.MANUFACTURER),
						"foo");
			}
		};
	}

	@Test
	public void testSetModel() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setModel("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "model"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.MODEL), "foo");
			}
		};
	}

	@Test
	public void testSetSerialNumber() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setSerialNumber("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "serialNumber"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.SERIAL_NUMBER),
						"foo");
			}
		};
	}

	@Test
	public void testSetSoftwareRevision() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);
		rfr300.addParamChangedListener(paramChangedListener);
		rfr300.setSoftwareRevision("foo");

		new Verifications() {
			{
				assertEquals("foo", getField(rfr300, "softwareRevision"));
				paramChangedListener.paramChanged(withSameInstance(rfr300), withSameInstance(Param.SOFTWARE_REVISION),
						"foo");
			}
		};
	}

	@Test
	public void testNotifyRfidEvent() throws Exception {
		final AimRfidReaderDevice rfr300 = new AimRfidReaderDevice(rfDevice, null /* ioDevice */, configurationManager);

		final Date timeStamp = new Date();
		final Map<String, Object> eventArgs = new HashMap<>();
		eventArgs.put("foo", new StringBuffer(new String("bar")));

		rfr300.addRfidEventListener(rfidEventListener);
		rfr300.notifyRfidEvent(EventType.RFID_SCAN_EVENT, timeStamp, eventArgs);

		new Verifications() {
			{
				rfidEventListener.rfidEventOccured(withSameInstance(rfr300), EventType.RFID_SCAN_EVENT,
						withSameInstance(timeStamp), withSameInstance(eventArgs));
			}
		};
	}

}
