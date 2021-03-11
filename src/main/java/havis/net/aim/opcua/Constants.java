package havis.net.aim.opcua;

import havis.net.aim.opcua.Environment;

public abstract class Constants {

	public enum Param {
		ANTENNA_NAMES(Environment.VARIABLE_NAME_ANTENNA_NAMES), //
		AUTOID_MODEL_VERSION(Environment.VARIABLE_NAME_AUTOID_MODEL_VERSION), //
		DEVICE_NAME(Environment.VARIABLE_NAME_DEVICE_NAME), //
		DEVICE_STATUS(Environment.VARIABLE_NAME_DEVICE_STATUS), //
		DEVICE_INFO(Environment.VARIABLE_NAME_DEVICE_INFO), //
		MANUFACTURER(Environment.VARIABLE_NAME_MANUFACTURER), //
		MODEL(Environment.VARIABLE_NAME_MODEL), //
		SOFTWARE_REVISION(Environment.VARIABLE_NAME_SOFTWARE_REVISION), //
		LAST_SCAN_DATA(Environment.VARIABLE_NAME_LAST_SCAN_DATA), //
		DEVICE_MANUAL(Environment.VARIABLE_NAME_DEVICE_MANUAL), //
		DEVICE_REVISION(Environment.VARIABLE_NAME_DEVICE_REVISION), //
		HARDWARE_REVISION(Environment.VARIABLE_NAME_HARDWARE_REVISION), //
		REVISION_COUNTER(Environment.VARIABLE_NAME_REVISION_COUNTER), //
		SERIAL_NUMBER(Environment.VARIABLE_NAME_SERIAL_NUMBER), //
		// runtime parameters
		CODE_TYPES(Environment.VARIABLE_NAME_CODE_TYPES), //
		CODE_TYPES_ENUM_STRINGS(Environment.VARIABLE_NAME_CODE_TYPES_ENUM_STRINGS), //
		TAG_TYPES(Environment.VARIABLE_NAME_TAG_TYPES), //
		TAG_TYPES_ENUM_STRINGS(Environment.VARIABLE_NAME_TAG_TYPES_ENUM_STRINGS), //
		RF_POWER(Environment.VARIABLE_NAME_RF_POWER), //
		MIN_RSSI(Environment.VARIABLE_NAME_MIN_RSSI), //
		// IOData
		HS1(Environment.VARIABLE_NAME_HS1), //
		HS1_DIRECTION(Environment.VARIABLE_NAME_HS1_DIRECTION), //
		HS2(Environment.VARIABLE_NAME_HS2), //
		HS2_DIRECTION(Environment.VARIABLE_NAME_HS2_DIRECTION), //
		HS3(Environment.VARIABLE_NAME_HS3), //
		HS3_DIRECTION(Environment.VARIABLE_NAME_HS3_DIRECTION), //
		HS4(Environment.VARIABLE_NAME_HS4), //
		HS4_DIRECTION(Environment.VARIABLE_NAME_HS4_DIRECTION), //
		LS1(Environment.VARIABLE_NAME_LS1), //
		LS1_DIRECTION(Environment.VARIABLE_NAME_LS1_DIRECTION), //
		LS2(Environment.VARIABLE_NAME_LS2), //
		LS2_DIRECTION(Environment.VARIABLE_NAME_LS2_DIRECTION), //
		SWS1_SWD1(Environment.VARIABLE_NAME_SWS1_SWD1), //
		SWS1_SWD1_DIRECTION(Environment.VARIABLE_NAME_SWS1_SWD1_DIRECTION), //
		SWS2_SWD2(Environment.VARIABLE_NAME_SWS2_SWD2), //
		SWS2_SWD2_DIRECTION(Environment.VARIABLE_NAME_SWS2_SWD2_DIRECTION);

		final String name;

		private Param(String name) {
			this.name = name;
		}

		public static Param forName(String name) {
			for (Param value : values()) {
				if (name.equals(value.name)) {
					return value;
				}
			}
			return null;
		}
	}

	public enum EventType {
		RFID_SCAN_EVENT(Environment.EVENT_RFID_SCAN_EVENT_TYPE);

		final String name;

		private EventType(String name) {
			this.name = name;
		}

		public static EventType forName(String name) {
			for (EventType value : values()) {
				if (name.equals(value.name)) {
					return value;
				}
			}
			return null;
		}
	}

	public enum Method {
		SCAN(Environment.METHOD_NAME_SCAN), //
		SCAN_START(Environment.METHOD_NAME_SCAN_START), //
		SCAN_STOP(Environment.METHOD_NAME_SCAN_STOP), //
		READ_TAG(Environment.METHOD_NAME_READ), //
		WRITE_TAG(Environment.METHOD_NAME_WRITE), //
		LOCK_TAG(Environment.METHOD_NAME_LOCK), //
		KILL_TAG(Environment.METHOD_NAME_KILL), //
		SET_TAG_PASSWORD(Environment.METHOD_NAME_SET_PASSWORD);

		final String name;

		private Method(String name) {
			this.name = name;
		}

		public static Method forName(String name) {
			for (Method value : values()) {
				if (name.equals(value.name)) {
					return value;
				}
			}
			return null;
		}
	}
}
