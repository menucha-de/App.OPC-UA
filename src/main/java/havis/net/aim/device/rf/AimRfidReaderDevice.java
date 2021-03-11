package havis.net.aim.device.rf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import havis.device.rf.capabilities.TransmitPowerTableEntry;
import havis.device.rf.common.util.RFUtils;
import havis.device.rf.configuration.AntennaConfiguration;
import havis.device.rf.configuration.Configuration;
import havis.device.rf.configuration.ConfigurationType;
import havis.device.rf.configuration.ConnectType;
import havis.device.rf.configuration.InventorySettings;
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
import havis.device.rf.tag.result.OperationResult;
import havis.device.rf.tag.result.ReadResult;
import havis.device.rf.tag.result.WriteResult;
import havis.net.aim.opcua.ConfigurationManager;
import havis.net.aim.opcua.Constants.EventType;
import havis.net.aim.opcua.Constants.Param;
import havis.net.aim.opcua.Deserializer;
import havis.net.aim.opcua.DeserializerException;
import havis.net.aim.opcua.Environment;
import havis.net.aim.opcua.LastScanDataChangeListener;
import havis.net.aim.opcua.RfidDeviceEventListener;
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
import havis.net.aim.xsd.ScanSettings;
import havis.opcua.message.exception.ApplicationException;

public class AimRfidReaderDevice extends RfidReaderDevice
		implements RFConsumer, LastScanDataChangeListener, RfidDeviceEventListener {

	private static final Logger log = Logger.getLogger(AimRfidReaderDevice.class.getName());
	private final RFDeviceConnectionManager rfDeviceConnectionManager;
	private final IODeviceConnectionManager ioDeviceConnectionManager;
	private ConfigurationManager configurationManager;
	private boolean initialized;
	private final Lock scanLock = new ReentrantLock();
	private final Condition scanAsyncStopped = scanLock.newCondition();
	private InventoryThread invThread;
	private Integer scanAsyncRfConnection;

	private interface Operation<TDevice, TResult> {
		String getName();

		TResult operation(TDevice device) throws Exception;

		TResult run() throws ApplicationException;
	}

	private abstract class RFOperation<TResult> implements Operation<RFDevice, TResult> {

		private String operationName;
		private RFDeviceConnectionManager connectionManager;

		RFOperation(String operationName, RFDeviceConnectionManager connectionManager) {
			this.operationName = operationName;
			this.connectionManager = connectionManager;
		}

		@Override
		public String getName() {
			return operationName;
		}

		public TResult run() throws ApplicationException {
			try {
				scanLock.lock();
				setDeviceStatus(DeviceStatusEnumeration.BUSY);
				Integer connection = connectionManager.acquire();
				TResult value;
				try {
					// if connection is not yet initialized
					if (invThread == null && !initialized) {
						initialize(connectionManager.getRfDevice());
					}
					// execute operation
					value = operation(connectionManager.getRfDevice());
				} finally {
					try {
						connectionManager.release(connection);
					} catch (Exception e) {
						log.log(Level.SEVERE, "Cannot close connection to RF device", e);
						throw ApplicationException.invalidState();
					}
				}
				setDeviceStatus(DeviceStatusEnumeration.IDLE);
				return value;
			} catch (ApplicationException e) {
				setDeviceStatus(DeviceStatusEnumeration.ERROR);
				throw e;
			} catch (Exception e) {
				log.log(Level.SEVERE, "Cannot execute RF operation: " + getName(), e);
				setDeviceStatus(DeviceStatusEnumeration.ERROR);
				throw ApplicationException.invalidState();
			} finally {
				scanLock.unlock();
			}
		}
	}

	private abstract class IOOperation<TResult> implements Operation<IODevice, TResult> {

		private String operationName;
		private IODeviceConnectionManager connectionManager;

		IOOperation(String operationName, IODeviceConnectionManager connectionManager) {
			this.operationName = operationName;
			this.connectionManager = connectionManager;
		}

		public String getName() {
			return operationName;
		}

		public TResult run() throws ApplicationException {
			try {
				scanLock.lock();
				setDeviceStatus(DeviceStatusEnumeration.BUSY);
				// get opened connection
				Integer connection = ioDeviceConnectionManager.acquire();
				TResult value;
				try {
					// execute operation
					value = operation(connectionManager.getIODevice());
				} finally {
					// release connection
					try {
						connectionManager.release(connection);
					} catch (Exception e) {
						log.log(Level.SEVERE, "Cannot close connection to IO device", e);
						throw ApplicationException.invalidState();
					}
				}
				setDeviceStatus(DeviceStatusEnumeration.IDLE);
				return value;
			} catch (ApplicationException e) {
				setDeviceStatus(DeviceStatusEnumeration.ERROR);
				throw e;
			} catch (Exception e) {
				log.log(Level.SEVERE, "Cannot execute IO operation: " + getName(), e);
				setDeviceStatus(DeviceStatusEnumeration.ERROR);
				throw ApplicationException.invalidState();
			} finally {
				scanLock.unlock();
			}
		}
	}

	public AimRfidReaderDevice(RFDevice rfDevice, final IODevice ioDevice, ConfigurationManager configurationManager)
			throws DeserializerException {
		this.configurationManager = configurationManager;
		rfDeviceConnectionManager = new RFDeviceConnectionManager(rfDevice);
		ioDeviceConnectionManager = new IODeviceConnectionManager(ioDevice);

		AntennaNameIdPair ant0 = new AntennaNameIdPair();
		ant0.setAntennaId(Environment.VARIABLE_VAL_ANTENNA_ID);
		ant0.setAntennaName(Environment.VARIABLE_VAL_ANTENNA_NAME);
		this.getAntennaNames().add(ant0);

		this.setAutoIdModelVersion(Environment.VARIABLE_VAL_AUTOID_MODEL_VERSION);
		this.setDeviceInfo(Environment.VARIABLE_VAL_DEVICE_INFO);
		this.setDeviceManual(Environment.VARIABLE_VAL_DEVICE_MANUAL);
		this.setDeviceName(Environment.VARIABLE_VAL_DEVICE_NAME);
		this.setDeviceRevision(Environment.VARIABLE_VAL_DEVICE_REVISION);
		this.setDeviceStatus(Environment.VARIABLE_VAL_DEVICE_STATUS);
		this.setHardwareRevision(Environment.VARIABLE_VAL_HARDWARE_REVISION);
		this.setSoftwareRevision(Environment.VARIABLE_VAL_SOFTWARE_REVISION);
		this.setManufacturer(Environment.VARIABLE_VAL_MANUFACTURER);
		this.setModel(Environment.VARIABLE_VAL_MODEL);
		this.setRevisionCounter(Environment.VARIABLE_VAL_REVISION_COUNTER);
		this.setSerialNumber(Environment.VARIABLE_VAL_SERIAL_NUMBER);
		this.setCodeTypes(Deserializer.deserializeCodeTypes(Environment.VARIABLE_VAL_CODE_TYPES));
		this.setTagTypes(Deserializer.deserializeTagTypes(Environment.VARIABLE_VAL_TAG_TYPES));
	}

	private void initialize(RFDevice rfDevice) throws ParameterException, ImplementationException, ConnectionException {
		if (!Boolean.valueOf(System.getProperty("havis.net.aim.dev.mode", "false")))
			return;

		String region = System.getProperty("havis.net.aim.dev.region", "EU");
		TxLevel txLevel = TxLevel.valueOf(System.getProperty("havis.net.aim.dev.txLevel", "TxLevel27"));
		ConnectType conType = ConnectType.valueOf(System.getProperty("havis.net.aim.dev.connectType", "TRUE"));

		rfDevice.setRegion(region);
		List<havis.device.rf.configuration.Configuration> configs = rfDevice
				.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, (short) 0, (short) 0, (short) 0);

		for (havis.device.rf.configuration.Configuration cfg : configs) {
			AntennaConfiguration aCfg = (AntennaConfiguration) cfg;
			aCfg.setTransmitPower((short) txLevel.ordinal());
			aCfg.setConnect(conType);
		}

		rfDevice.setConfiguration(configs);
		this.initialized = true;
	}

	@Override
	public byte getRfPower() throws ApplicationException {
		RFOperation<Byte> op = new RFOperation<Byte>("get " + Param.RF_POWER, rfDeviceConnectionManager) {

			@Override
			public Byte operation(RFDevice rfDevice) throws Exception {
				// get transmit power table
				RegulatoryCapabilities regulatoryCaps = getRegulatoryCapabilities(rfDevice);
				Byte transmitPower = null;
				// for each antenna
				for (short antennaId : getAntennaIds()) {
					// get configurations
					List<Configuration> configs = rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION,
							antennaId, (short) 0, (short) 0);
					if (log.isLoggable(Level.INFO)) {
						log.log(Level.INFO, "Received " + ConfigurationType.ANTENNA_CONFIGURATION);
					}
					// for each configuration
					for (Configuration config : configs) {
						AntennaConfiguration antennaConf = (AntennaConfiguration) config;
						if (log.isLoggable(Level.FINE)) {
							log.log(Level.FINE, "  antennaId=" + antennaId + ", transmitPowerIndex="
									+ antennaConf.getTransmitPower());
						}
						if (antennaConf.getTransmitPower() == null) {
							// proceed with next configuration
							continue;
						}
						// for each transmit power entry
						for (TransmitPowerTableEntry entry : regulatoryCaps.getTransmitPowerTable().getEntryList()) {
							// if entry has the transmit power index
							if (entry.getIndex() == antennaConf.getTransmitPower()) {
								if (log.isLoggable(Level.FINE)) {
									log.log(Level.FINE, "  antennaId=" + antennaId + ", transmitPowerIndex="
											+ entry.getIndex() + ", transmitPower=" + entry.getTransmitPower());
								}
								if (transmitPower != null && transmitPower != entry.getTransmitPower()) {
									log.log(Level.SEVERE, "Cannot get RF power: Found several transmit powers");
									throw ApplicationException.invalidState();
								}
								transmitPower = (byte) entry.getTransmitPower();
							}
						}
					}
				}
				if (transmitPower == null) {
					log.log(Level.SEVERE, "Cannot get RF power");
					throw ApplicationException.invalidState();
				}
				return transmitPower;
			}
		};
		return op.run();
	}

	@Override
	public void setRfPower(final byte rfPower) throws ApplicationException {
		RFOperation<Byte> op = new RFOperation<Byte>("set " + Param.RF_POWER, rfDeviceConnectionManager) {

			@Override
			public Byte operation(RFDevice rfDevice) throws Exception {
				// get transmit power table
				RegulatoryCapabilities regulatoryCaps = getRegulatoryCapabilities(rfDevice);
				Short index = null;
				// for each transmit power entry
				for (TransmitPowerTableEntry entry : regulatoryCaps.getTransmitPowerTable().getEntryList()) {
					// if entry has the transmit power
					if (entry.getTransmitPower() == rfPower) {
						if (log.isLoggable(Level.FINE)) {
							log.log(Level.FINE, "  transmitPowerIndex=" + entry.getIndex() + ", transmitPower="
									+ entry.getTransmitPower());
						}
						index = entry.getIndex();
					}
				}
				if (index == null) {
					log.log(Level.SEVERE, "Cannot set RF power: Missing transmit power index");
					throw ApplicationException.invalidState();
				}
				List<Short> antennas = getAntennaIds();
				// for each antenna
				for (short antennaId : antennas) {
					// set transmit power index
					AntennaConfiguration conf = new AntennaConfiguration();
					conf.setId(antennaId);
					conf.setTransmitPower(index);
					if (log.isLoggable(Level.INFO)) {
						log.log(Level.INFO, "Sending " + ConfigurationType.ANTENNA_CONFIGURATION);
					}
					rfDevice.setConfiguration(Arrays.asList((Configuration) conf));
				}
				if (!antennas.isEmpty()) {
					// send change event
					notifyParamChange(Param.RF_POWER, rfPower);
				}
				return null;
			}
		};
		op.run();
	};

	@Override
	public int getMinRssi() throws ApplicationException {
		RFOperation<Integer> op = new RFOperation<Integer>("get " + Param.MIN_RSSI, rfDeviceConnectionManager) {

			@Override
			public Integer operation(RFDevice rfDevice) throws Exception {
				// get min. RSSI
				InventorySettings inventorySettings = getInventorySettings(rfDevice);
				int minRssi = inventorySettings.getRssiFilter().getMinRssi();
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "  minRssi=" + minRssi);
				}
				return minRssi;
			}
		};
		return op.run();
	}

	@Override
	public void setMinRssi(final int minRssi) throws ApplicationException {
		RFOperation<Object> op = new RFOperation<Object>("set " + Param.MIN_RSSI, rfDeviceConnectionManager) {

			@Override
			public Object operation(RFDevice rfDevice) throws Exception {
				// get current configuration
				InventorySettings inventorySettings = getInventorySettings(rfDevice);
				// set min. RSSI
				inventorySettings.getRssiFilter().setMinRssi((short) minRssi);
				// update configuration
				setInventorySettings(rfDevice, inventorySettings);
				// send change event
				notifyParamChange(Param.MIN_RSSI, minRssi);
				return null;
			}
		};
		op.run();
	};

	@Override
	public HaIOState getHs1() throws ApplicationException {
		return getIOState(Param.HS1, (short) 1 /* port */);
	}

	@Override
	public void setHs1(HaIOState state) throws ApplicationException {
		setIOState(Param.HS1, (short) 1 /* port */, state);
	}

	@Override
	public HaIODirection getHs1Direction() throws ApplicationException {
		return getIODirection(Param.HS1_DIRECTION, (short) 1 /* port */);
	}

	@Override
	public void setHs1Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.HS1_DIRECTION, (short) 1 /* port */, direction);
	}

	@Override
	public HaIOState getHs2() throws ApplicationException {
		return getIOState(Param.HS2, (short) 2 /* port */);
	}

	@Override
	public void setHs2(HaIOState state) throws ApplicationException {
		setIOState(Param.HS2, (short) 2 /* port */, state);
	}

	@Override
	public HaIODirection getHs2Direction() throws ApplicationException {
		return getIODirection(Param.HS2_DIRECTION, (short) 2 /* port */);
	}

	@Override
	public void setHs2Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.HS2_DIRECTION, (short) 2 /* port */, direction);
	}

	@Override
	public HaIOState getHs3() throws ApplicationException {
		return getIOState(Param.HS3, (short) 3 /* port */);
	}

	@Override
	public void setHs3(HaIOState state) throws ApplicationException {
		setIOState(Param.HS3, (short) 3 /* port */, state);
	}

	@Override
	public HaIODirection getHs3Direction() throws ApplicationException {
		return getIODirection(Param.HS3_DIRECTION, (short) 3 /* port */);
	}

	@Override
	public void setHs3Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.HS3_DIRECTION, (short) 3 /* port */, direction);
	}

	@Override
	public HaIOState getHs4() throws ApplicationException {
		return getIOState(Param.HS4, (short) 4 /* port */);
	}

	@Override
	public void setHs4(HaIOState state) throws ApplicationException {
		setIOState(Param.HS4, (short) 4 /* port */, state);
	}

	@Override
	public HaIODirection getHs4Direction() throws ApplicationException {
		return getIODirection(Param.HS4_DIRECTION, (short) 4 /* port */);
	}

	@Override
	public void setHs4Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.HS4_DIRECTION, (short) 4 /* port */, direction);
	}

	@Override
	public HaIOState getSws1Swd1() throws ApplicationException {
		return getIOState(Param.SWS1_SWD1, (short) 5 /* port */);
	}

	@Override
	public void setSws1Swd1(HaIOState state) throws ApplicationException {
		setIOState(Param.SWS1_SWD1, (short) 5 /* port */, state);
	}

	@Override
	public HaIODirection getSws1Swd1Direction() throws ApplicationException {
		return getIODirection(Param.SWS1_SWD1_DIRECTION, (short) 5 /* port */);
	}

	@Override
	public void setSws1Swd1Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.SWS1_SWD1_DIRECTION, (short) 5 /* port */, direction);
	}

	@Override
	public HaIOState getSws2Swd2() throws ApplicationException {
		return getIOState(Param.SWS2_SWD2, (short) 6 /* port */);
	}

	@Override
	public void setSws2Swd2(HaIOState state) throws ApplicationException {
		setIOState(Param.SWS2_SWD2, (short) 6 /* port */, state);
	}

	@Override
	public HaIODirection getSws2Swd2Direction() throws ApplicationException {
		return getIODirection(Param.SWS2_SWD2_DIRECTION, (short) 6 /* port */);
	}

	@Override
	public void setSws2Swd2Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.SWS2_SWD2_DIRECTION, (short) 6 /* port */, direction);
	}

	@Override
	public HaIOState getLs1() throws ApplicationException {
		return getIOState(Param.LS1, (short) 7 /* port */);
	}

	@Override
	public void setLs1(HaIOState state) throws ApplicationException {
		setIOState(Param.LS1, (short) 7 /* port */, state);
	}

	@Override
	public HaIODirection getLs1Direction() throws ApplicationException {
		return getIODirection(Param.LS1_DIRECTION, (short) 7 /* port */);
	}

	@Override
	public void setLs1Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.LS1_DIRECTION, (short) 7 /* port */, direction);
	}

	@Override
	public HaIOState getLs2() throws ApplicationException {
		return getIOState(Param.LS2, (short) 8 /* port */);
	}

	@Override
	public void setLs2(HaIOState state) throws ApplicationException {
		setIOState(Param.LS2, (short) 8 /* port */, state);
	}

	@Override
	public HaIODirection getLs2Direction() throws ApplicationException {
		return getIODirection(Param.LS2_DIRECTION, (short) 8 /* port */);
	}

	@Override
	public void setLs2Direction(HaIODirection direction) throws ApplicationException {
		setIODirection(Param.LS2_DIRECTION, (short) 8 /* port */, direction);
	}

	@Override
	public AutoIdOperationStatusEnumeration scanStart(ScanSettings settings) throws ApplicationException {
		try {
			scanLock.lock();

			if (invThread != null)
				throw ApplicationException.invalidState();

			RFDevice rfDevice = rfDeviceConnectionManager.getRfDevice();
			try {
				scanAsyncRfConnection = rfDeviceConnectionManager.acquire();
			} catch (Exception e) {
				log.log(Level.FINER, e.getMessage(), e);
				this.setDeviceStatus(DeviceStatusEnumeration.ERROR);
				return AutoIdOperationStatusEnumeration.DEVICE_NOT_READY;
			}

			try {
				if (!initialized)
					initialize(rfDevice);
			} catch (ParameterException | ImplementationException | ConnectionException e) {
				log.log(Level.FINER, e.getMessage(), e);
				return AutoIdOperationStatusEnumeration.DEVICE_NOT_READY;
			}

			this.setDeviceStatus(DeviceStatusEnumeration.SCANNING);
			invThread = new InventoryThread(rfDevice);
			invThread.setTagSet(configurationManager.getTagSet());

			try {
				invThread.startInventoryAsync(settings, getAntennaIds(), this, this);
				return AutoIdOperationStatusEnumeration.SUCCESS;

			} catch (Throwable error) {
				log.log(Level.FINER, error.getMessage(), error);
				return Mapper.mapScanError(error);
			}
		} finally {
			scanLock.unlock();
		}
	}

	@Override
	public void scanAsyncFinished(Object source) {
		try {
			this.scanLock.lock();
			rfDeviceConnectionManager.release(scanAsyncRfConnection);
			this.scanAsyncStopped.signal();
		} catch (Exception e) {
			log.log(Level.FINER, e.getMessage(), e);
		} finally {
			this.invThread = null;
			this.setDeviceStatus(DeviceStatusEnumeration.IDLE);
			this.scanLock.unlock();
		}
	}

	@Override
	public boolean scanStop() throws ApplicationException {
		try {
			scanLock.lock();
			if (invThread == null)
				throw ApplicationException.invalidState();

			this.invThread.stopInventoryAsync();
			return this.scanAsyncStopped.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.log(Level.FINER, e.getMessage(), e);
			return false;
		} finally {
			scanLock.unlock();
		}
	}

	@Override
	public RfidScanResultPair scan(ScanSettings settings) throws ApplicationException {
		RfidScanResultPair srp = null;

		try {
			scanLock.lock();

			if (invThread != null)
				throw ApplicationException.invalidState();

			if (!settings.isDataAvailable() && settings.getCycles() == 0 && settings.getDuration() == 0)
				throw ApplicationException.invalidArgument();

			srp = new RfidScanResultPair();

			RFDevice rfDevice = rfDeviceConnectionManager.getRfDevice();
			Integer connection;
			try {
				connection = rfDeviceConnectionManager.acquire();
			} catch (Exception e) {
				log.log(Level.FINER, e.getMessage(), e);
				srp.setStatus(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY);
				this.setDeviceStatus(DeviceStatusEnumeration.ERROR);
				return srp;
			}

			try {
				if (!initialized)
					initialize(rfDevice);
			} catch (ParameterException | ImplementationException | ConnectionException e) {
				log.log(Level.FINER, e.getMessage(), e);
				srp.setStatus(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY);
				throw e;
			}

			this.setDeviceStatus(DeviceStatusEnumeration.BUSY);
			invThread = new InventoryThread(rfDevice);
			invThread.setTagSet(configurationManager.getTagSet());

			try {
				Collection<RfidScanResult> scanResults = invThread.startInventory(settings, scanLock, getAntennaIds(),
						this);
				if (invThread.getError() != null)
					throw invThread.getError();
				if (scanResults == null || scanResults.isEmpty()) {
					srp.setStatus(AutoIdOperationStatusEnumeration.NO_IDENTIFIER);
				} else {
					srp.getResults().addAll(scanResults);
					srp.setStatus(AutoIdOperationStatusEnumeration.SUCCESS);
					// send scan events
					for (RfidScanResult scanResult : scanResults) {
						Map<String, Object> eventArgs = new HashMap<>();
						eventArgs.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT, scanResult);
						rfidEventOccured(this, EventType.RFID_SCAN_EVENT, new Date(), eventArgs);
					}
				}

			} catch (Throwable error) {
				log.log(Level.FINER, error.getMessage(), error);
				srp.setStatus(Mapper.mapScanError(error));
			}

			try {
				rfDeviceConnectionManager.release(connection);
			} catch (Exception e) {
				log.log(Level.FINER, e.getMessage(), e);
			}

			this.setDeviceStatus(DeviceStatusEnumeration.IDLE);
			this.invThread = null;
			return srp;

		} catch (ApplicationException aex) {
			throw aex;
		} catch (Exception e) {
			log.log(Level.FINER, e.getMessage(), e);
			this.setDeviceStatus(DeviceStatusEnumeration.ERROR);
			invThread = null;
			return srp;
		} finally {
			scanLock.unlock();
		}
	}

	@Override
	public ReadResultPair readTag(ScanData identifier, CodeTypeEnumeration codeType, int region, long offset,
			long length, byte[] password) {
		ReadResultPair rrp = new ReadResultPair();

		if (region != RFUtils.BANK_EPC && region != RFUtils.BANK_PSW && region != RFUtils.BANK_TID
				&& region != RFUtils.BANK_USR) {
			rrp.setStatus(AutoIdOperationStatusEnumeration.REGION_NOT_FOUND_ERROR);
			return rrp;
		}

		ReadOperation rdOp = new ReadOperation();
		rdOp.setBank((short) region);

		if (password != null && password.length > 0) {
			if (password.length != 4)
				password = Arrays.copyOf(password, 4);
			rdOp.setPassword(RFUtils.bytesToInt(password));
		}

		/*
		 * converts length and offset given in bytes to the right number of words
		 */
		int decreasedOffset = 0, increasedLength = 0;
		/*
		 * offset and length are both even: convert length to words as is convert offset
		 * to words as is
		 */
		if (offset % 2 == 0 && length % 2 == 0) {
			rdOp.setLength((short) (length / 2));
			rdOp.setOffset((short) (offset / 2));
		}
		/*
		 * offset is odd and length is even: - increase length by one word (to ensure
		 * that enough data is read) - decrease offset by one byte and convert result to
		 * words
		 */
		else if (offset % 2 == 1 && length % 2 == 0) {
			rdOp.setLength((short) (length / 2 + 1));
			rdOp.setOffset((short) ((offset - 1) / 2));
			decreasedOffset = 1;
			increasedLength = 1;
		}
		/*
		 * offset is even and length is odd: - increase length by one byte and convert
		 * result to words - convert offset to words as is
		 */
		else if (offset % 2 == 0 && length % 2 == 1) {
			rdOp.setLength((short) ((length + 1) / 2));
			rdOp.setOffset((short) (offset / 2));
			increasedLength = 1;
		}
		/*
		 * both offset and length are odd: - increase length by one byte and convert
		 * result to words - decrease offset by one byte and convert result to words
		 */
		else if (offset % 2 == 1 && length % 2 == 1) {
			rdOp.setLength((short) ((length + 1) / 2));
			rdOp.setOffset((short) ((offset - 1) / 2));
			decreasedOffset = 1;
		}

		Object result = executeOperation(identifier, rdOp);

		if (result instanceof AutoIdOperationStatusEnumeration)
			rrp.setStatus((AutoIdOperationStatusEnumeration) result);
		else if (result instanceof byte[]) {
			byte[] resultBytes = (byte[]) result;
			byte[] readData = (decreasedOffset == 0 && increasedLength == 0) ? resultBytes
					: Arrays.copyOfRange(resultBytes, decreasedOffset, resultBytes.length - increasedLength);

			rrp.setResultData(readData);
			rrp.setStatus(AutoIdOperationStatusEnumeration.SUCCESS);
		} else
			rrp.setStatus(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL);

		return rrp;

	}

	@Override
	public AutoIdOperationStatusEnumeration writeTag(ScanData identifier, CodeTypeEnumeration codeType, int region,
			long offset, byte[] data, byte[] password) {

		if (region != RFUtils.BANK_EPC && region != RFUtils.BANK_PSW && region != RFUtils.BANK_TID
				&& region != RFUtils.BANK_USR)
			return AutoIdOperationStatusEnumeration.REGION_NOT_FOUND_ERROR;

		if (data.length % 2 == 1 || offset % 2 == 1)
			return AutoIdOperationStatusEnumeration.NOT_SUPPORTED_BY_DEVICE;

		WriteOperation wrOp = new WriteOperation();
		wrOp.setBank((short) region);
		wrOp.setData(data);
		wrOp.setOffset((short) (offset / 2));

		if (password != null && password.length > 0) {
			if (password.length != 4)
				password = Arrays.copyOf(password, 4);
			wrOp.setPassword(RFUtils.bytesToInt(password));
		}

		return (AutoIdOperationStatusEnumeration) executeOperation(identifier, wrOp);
	}

	@Override
	public void lastScanDataChanged(Object source, ScanData oldScanData, ScanData newScanData) {
		this.setLastScanData(newScanData);
	}

	@Override
	public void rfidEventOccured(Object source, EventType event, Date timeStamp, Map<String, Object> eventArgs) {
		switch (event) {
		case RFID_SCAN_EVENT:
			eventArgs.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_DEVICE_NAME, getDeviceName());
			notifyRfidEvent(event, timeStamp, eventArgs);
			break;
		}
	}

	@Override
	public AutoIdOperationStatusEnumeration lockTag(ScanData identifier, CodeTypeEnumeration codeType, byte[] password,
			RfidLockRegionEnumeration region, RfidLockOperationEnumeration lock, long offset, long length) {

		LockOperation lockOp = new LockOperation();
		if (password != null && password.length > 0) {
			if (password.length != 4)
				password = Arrays.copyOf(password, 4);
			lockOp.setPassword(RFUtils.bytesToInt(password));
		}

		switch (region) {
		case ACCESS:
			lockOp.setField(Field.ACCESS_PASSWORD);
			break;
		case KILL:
			lockOp.setField(Field.KILL_PASSWORD);
			break;
		case TID:
			lockOp.setField(Field.TID_MEMORY);
			break;
		case EPC:
			lockOp.setField(Field.EPC_MEMORY);
			break;
		case USER:
			lockOp.setField(Field.USER_MEMORY);
			break;
		}

		switch (lock) {
		case LOCK:
			lockOp.setPrivilege(Privilege.LOCK);
			break;
		case UNLOCK:
			lockOp.setPrivilege(Privilege.UNLOCK);
			break;
		case PERMANENTLOCK:
			lockOp.setPrivilege(Privilege.PERMALOCK);
			break;
		case PERMANENTUNLOCK:
			lockOp.setPrivilege(Privilege.PERMAUNLOCK);
			break;
		}

		return (AutoIdOperationStatusEnumeration) executeOperation(identifier, lockOp);
	}

	@Override
	public AutoIdOperationStatusEnumeration setTagPassword(ScanData identifier, CodeTypeEnumeration codeType,
			RfidPasswordTypeEnumeration passwordType, byte[] accessPassword, byte[] newPassword) {
		int offset = 0;

		switch (passwordType) {
		case KILL:
			offset = 0;
			break;
		case ACCESS:
			offset = 4;
			break;
		default:
			return AutoIdOperationStatusEnumeration.NOT_SUPPORTED_BY_DEVICE;
		}

		byte[] psw = new byte[4];
		for (int i = 0; i < psw.length; i++)
			psw[i] = i < newPassword.length ? newPassword[i] : 0x00;

		return writeTag(identifier, codeType, /* reserved bank */ (short) 0, offset, psw, accessPassword);
	}

	@Override
	public AutoIdOperationStatusEnumeration killTag(ScanData identifier, CodeTypeEnumeration codeType,
			byte[] killPassword) {
		KillOperation killOp = new KillOperation();
		if (killPassword != null && killPassword.length > 0) {
			if (killPassword.length != 4)
				killPassword = Arrays.copyOf(killPassword, 4);
			killOp.setKillPassword(RFUtils.bytesToInt(killPassword));
		} else
			return AutoIdOperationStatusEnumeration.PERMISSON_ERROR;

		return (AutoIdOperationStatusEnumeration) executeOperation(identifier, killOp);
	}

	private RegulatoryCapabilities getRegulatoryCapabilities(RFDevice rfDevice)
			throws ImplementationException, ConnectionException, ApplicationException {
		List<Capabilities> caps = rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
		if (caps == null || caps.isEmpty()) {
			log.log(Level.SEVERE, "Cannot get regulatory capabilities: Missing result");
			throw ApplicationException.invalidState();
		}
		RegulatoryCapabilities regulatoryCaps = (RegulatoryCapabilities) caps.get(0);
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Received " + CapabilityType.REGULATORY_CAPABILITIES);
		}
		return regulatoryCaps;
	};

	private InventorySettings getInventorySettings(RFDevice rfDevice)
			throws ConnectionException, ImplementationException, ApplicationException {
		List<Configuration> confs = rfDevice.getConfiguration(ConfigurationType.INVENTORY_SETTINGS,
				(short) 0 /* antennaId */, (short) 0, (short) 0);
		if (confs == null || confs.isEmpty()) {
			log.log(Level.SEVERE, "Cannot get inventory settings: Missing result");
			throw ApplicationException.invalidState();
		}
		InventorySettings inventorySettings = (InventorySettings) confs.get(0);
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Received " + ConfigurationType.INVENTORY_SETTINGS);
		}
		return inventorySettings;
	};

	private void setInventorySettings(RFDevice rfDevice, InventorySettings inventorySettings)
			throws ConnectionException, ImplementationException, ParameterException {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Sending " + ConfigurationType.INVENTORY_SETTINGS);
		}
		rfDevice.setConfiguration(Arrays.asList((Configuration) inventorySettings));
	}

	private IOConfiguration getIOConfiguration(IODevice ioDevice, short port)
			throws havis.device.io.exception.ConnectionException, havis.device.io.exception.ParameterException,
			havis.device.io.exception.ImplementationException, ApplicationException {
		List<havis.device.io.Configuration> confs = ioDevice.getConfiguration(Type.IO, port);
		if (confs == null || confs.isEmpty()) {
			log.log(Level.SEVERE, "Cannot get IO configuration: Missing result");
			throw ApplicationException.invalidState();
		}
		IOConfiguration ioConf = (IOConfiguration) confs.get(0);
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Received " + Type.IO);
		}
		return ioConf;
	}

	private void setIOConfiguration(IODevice ioDevice, IOConfiguration conf)
			throws havis.device.io.exception.ConnectionException, havis.device.io.exception.ParameterException,
			havis.device.io.exception.ImplementationException {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Sending " + Type.IO);
		}
		List<havis.device.io.Configuration> confs = new ArrayList<>();
		confs.add(conf);
		ioDevice.setConfiguration(confs);
	}

	private HaIOState getIOState(final Param param, final short port) throws ApplicationException {
		IOOperation<HaIOState> op = new IOOperation<HaIOState>("get " + param, ioDeviceConnectionManager) {

			@Override
			public HaIOState operation(IODevice ioDevice) throws Exception {
				IOConfiguration ioConf = getIOConfiguration(ioDevice, port);
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "  state=" + ioConf.getState());
				}
				switch (ioConf.getState()) {
				case HIGH:
					return HaIOState.HIGH;
				case LOW:
					return HaIOState.LOW;
				case UNKNOWN:
				}
				return HaIOState.UNKNOWN;
			}
		};
		return op.run();
	}

	private void setIOState(final Param param, final short port, final HaIOState state) throws ApplicationException {
		IOOperation<Object> op = new IOOperation<Object>("set " + param, ioDeviceConnectionManager) {

			@Override
			public Object operation(IODevice ioDevice) throws Exception {
				// get current configuration
				IOConfiguration ioConf = getIOConfiguration(ioDevice, port);
				if (Direction.INPUT == ioConf.getDirection()) {
					log.log(Level.SEVERE, "Cannot set state for input port " + port);
					throw ApplicationException.invalidState();
				}
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "  oldState=" + ioConf.getState() + ", newState=" + state);
				}
				State newState = State.UNKNOWN;
				// set new state
				switch (state) {
				case HIGH:
					newState = State.HIGH;
					break;
				case LOW:
					newState = State.LOW;
					break;
				case UNKNOWN:
					break;
				}
				if (ioConf.getState() != newState) {
					// update configuration
					ioConf.setState(newState);
					setIOConfiguration(ioDevice, ioConf);
					// send change event
					notifyParamChange(param, state.value());
				}
				return null;
			}
		};
		op.run();
	}

	private HaIODirection getIODirection(final Param param, final short port) throws ApplicationException {
		IOOperation<HaIODirection> op = new IOOperation<HaIODirection>("get " + param, ioDeviceConnectionManager) {

			@Override
			public HaIODirection operation(IODevice ioDevice) throws Exception {
				IOConfiguration ioConf = getIOConfiguration(ioDevice, port);
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "  direction=" + ioConf.getDirection());
				}
				switch (ioConf.getDirection()) {
				case INPUT:
					return HaIODirection.INPUT;
				case OUTPUT:
				}
				return HaIODirection.OUTPUT;
			}
		};
		return op.run();
	}

	private void setIODirection(final Param param, final short port, final HaIODirection direction)
			throws ApplicationException {
		IOOperation<Object> op = new IOOperation<Object>("set " + param, ioDeviceConnectionManager) {

			@Override
			public Object operation(IODevice ioDevice) throws Exception {
				// get current configuration
				IOConfiguration ioConf = getIOConfiguration(ioDevice, port);
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "  oldDirection=" + ioConf.getDirection() + ", newDirection=" + direction);
				}
				// set new direction
				Direction newDirection = Direction.OUTPUT;
				switch (direction) {
				case INPUT:
					newDirection = Direction.INPUT;
				case OUTPUT:
				}
				if (ioConf.getDirection() != newDirection) {
					// update configuration
					ioConf.setDirection(newDirection);
					setIOConfiguration(ioDevice, ioConf);
					// send change event
					notifyParamChange(param, direction.value());
				}
				return null;
			}
		};
		op.run();
	}

	private Object executeOperation(ScanData identifier, TagOperation operation) {

		/* if an inventory thread exists, set the suspend flag */
		boolean suspend = this.invThread != null;
		Integer connection = null;
		try {
			scanLock.lock();

			/*
			 * if suspend flag is set, suspend the inventory thread prior to executing the
			 * operation
			 */
			if (suspend) {
				if (!this.invThread.suspendInventory()) /*
														 * if inventory thread cannot be suspended on time, return
														 * DEVICE_NOT_READY
														 */
					return AutoIdOperationStatusEnumeration.DEVICE_NOT_READY;
			}
			this.setDeviceStatus(DeviceStatusEnumeration.BUSY);

			/*
			 * no need to to extra inventory round if operation is a read operation
			 */
			boolean skipInventory = operation instanceof ReadOperation;

			Filter filter = createFilter(identifier);
			if (filter == null)
				return AutoIdOperationStatusEnumeration.NO_IDENTIFIER;

			RFDevice rfDevice = rfDeviceConnectionManager.getRfDevice();
			if (!suspend) {
				try {
					connection = rfDeviceConnectionManager.acquire();
				} catch (Exception e) {
					log.log(Level.FINER, e.getMessage(), e);
					this.setDeviceStatus(DeviceStatusEnumeration.ERROR);
					return AutoIdOperationStatusEnumeration.DEVICE_NOT_READY;
				}

				if (!initialized)
					initialize(rfDevice);
			}

			TagData tag = null;
			try {
				List<TagData> tags = null;

				if (!skipInventory) {
					tags = rfDevice.execute(getAntennaIds(), Arrays.asList(filter), new ArrayList<TagOperation>());
					if (tags == null || tags.isEmpty())
						/*
						 * if no tags exist have been found, a NO_IDENTIFIER error state is returned
						 */
						return AutoIdOperationStatusEnumeration.NO_IDENTIFIER;

					if (tags.size() > 1)
						/*
						 * if multiple tags exist have been found, a MULTIPLE_IDENTIFIERS error state is
						 * returned
						 */
						return AutoIdOperationStatusEnumeration.MULTIPLE_IDENTIFIERS;

					/*
					 * check if the reported tag was validly matched. If not, return NO_IDENTIFIER
					 */
					if (!isValidMatch(identifier, tags.get(0)))
						return AutoIdOperationStatusEnumeration.NO_IDENTIFIER;
				}

				/*
				 * execute the operation in another call, now knowing that only one transponder
				 * is in the field unless inventory has been skipped
				 */
				tags = rfDevice.execute(getAntennaIds(), Arrays.asList(filter), Arrays.asList(operation));

				/* get the first (and only) result */
				if (tags != null && tags.size() == 1) {
					tag = tags.get(0);
					if (!isValidMatch(identifier, tag))
						return AutoIdOperationStatusEnumeration.NO_IDENTIFIER;
				}

				/*
				 * or return an error state depending on the results returned by the device
				 */
				else
					return (tags == null || tags.size() == 0) ? AutoIdOperationStatusEnumeration.NO_IDENTIFIER
							: AutoIdOperationStatusEnumeration.MULTIPLE_IDENTIFIERS;

				/* If tag exists but no result, something peculiar happened */
				if (tag.getResultList().isEmpty())
					return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;

			} catch (ParameterException e) {
				log.log(Level.FINER, e.getMessage(), e);
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
			} catch (ImplementationException | CommunicationException | ConnectionException e) {
				log.log(Level.FINER, e.getMessage(), e);
				this.setDeviceStatus(DeviceStatusEnumeration.ERROR);
				return AutoIdOperationStatusEnumeration.DEVICE_NOT_READY;
			}

			OperationResult opRes = tag.getResultList().get(0);

			if (opRes instanceof ReadResult) {
				ReadResult rdRes = (ReadResult) opRes;
				AutoIdOperationStatusEnumeration status = Mapper.mapReadResultToStatus(rdRes.getResult());
				if (status == AutoIdOperationStatusEnumeration.SUCCESS)
					return rdRes.getReadData();
				else
					return status;

			} else if (opRes instanceof WriteResult) {
				WriteResult wrRes = (WriteResult) opRes;
				return Mapper.mapWriteResultToStatus(wrRes.getResult());

			} else if (opRes instanceof LockResult) {
				LockResult lkRes = (LockResult) opRes;

				AutoIdOperationStatusEnumeration status = Mapper.mapLockResultToStatus(lkRes.getResult());

				/*
				 * if status is PASSWORD_ERROR but no password has been provided, return
				 * PERMISSION_ERROR instead
				 */

				if (status == AutoIdOperationStatusEnumeration.PASSWORD_ERROR
						&& ((LockOperation) operation).getPassword() == 0)
					return AutoIdOperationStatusEnumeration.PERMISSON_ERROR;

				return status;

			} else if (opRes instanceof KillResult) {
				KillResult klRes = (KillResult) opRes;
				return Mapper.mapKillResultToStatus(klRes.getResult());
			}

			return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;

		} catch (Exception e) {
			log.log(Level.FINER, e.getMessage(), e);
			this.setDeviceStatus(DeviceStatusEnumeration.ERROR);
			return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
		} finally {

			/* only disconnect if there was no inventory thread running */
			if (!suspend) {
				if (connection != null) {
					try {
						rfDeviceConnectionManager.release(connection);
					} catch (Exception e) {
						log.log(Level.FINER, e.getMessage(), e);
					}
				}
				if (this.getDeviceStatus() == DeviceStatusEnumeration.BUSY)
					this.setDeviceStatus(DeviceStatusEnumeration.IDLE);
			} else { /* if there was an inventory thread running, resume it */
				try {
					if (!this.invThread.resumeInventory())
						log.fine("Inventory thread could not be resumed on time.");
				} catch (Exception e) {
					log.log(Level.FINER, e.getMessage(), e);
				}
			}

			scanLock.unlock();
		}
	}

	private boolean isValidMatch(ScanData identifier, TagData tag) {

		/* if identifier does not look as expected it won't be a valid match */
		if (identifier.getEpc() == null || identifier.getEpc().getUid() == null)
			return false;

		/* compare the full EPC of the identifier with the one of the tag */
		return Arrays.equals(tag.getEpc(), identifier.getEpc().getUid());
	}

	private Filter createFilter(ScanData identifier) {
		if (identifier.getEpc() == null || identifier.getEpc().getUid() == null)
			return null;

		byte[] epcData = null;
		epcData = identifier.getEpc().getUid();

		Filter f = new Filter();
		f.setData(epcData);
		f.setBank(RFUtils.BANK_EPC);
		f.setBitOffset((short) 32 /* 2 words = 4 bytes = 32 bits */);
		f.setBitLength((short) (epcData.length * 8));
		f.setMatch(true);
		byte[] mask = new byte[epcData.length];
		for (int i = 0; i < epcData.length; i++)
			mask[i] = (byte) 0xff;
		f.setMask(mask);

		return f;
	}

	@Override
	public void setAntennaNames(List<AntennaNameIdPair> list) {
		try {
			scanLock.lock();
			super.setAntennaNames(list);
		} finally {
			scanLock.unlock();
		}
	}

	private List<Short> getAntennaIds() {
		List<Short> antennas = new ArrayList<>();
		for (AntennaNameIdPair a : getAntennaNames()) {
			// if all antennas
			if (0 == a.getAntennaId()) {
				antennas.clear();
				antennas.add((short) 0);
				break;
			} else {
				antennas.add((short) a.getAntennaId());
			}
		}
		return antennas;
	}

	// RFConsumer

	@Override
	public void connectionAttempted() {

	}

	@Override
	public List<TagOperation> getOperations(TagData arg0) {
		return null;
	}

	@Override
	public void keepAlive() {

	}
}
