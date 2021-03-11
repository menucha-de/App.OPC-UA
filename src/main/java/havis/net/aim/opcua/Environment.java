package havis.net.aim.opcua;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.DeviceStatusEnumeration;
import havis.net.aim.xsd.TagTypeEnumeration;

public class Environment {
	private final static Logger log = Logger.getLogger(Environment.class.getName());

	private final static Properties properties = new Properties();

	static {
		try (InputStream stream = Environment.class.getClassLoader().getResourceAsStream("havis.net.aim.properties")) {
			properties.load(stream);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public static final String ANTENNANAMEIDPAIR_ID = properties.getProperty("havis.net.aim.serialize.AntennaNameIdPair.id", "antenna_");
	public static final String ANTENNANAMEIDPAIR_ANTENNAID = properties.getProperty("havis.net.aim.serialize.AntennaNameIdPair.antennaId", "AntennaId");
	public static final String ANTENNANAMEIDPAIR_ANTENNANAME = properties.getProperty("havis.net.aim.serialize.AntennaNameIdPair.antennaName", "AntennaName");
	public static final String RFIDSCANRESULT_ID = properties.getProperty("havis.net.aim.serialize.RfidScanResult.id", "rfidScanResult_");
	public static final String RFIDSCANRESULT_SIGHTING = properties.getProperty("havis.net.aim.serialize.RfidScanResult.sighting", "Sighting");
	public static final String RFIDSCANRESULT_CODETYPE = properties.getProperty("havis.net.aim.serialize.RfidScanResult.codeType", "CodeType");
	public static final String RFIDSCANRESULT_SCANDATA = properties.getProperty("havis.net.aim.serialize.RfidScanResult.scanData", "ScanData");
	public static final String RFIDSCANRESULT_TIMESTAMP = properties.getProperty("havis.net.aim.serialize.RfidScanResult.timestamp", "Timestamp");
	public static final String RFIDSIGHTING_ID = properties.getProperty("havis.net.aim.serialize.RfidSighting.id", "rfidSighting_");
	public static final String RFIDSIGHTING_ANTENNA = properties.getProperty("havis.net.aim.serialize.RfidSighting.antenna", "Antenna");
	public static final String RFIDSIGHTING_STRENGTH = properties.getProperty("havis.net.aim.serialize.RfidSighting.strength", "Strength");
	public static final String RFIDSIGHTING_TIMESTAMP = properties.getProperty("havis.net.aim.serialize.RfidSighting.timestamp", "Timestamp");
	public static final String RFIDSIGHTING_CURRENTPOWERLEVEL = properties.getProperty("havis.net.aim.serialize.RfidSighting.currentPowerLevel",
			"CurrentPowerLevel");
	public static final String SCANDATA_ID = properties.getProperty("havis.net.aim.serialize.ScanData.id", "scanData_");
	public static final String SCANDATA_BYTESTRING = properties.getProperty("havis.net.aim.serialize.ScanData.byteString", "ByteString");
	public static final String SCANDATA_STRING = properties.getProperty("havis.net.aim.serialize.ScanData.string", "String");
	public static final String SCANDATA_EPC = properties.getProperty("havis.net.aim.serialize.ScanData.epc", "Epc");
	public static final String SCANDATAEPC_ID = properties.getProperty("havis.net.aim.serialize.ScanDataEpc.id", "epc_");
	public static final String SCANDATAEPC_PC = properties.getProperty("havis.net.aim.serialize.ScanDataEpc.pc", "PC");
	public static final String SCANDATAEPC_UID = properties.getProperty("havis.net.aim.serialize.ScanDataEpc.uid", "UId");
	public static final String SCANDATAEPC_XPC_W1 = properties.getProperty("havis.net.aim.serialize.ScanDataEpc.xpc_w1", "XPC_W1");
	public static final String SCANDATAEPC_XPC_W2 = properties.getProperty("havis.net.aim.serialize.ScanDataEpc.xpc_w2", "XPC_W2");
	public static final String LASTSCANDATA_ID = properties.getProperty("havis.net.aim.serialize.LastScanData.id", "lastScanData_");
	public static final String SCANSETTINGS_CYCLES = properties.getProperty("havis.net.aim.serialize.ScanSettings.cycles", "Cycles");
	public static final String SCANSETTINGS_DATAAVAILABLE = properties.getProperty("havis.net.aim.serialize.ScanSettings.dataAvailable", "DataAvailable");
	public static final String SCANSETTINGS_DURATION = properties.getProperty("havis.net.aim.serialize.ScanSettings.duration", "Duration");

	public static final String INSTANCE_NAME = properties.getProperty("havis.net.aim.instance.name", "rfr310");
	public static final String VARIABLE_NAME_ANTENNA_NAMES = properties
			.getProperty("havis.net.aim.variable.name.antennaNames", INSTANCE_NAME + ".AntennaNames");
	public static final String VARIABLE_NAME_AUTOID_MODEL_VERSION = properties.getProperty("havis.net.aim.variable.name.autoIdModelVersion", INSTANCE_NAME
			+ ".AutoIdModelVersion");
	public static final String VARIABLE_NAME_DEVICE_NAME = properties.getProperty("havis.net.aim.variable.name.deviceName", INSTANCE_NAME + ".DeviceName");
	public static final String VARIABLE_NAME_DEVICE_STATUS = properties
			.getProperty("havis.net.aim.variable.name.deviceStatus", INSTANCE_NAME + ".DeviceStatus");
	public static final String VARIABLE_NAME_DEVICE_INFO = properties.getProperty("havis.net.aim.variable.name.deviceInfo", INSTANCE_NAME + ".DeviceInfo");
	public static final String VARIABLE_NAME_MANUFACTURER = properties.getProperty("havis.net.aim.variable.name.manufacturer", INSTANCE_NAME + ".Manufacturer");
	public static final String VARIABLE_NAME_MODEL = properties.getProperty("havis.net.aim.variable.name.model", INSTANCE_NAME + ".Model");
	public static final String VARIABLE_NAME_SOFTWARE_REVISION = properties.getProperty("havis.net.aim.variable.name.softwareRevision", INSTANCE_NAME
			+ ".SoftwareRevision");
	public static final String VARIABLE_NAME_LAST_SCAN_DATA = properties.getProperty("havis.net.aim.variable.name.lastScanData", INSTANCE_NAME
			+ ".LastScanData");
	public static final String VARIABLE_NAME_DEVICE_MANUAL = properties
			.getProperty("havis.net.aim.variable.name.deviceManual", INSTANCE_NAME + ".DeviceManual");
	public static final String VARIABLE_NAME_DEVICE_REVISION = properties.getProperty("havis.net.aim.variable.name.deviceRevision", INSTANCE_NAME
			+ ".DeviceRevision");
	public static final String VARIABLE_NAME_HARDWARE_REVISION = properties.getProperty("havis.net.aim.variable.name.hardwareRevision", INSTANCE_NAME
			+ ".HardwareRevision");
	public static final String VARIABLE_NAME_REVISION_COUNTER = properties.getProperty("havis.net.aim.variable.name.revisionCounter", INSTANCE_NAME
			+ ".RevisionCounter");
	public static final String VARIABLE_NAME_SERIAL_NUMBER = properties
			.getProperty("havis.net.aim.variable.name.serialNumber", INSTANCE_NAME + ".SerialNumber");
	// runtime parameters
	public static final String VARIABLE_NAME_CODE_TYPES = properties.getProperty("havis.net.aim.variable.name.codeTypes", INSTANCE_NAME
			+ ".RuntimeParameters.CodeTypes");
	public static final String VARIABLE_NAME_CODE_TYPES_ENUM_STRINGS = properties.getProperty("havis.net.aim.variable.name.codeTypes.enumStrings",
			INSTANCE_NAME + ".RuntimeParameters.CodeTypes.EnumStrings");
	public static final String VARIABLE_NAME_TAG_TYPES = properties.getProperty("havis.net.aim.variable.name.tagTypes", INSTANCE_NAME
			+ ".RuntimeParameters.TagTypes");
	public static final String VARIABLE_NAME_TAG_TYPES_ENUM_STRINGS = properties.getProperty("havis.net.aim.variable.name.tagTypes.enumStrings", INSTANCE_NAME
			+ ".RuntimeParameters.TagTypes.EnumStrings");
	public static final String VARIABLE_NAME_RF_POWER = properties.getProperty("havis.net.aim.variable.name.rfPower", INSTANCE_NAME
			+ ".RuntimeParameters.RfPower");
	public static final String VARIABLE_NAME_MIN_RSSI = properties.getProperty("havis.net.aim.variable.name.minRssi", INSTANCE_NAME
			+ ".RuntimeParameters.MinRssi");
	// IOData
	public static final String VARIABLE_NAME_HS1 = properties.getProperty("havis.net.aim.variable.name.hs1", INSTANCE_NAME + ".IOData.HS1");
	public static final String VARIABLE_NAME_HS1_DIRECTION = properties.getProperty("havis.net.aim.variable.name.hs1.direction", INSTANCE_NAME
			+ ".IOData.HS1.direction");
	public static final String VARIABLE_NAME_HS2 = properties.getProperty("havis.net.aim.variable.name.hs2", INSTANCE_NAME + ".IOData.HS2");
	public static final String VARIABLE_NAME_HS2_DIRECTION = properties.getProperty("havis.net.aim.variable.name.hs2.direction", INSTANCE_NAME
			+ ".IOData.HS2.direction");
	public static final String VARIABLE_NAME_HS3 = properties.getProperty("havis.net.aim.variable.name.hs3", INSTANCE_NAME + ".IOData.HS3");
	public static final String VARIABLE_NAME_HS3_DIRECTION = properties.getProperty("havis.net.aim.variable.name.hs3.direction", INSTANCE_NAME
			+ ".IOData.HS3.direction");
	public static final String VARIABLE_NAME_HS4 = properties.getProperty("havis.net.aim.variable.name.hs4", INSTANCE_NAME + ".IOData.HS4");
	public static final String VARIABLE_NAME_HS4_DIRECTION = properties.getProperty("havis.net.aim.variable.name.hs4.direction", INSTANCE_NAME
			+ ".IOData.HS4.direction");
	public static final String VARIABLE_NAME_SWS1_SWD1 = properties.getProperty("havis.net.aim.variable.name.sws1Swd1", INSTANCE_NAME + ".IOData.SWS1_SWD1");
	public static final String VARIABLE_NAME_SWS1_SWD1_DIRECTION = properties.getProperty("havis.net.aim.variable.name.sws1Swd1.direction", INSTANCE_NAME
			+ ".IOData.SWS1_SWD1.direction");
	public static final String VARIABLE_NAME_SWS2_SWD2 = properties.getProperty("havis.net.aim.variable.name.sws2Swd2", INSTANCE_NAME + ".IOData.SWS2_SWD2");
	public static final String VARIABLE_NAME_SWS2_SWD2_DIRECTION = properties.getProperty("havis.net.aim.variable.name.sws2Swd2.direction", INSTANCE_NAME
			+ ".IOData.SWS2_SWD2.direction");
	public static final String VARIABLE_NAME_LS1 = properties.getProperty("havis.net.aim.variable.name.ls1", INSTANCE_NAME + ".IOData.LS1");
	public static final String VARIABLE_NAME_LS1_DIRECTION = properties.getProperty("havis.net.aim.variable.name.ls1.direction", INSTANCE_NAME
			+ ".IOData.LS1.direction");
	public static final String VARIABLE_NAME_LS2 = properties.getProperty("havis.net.aim.variable.name.ls2", INSTANCE_NAME + ".IOData.LS2");
	public static final String VARIABLE_NAME_LS2_DIRECTION = properties.getProperty("havis.net.aim.variable.name.ls2.direction", INSTANCE_NAME
			+ ".IOData.LS2.direction");

	public static final int VARIABLE_VAL_ANTENNA_ID = Integer.valueOf(properties.getProperty("havis.net.aim.variable.value.antennaNames.id", "0"));
	public static final String VARIABLE_VAL_ANTENNA_NAME = properties.getProperty("havis.net.aim.variable.value.antennaNames.name", "All");
	public static final String VARIABLE_VAL_AUTOID_MODEL_VERSION = properties.getProperty("havis.net.aim.variable.value.autoIdModelVersion", "1.00");
	public static final String VARIABLE_VAL_DEVICE_NAME = properties.getProperty("havis.net.aim.variable.value.deviceName", getMicaNameOrDefault());
	public static final String VARIABLE_VAL_DEVICE_INFO = properties.getProperty("havis.net.aim.variable.value.deviceInfo", "");
	public static final String VARIABLE_VAL_MANUFACTURER = properties.getProperty("havis.net.aim.variable.value.manufacturer",
			"HARTING IT Software Development GmbH & Co. KG");
	public static final String VARIABLE_VAL_MODEL = properties.getProperty("havis.net.aim.variable.value.model", "Ha-VIS RF-R310");
	public static final String VARIABLE_VAL_DEVICE_MANUAL = properties.getProperty("havis.net.aim.variable.value.deviceManual", "http://www.harting-rfid.com");
	public static final String VARIABLE_VAL_DEVICE_REVISION = properties.getProperty("havis.net.aim.variable.value.deviceRevision", "0");
	public static final String VARIABLE_VAL_HARDWARE_REVISION = properties.getProperty("havis.net.aim.variable.value.hardwareRevision", "1.0");
	public static final String VARIABLE_VAL_SOFTWARE_REVISION = properties.getProperty("havis.net.aim.variable.value.softwareRevision", "1.0.0");
	public static final int VARIABLE_VAL_REVISION_COUNTER = Integer.valueOf(properties.getProperty("havis.net.aim.variable.value.revisionCounter", "0"));
	public static final String VARIABLE_VAL_SERIAL_NUMBER = properties.getProperty("havis.net.aim.variable.value.serialNumber", "00000000000");
	public static final DeviceStatusEnumeration VARIABLE_VAL_DEVICE_STATUS = DeviceStatusEnumeration.valueOf(properties.getProperty(
			"havis.net.aim.variable.value.deviceStatus", "IDLE"));
	// runtime parameters
	public static final Long[] VARIABLE_VAL_CODE_TYPES;
	public static final CodeTypeEnumeration[] VARIABLE_VAL_CODE_TYPES_ENUM_STRINGS;
	public static final Long[] VARIABLE_VAL_TAG_TYPES;
	public static final TagTypeEnumeration[] VARIABLE_VAL_TAG_TYPES_ENUM_STRINGS;

	public static final String METHOD_NAME_SCAN = properties.getProperty("havis.net.aim.method.name.scan", INSTANCE_NAME + ".Scan");
	public static final String METHOD_NAME_SCAN_START = properties.getProperty("havis.net.aim.method.name.scanStop", INSTANCE_NAME + ".ScanStart");
	public static final String METHOD_NAME_SCAN_STOP = properties.getProperty("havis.net.aim.method.name.scanStop", INSTANCE_NAME + ".ScanStop");
	public static final String METHOD_NAME_READ = properties.getProperty("havis.net.aim.method.name.readTag", INSTANCE_NAME + ".ReadTag");
	public static final String METHOD_NAME_WRITE = properties.getProperty("havis.net.aim.method.name.writeTag", INSTANCE_NAME + ".WriteTag");
	public static final String METHOD_NAME_LOCK = properties.getProperty("havis.net.aim.method.name.lockTag", INSTANCE_NAME + ".LockTag");
	public static final String METHOD_NAME_KILL = properties.getProperty("havis.net.aim.method.name.killTag", INSTANCE_NAME + ".KillTag");
	public static final String METHOD_NAME_SET_PASSWORD = properties.getProperty("havis.net.aim.method.name.setTagPassword", INSTANCE_NAME + ".SetTagPassword");

	public static final String EVENT_RFID_SCAN_EVENT_TYPE = properties.getProperty("havis.net.aim.event.rfidScanEvent.type", "#1006");
	public static final String EVENT_RFID_SCAN_EVENT_PARAM_DEVICE_NAME = properties.getProperty("havis.net.aim.event.rfidScanEvent.param.deviceName", "#6049");
	public static final String EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT = properties.getProperty("havis.net.aim.event.rfidScanEvent.param.scanResult", "#6042");
	public static final int EVENT_RFID_SCAN_EVENT_SEVERITY = Integer.valueOf(properties.getProperty("havis.net.aim.event.rfidScanEvent.severity", "500"));

	public static final int MAXIMUM_SIGHTINGS = Integer.valueOf(properties.getProperty("havis.net.aim.maxSightings", "1000"));
	public static final int EVENT_INTERVAL = Integer.valueOf(properties.getProperty("havis.net.aim.event.interval", "300"));

	public static final short TAG_SET_CURRENT = (short) Integer.valueOf(properties.getProperty("havis.net.aim.tagSet.current", "0")).intValue();
	public static final short TAG_SET_ADDITIONS = (short) Integer.valueOf(properties.getProperty("havis.net.aim.tagSet.additions", "1")).intValue();
	public static final short TAG_SET_DELETIONS = (short) Integer.valueOf(properties.getProperty("havis.net.aim.tagSet.deletions", "2")).intValue();

	public static final String CONFIG_FILE = properties.getProperty("havis.net.aim.configFile", "conf/havis/net/aim/config.json");

	public static final String SERVER_CONFIG_FILE = properties.getProperty("havis.net.aim.server.configFile", "/opt/havis.opc-ua/conf/ServerConfig.xml");
	public static final String SERVER_CERTIFICATES_ROOT = properties.getProperty("havis.net.aim.server.certificatesRoot", "/opt/havis.opc-ua/pkiserver/");
	public static final String SERVER_SCRIPT = properties.getProperty("havis.net.aim.server.script", "/etc/init.d/opc-ua.sh");
	public static final String SERVER_LOG = properties.getProperty("havis.net.aim.server.log", "/var/log/opc-ua/uaserver.log");

	static {
		// code types
		int[] intIndices = parseIntList(properties.getProperty("havis.net.aim.variable.value.codeTypes", "0"));
		Long[] longIndices = new Long[intIndices.length];
		for (int i = 0; i < intIndices.length; i++) {
			longIndices[i] = Long.valueOf(intIndices[i]);
		}
		VARIABLE_VAL_CODE_TYPES = longIndices;

		// code type enum strings
		String[] values = parseList(properties.getProperty("havis.net.aim.variable.value.codeTypes.enumStrings", "EPC"));
		VARIABLE_VAL_CODE_TYPES_ENUM_STRINGS = new CodeTypeEnumeration[values.length];
		for (int i = 0; i < values.length; i++) {
			VARIABLE_VAL_CODE_TYPES_ENUM_STRINGS[i] = CodeTypeEnumeration.fromValue(values[i]);
		}

		// tag types
		intIndices = parseIntList(properties.getProperty("havis.net.aim.variable.value.tagTypes", "0"));
		longIndices = new Long[intIndices.length];
		for (int i = 0; i < intIndices.length; i++) {
			longIndices[i] = Long.valueOf(intIndices[i]);
		}
		VARIABLE_VAL_TAG_TYPES = longIndices;

		// tag type enum strings
		values = parseList(properties.getProperty("havis.net.aim.variable.value.tagTypes.enumStrings", "EPC Class1 Gen2 V1"));
		VARIABLE_VAL_TAG_TYPES_ENUM_STRINGS = new TagTypeEnumeration[values.length];
		for (int i = 0; i < values.length; i++) {
			VARIABLE_VAL_TAG_TYPES_ENUM_STRINGS[i] = TagTypeEnumeration.fromValue(values[i]);
		}
	}

	private static String[] parseList(String list) {
		String[] ret = list.split(",");
		for (int i = 0; i < ret.length; i++) {
			ret[i] = ret[i].trim();
		}
		return ret;
	}

	private static int[] parseIntList(String list) {
		String[] l = parseList(list);
		int[] ret = new int[l.length];
		for (int i = 0; i < l.length; i++) {
			ret[i] = Integer.parseInt(l[i]);
		}
		return ret;
	}

	private static String getMicaNameOrDefault() {
		String micaName = "mica-00";
		String micaNameProperty = "mica.device.name";

		String propertyValue = System.getProperty(micaNameProperty);
		if (propertyValue != null) {
			propertyValue = propertyValue.trim();
		}
		if (propertyValue == null || propertyValue.isEmpty()) {
			propertyValue = System.getenv(micaNameProperty);
			if (propertyValue != null) {
				propertyValue = propertyValue.trim();
			}
		}

		if (propertyValue != null && !propertyValue.isEmpty())
			micaName = propertyValue;

		return micaName;
	}
}
