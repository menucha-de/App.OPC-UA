package havis.net.aim.opcua;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import havis.net.aim.device.rf.RfidReaderDevice;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.ReadResultPair;
import havis.net.aim.xsd.RfidScanResultPair;
import havis.opcua.message.DataProvider;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.exception.ApplicationException;
import havis.opcua.message.exception.InvalidParameterException;
import havis.opcua.message.exception.NoSuchParameterException;
import havis.opcua.message.exception.ParameterException;

/**
 * This class provides an implementation of the {@link DataProvider} interface
 * with RFID backend.
 * 
 * @author Christian Brink
 *
 */
public class AimDataProvider implements DataProvider {
	private static final Logger log = Logger.getLogger(AimDataProvider.class.getName());
	private RfidReaderDevice rfidReaderDevice;
	private MessageHandler messageHandler;
	private SubscriptionManager subscriptionManager;

	public AimDataProvider(RfidReaderDevice rfidReaderDevice, MessageHandler messageHandler) {
		this.rfidReaderDevice = rfidReaderDevice;
		this.messageHandler = messageHandler;
		this.subscriptionManager = new SubscriptionManager(messageHandler);
		this.rfidReaderDevice.addParamChangedListener(subscriptionManager);
		this.rfidReaderDevice.addRfidEventListener(subscriptionManager);
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public RfidReaderDevice getRfidDevice() {
		return rfidReaderDevice;
	}

	public SubscriptionManager getSubscriptionManager() {
		return subscriptionManager;
	}

	@Override
	public Object[] call(String methodId, String paramId, Object[] args) throws ParameterException {
		Constants.Method method = Constants.Method.forName(methodId);
		if (method == null) {
			log.log(Level.FINE, "Failed to call method {0}. No such method exists.", method);
			throw new NoSuchParameterException("The method " + methodId + " does not exist.");
		}

		log.log(Level.FINE, "Calling method {0}.", method);
		try {
			switch (method) {
			case SCAN:
				RfidScanResultPair scp = this.rfidReaderDevice
						.scan(Deserializer.deserializeScanSettings(args[0]));
				return Serializer.serialize(scp);

			case SCAN_START:
				AutoIdOperationStatusEnumeration startScanStatus = this.rfidReaderDevice
						.scanStart(Deserializer.deserializeScanSettings(args[0]));
				return Serializer.serialize(startScanStatus);

			case SCAN_STOP:
				this.rfidReaderDevice.scanStop();
				return new Object[] {};

			case READ_TAG:
				ReadResultPair rrp = this.rfidReaderDevice.readTag(
						/* identifier */ Deserializer.deserializeScanData(args[0]),
						CodeTypeEnumeration.valueOf((String) args[1]), /* region */ (int) args[2],
						/* offset */ (long) args[3], /* length */ (long) args[4],
						/* password */ Deserializer.unboxByteArray((Byte[]) args[5]));
				return Serializer.serialize(rrp);

			case WRITE_TAG:
				AutoIdOperationStatusEnumeration writeStatus = rfidReaderDevice.writeTag(
						/* identifier */ Deserializer.deserializeScanData(args[0]),
						CodeTypeEnumeration.valueOf((String) args[1]), /* region */ (int) args[2],
						/* offset */ (long) args[3],
						/* data */ Deserializer.unboxByteArray((Byte[]) args[4]),
						/* password */ Deserializer.unboxByteArray((Byte[]) args[5]));
				return Serializer.serialize(writeStatus);

			case LOCK_TAG:
				AutoIdOperationStatusEnumeration lockStatus = rfidReaderDevice.lockTag(
						/* identifier */ Deserializer.deserializeScanData(args[0]),
						CodeTypeEnumeration.valueOf((String) args[1]),
						/* password */ Deserializer.unboxByteArray((Byte[]) args[2]),
						/* lockRegion */ Deserializer.deserialzeLockRegion((int) args[3]),
						/* lockOperation */ Deserializer.deserialzeLockType((int) args[4]),
						/* offset */ (long) args[5], /* length */ (long) args[6]);
				return Serializer.serialize(lockStatus);

			case KILL_TAG:
				AutoIdOperationStatusEnumeration killStatus = rfidReaderDevice.killTag(
						/* identifier */ Deserializer.deserializeScanData(args[0]),
						CodeTypeEnumeration.valueOf((String) args[1]),
						/* password */ Deserializer.unboxByteArray((Byte[]) args[2]));
				return Serializer.serialize(killStatus);

			case SET_TAG_PASSWORD:
				AutoIdOperationStatusEnumeration pswStatus = rfidReaderDevice.setTagPassword(
						/* identifier */ Deserializer.deserializeScanData(args[0]),
						CodeTypeEnumeration.valueOf((String) args[1]),
						/* passwordType */ Deserializer.deserialzePasswordType((int) args[2]),
						/* accessPassword */ Deserializer.unboxByteArray((Byte[]) args[3]),
						/* newPassword */ Deserializer.unboxByteArray((Byte[]) args[4]));
				return Serializer.serialize(pswStatus);

			default:
				log.log(Level.FINE,
						"Unexpectedly failed to call method {0}. No such method is implemented.",
						method);
				throw new NoSuchParameterException("The method " + methodId + " does not exist.");
			}
		} catch (NoSuchParameterException nspe) {
			throw nspe;
		}

		catch (ApplicationException aex) {
			LogRecord logRec = new LogRecord(Level.FINEST,
					"ApplicationException (in call) occurred: {0}");
			logRec.setThrown(aex);
			logRec.setParameters(new Object[] { aex });
			logRec.setLoggerName(log.getName());
			log.log(logRec);
			throw aex;
		}

		catch (Exception ex) {
			throw new InvalidParameterException(ex);
		}
	}

	@Override
	public Object read(String paramName) throws ParameterException {
		Constants.Param param = Constants.Param.forName(paramName);
		if (param == null) {
			log.log(Level.FINE, "Failed to read parameter {0}. No such parameter exists.",
					paramName);
			throw new NoSuchParameterException("The parameter " + paramName + " does not exist.");
		}
		log.log(Level.FINE, "Reading parameter {0}.", paramName);
		try {
			switch (param) {
			case ANTENNA_NAMES:
				return Serializer.serialize(rfidReaderDevice.getAntennaNames());

			case AUTOID_MODEL_VERSION:
				return rfidReaderDevice.getAutoIdModelVersion();

			case DEVICE_INFO:
				return rfidReaderDevice.getDeviceInfo();

			case DEVICE_MANUAL:
				return rfidReaderDevice.getDeviceManual();

			case DEVICE_NAME:
				return rfidReaderDevice.getDeviceName();

			case DEVICE_REVISION:
				return rfidReaderDevice.getDeviceRevision();

			case DEVICE_STATUS:
				return rfidReaderDevice.getDeviceStatus().ordinal();

			case HARDWARE_REVISION:
				return rfidReaderDevice.getHardwareRevision();

			case LAST_SCAN_DATA:
				return Serializer.serialize(rfidReaderDevice.getLastScanData());

			case MANUFACTURER:
				return rfidReaderDevice.getManufacturer();

			case MODEL:
				return rfidReaderDevice.getModel();

			case REVISION_COUNTER:
				return new Integer(rfidReaderDevice.getRevisionCounter());

			case SERIAL_NUMBER:
				return rfidReaderDevice.getSerialNumber();

			case SOFTWARE_REVISION:
				return rfidReaderDevice.getSoftwareRevision();

			// runtime parameters
			case CODE_TYPES:
				return Serializer.serialize(rfidReaderDevice.getCodeTypes());
			case CODE_TYPES_ENUM_STRINGS:
				return Serializer.serialize(Environment.VARIABLE_VAL_CODE_TYPES_ENUM_STRINGS);
			case TAG_TYPES:
				return Serializer.serialize(rfidReaderDevice.getTagTypes());
			case TAG_TYPES_ENUM_STRINGS:
				return Serializer.serialize(Environment.VARIABLE_VAL_TAG_TYPES_ENUM_STRINGS);
			case RF_POWER:
				return rfidReaderDevice.getRfPower();
			case MIN_RSSI:
				return rfidReaderDevice.getMinRssi();

			// IOData
			case HS1:
				return rfidReaderDevice.getHs1().value();
			case HS1_DIRECTION:
				return rfidReaderDevice.getHs1Direction().value();
			case HS2:
				return rfidReaderDevice.getHs2().value();
			case HS2_DIRECTION:
				return rfidReaderDevice.getHs2Direction().value();
			case HS3:
				return rfidReaderDevice.getHs3().value();
			case HS3_DIRECTION:
				return rfidReaderDevice.getHs3Direction().value();
			case HS4:
				return rfidReaderDevice.getHs4().value();
			case HS4_DIRECTION:
				return rfidReaderDevice.getHs4Direction().value();
			case SWS1_SWD1:
				return rfidReaderDevice.getSws1Swd1().value();
			case SWS1_SWD1_DIRECTION:
				return rfidReaderDevice.getSws1Swd1Direction().value();
			case SWS2_SWD2:
				return rfidReaderDevice.getSws2Swd2().value();
			case SWS2_SWD2_DIRECTION:
				return rfidReaderDevice.getSws2Swd2Direction().value();
			case LS1:
				return rfidReaderDevice.getLs1().value();
			case LS1_DIRECTION:
				return rfidReaderDevice.getLs1Direction().value();
			case LS2:
				return rfidReaderDevice.getLs2().value();
			case LS2_DIRECTION:
				return rfidReaderDevice.getLs2Direction().value();

			default:
				log.log(Level.FINE,
						"Unexpectedly failed to read parameter {0}. No such parameter is implemented.",
						paramName);
				throw new NoSuchParameterException(
						"The parameter " + paramName + " does not exist.");
			}
		} catch (NoSuchParameterException nspe) {
			throw nspe;
		} catch (Exception ex) {
			throw new InvalidParameterException(ex);
		}
	}

	@Override
	public void subscribe(String name) throws ParameterException {
		Constants.Param param = Constants.Param.forName(name);
		Constants.EventType event = Constants.EventType.forName(name);

		if (param == null && event == null) {
			log.log(Level.FINE,
					"Failed to subscribe parameter or event {0}. No such object exists.", name);
			throw new NoSuchParameterException(
					"The parameter or event " + name + " does not exist.");
		}

		try {

			if (param != null)
				this.subscriptionManager.addSubscription(param, read(name));
			else
				this.subscriptionManager.addSubscription(event);

		} catch (Exception ex) {
			throw new InvalidParameterException(ex);
		}
	}

	@Override
	public void unsubscribe(String name) throws ParameterException {
		if ("*".equals(name)) {
			this.subscriptionManager.removeAllSubscriptions();
			return;
		}

		Constants.Param param = Constants.Param.forName(name);
		Constants.EventType event = Constants.EventType.forName(name);

		if (param == null && event == null) {
			log.log(Level.FINE,
					"Failed to unsubscribe parameter or event {0}. No such object exists.", name);
			throw new NoSuchParameterException(
					"The parameter or event " + name + " does not exist.");
		}

		try {
			if (param != null)
				this.subscriptionManager.removeSubscription(param);
			else
				this.subscriptionManager.removeSubscription(event);

		} catch (Exception ex) {
			throw new InvalidParameterException(ex);
		}
	}

	@Override
	public void write(String paramName, Object arg) throws ParameterException {
		Constants.Param param = Constants.Param.forName(paramName);

		if (param == null) {
			log.log(Level.FINE, "Failed to write parameter {0}. No such parameter exists.",
					paramName);
			throw new NoSuchParameterException("The parameter " + paramName + " does not exist.");
		}

		log.log(Level.FINE, "Writing value {0} to parameter {1}.", new Object[] { arg, paramName });
		try {
			switch (param) {
			case ANTENNA_NAMES:
				rfidReaderDevice.setAntennaNames(Deserializer.deserializeAntennaNameList(arg));
				break;

			case AUTOID_MODEL_VERSION:
				rfidReaderDevice.setAutoIdModelVersion((String) arg);
				break;

			case DEVICE_INFO:
				rfidReaderDevice.setDeviceInfo((String) arg);
				break;

			case DEVICE_NAME:
				rfidReaderDevice.setDeviceName((String) arg);
				break;
				
			case LAST_SCAN_DATA:
				rfidReaderDevice.setLastScanData(Deserializer.deserializeScanData(arg));
				break;

			// runtime parameters
			case CODE_TYPES:
				rfidReaderDevice.setCodeTypes(Deserializer.deserializeCodeTypes((Long[]) arg));
				break;
			case TAG_TYPES:
				rfidReaderDevice.setTagTypes(Deserializer.deserializeTagTypes((Long[]) arg));
				break;
			case RF_POWER:
				rfidReaderDevice.setRfPower((byte) arg);
				break;
			case MIN_RSSI:
				rfidReaderDevice.setMinRssi((int) arg);
				break;

			// IOData
			case HS1:
				rfidReaderDevice.setHs1(Deserializer.deserializeHaIOState((int) arg));
				break;
			case HS1_DIRECTION:
				rfidReaderDevice.setHs1Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case HS2:
				rfidReaderDevice.setHs2(Deserializer.deserializeHaIOState((int) arg));
				break;
			case HS2_DIRECTION:
				rfidReaderDevice.setHs2Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case HS3:
				rfidReaderDevice.setHs3(Deserializer.deserializeHaIOState((int) arg));
				break;
			case HS3_DIRECTION:
				rfidReaderDevice.setHs3Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case HS4:
				rfidReaderDevice.setHs4(Deserializer.deserializeHaIOState((int) arg));
				break;
			case HS4_DIRECTION:
				rfidReaderDevice.setHs4Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case SWS1_SWD1:
				rfidReaderDevice.setSws1Swd1(Deserializer.deserializeHaIOState((int) arg));
				break;
			case SWS1_SWD1_DIRECTION:
				rfidReaderDevice
						.setSws1Swd1Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case SWS2_SWD2:
				rfidReaderDevice.setSws2Swd2(Deserializer.deserializeHaIOState((int) arg));
				break;
			case SWS2_SWD2_DIRECTION:
				rfidReaderDevice
						.setSws2Swd2Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case LS1:
				rfidReaderDevice.setLs1(Deserializer.deserializeHaIOState((int) arg));
				break;
			case LS1_DIRECTION:
				rfidReaderDevice.setLs1Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;
			case LS2:
				rfidReaderDevice.setLs2(Deserializer.deserializeHaIOState((int) arg));
				break;
			case LS2_DIRECTION:
				rfidReaderDevice.setLs2Direction(Deserializer.deserializeHaIODirection((int) arg));
				break;

			case DEVICE_MANUAL:
			case DEVICE_REVISION:
			case DEVICE_STATUS:
			case HARDWARE_REVISION:
			case MANUFACTURER:
			case MODEL:
			case REVISION_COUNTER:
			case SERIAL_NUMBER:
			case SOFTWARE_REVISION:
			case CODE_TYPES_ENUM_STRINGS:
			case TAG_TYPES_ENUM_STRINGS:
				log.log(Level.FINE, "Failed to write parameter {0}. The parameter is not writable.",
						paramName);
				throw new InvalidParameterException(
						"The parameter " + paramName + " is not writable.");

			default:
				log.log(Level.FINE,
						"Unexpectedly failed to write parameter {0}. No such parameter is implemented.",
						paramName);
				throw new NoSuchParameterException(
						"The parameter " + paramName + " does not exist.");
			}
		} catch (Exception ex) {
			throw new InvalidParameterException(ex);
		}
	}
}
