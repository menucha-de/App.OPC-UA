package havis.net.aim.device.rf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.DatatypeConverter;

import havis.device.rf.RFDevice;
import havis.device.rf.capabilities.Capabilities;
import havis.device.rf.capabilities.CapabilityType;
import havis.device.rf.capabilities.RegulatoryCapabilities;
import havis.device.rf.capabilities.TransmitPowerTableEntry;
import havis.device.rf.configuration.AntennaConfiguration;
import havis.device.rf.configuration.Configuration;
import havis.device.rf.configuration.ConfigurationType;
import havis.device.rf.exception.CommunicationException;
import havis.device.rf.exception.ConnectionException;
import havis.device.rf.exception.ImplementationException;
import havis.device.rf.exception.ParameterException;
import havis.device.rf.tag.Filter;
import havis.device.rf.tag.TagData;
import havis.device.rf.tag.operation.TagOperation;
import havis.net.aim.opcua.Constants.EventType;
import havis.net.aim.opcua.Environment;
import havis.net.aim.opcua.LastScanDataChangeListener;
import havis.net.aim.opcua.RfidDeviceEventListener;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.RfidSighting;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanDataEpc;
import havis.net.aim.xsd.ScanSettings;
import havis.net.aim.xsd.TagSet;

public class InventoryThread implements Runnable {
	private ScanSettings scanSettings;
	InventoryTagProcessor tagProcessor;
	// antennaId -> transmit power in mW
	private HashMap<Short, Short> powerMap;
	private RFDevice rfDevice;
	private int cycles;
	private boolean running;
	private boolean suspended;
	private Lock scanLock;
	private Condition inventoryFinished;
	private Condition inventoryStopped;
	private Condition inventorySuspended;
	private Condition inventoryResumed;

	private boolean sigInventoryFinished;
	private boolean sigInventoryStopped;
	private boolean sigInventorySuspended;
	private boolean sigInventoryResumed;

	private List<Short> antennas;
	private Throwable error;
	private LastScanDataChangeListener scanDataListener;
	private ScanData lastScanData;
	private ScanData currentScanData;
	private RfidDeviceEventListener scanEventListener;
	private long eventsSent = 0L;
	private boolean sightingsLimitReached;
	private Integer eventMaxSightingCount;
	private Integer totalMaxSightingCount;

	private TagSet tagSet = TagSet.CURRENT;

	public InventoryThread(RFDevice rfDevice) {
		this.rfDevice = rfDevice;
	}

	private void inventory() {

		/* reset the error instance */
		error = null;

		/* set cycle count to 0 */
		cycles = 0;

		/* reset the sightingsLimitReached flag */
		sightingsLimitReached = false;

		/* create a new map for the results */
		tagProcessor = new InventoryTagProcessor(tagSet, powerMap, eventMaxSightingCount, totalMaxSightingCount);

		/* as long as running flag is set... */
		while (running) {

			/* result list for tags coming from RF device */
			List<TagData> tags = null;
			try {

				if (suspended) {
					signalSuspended();
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				} else if (running) {
					/*
					 * call execute with empty filters and operations list to
					 * perform inventory
					 */
					tags = this.rfDevice.execute(this.antennas, new ArrayList<Filter>(), new ArrayList<TagOperation>());

					// process results
					// this.processScanResult(tags);
					sightingsLimitReached = tagProcessor.process(tags);
					if (tags == null || tags.isEmpty()) {
						currentScanData = null;
					} else {
						// get scan data of last tag
						TagData lastTag = tags.get(tags.size() - 1);
						String epcStr = DatatypeConverter.printHexBinary(lastTag.getEpc());
						RfidScanResult rsr = tagProcessor.getCurrentScanResultMap().get(epcStr);
						currentScanData = rsr.getScanData();
					}

					/* increment the cycle count */
					cycles++;
				}

				if (!suspended)
					signalResumed();

			} catch (ParameterException | CommunicationException | ImplementationException | ConnectionException e) {
				this.error = e;
				// fire scan events
				fireScanEvents(tagProcessor.generateScanEvents());

				/* unset the running flag */
				running = false;

				/* send signal that inventory cycle is finished */
				signalFinished();

				return;
			}

			/*
			 * if cycle limit has been reached or data available option is set
			 * and data became available
			 */
			if (this.sightingsLimitReached || this.scanSettings.getCycles() > 0 && cycles == scanSettings.getCycles()
					|| scanSettings.isDataAvailable() && null != tags && tags.size() > 0) {
				// fire scan events
				fireScanEvents(tagProcessor.generateScanEvents());

				/* unset the running flag */
				running = false;

				/* send signal that inventory cycle is finished */
				signalFinished();

				return;
			} // else if inventory has been stopped or the event interval has
				// elapsed
			else if (!running || eventsSent + Environment.EVENT_INTERVAL < new Date().getTime()) {
				// fire scan events
				fireScanEvents(tagProcessor.generateScanEvents());
			}
		}

		signalFinished();
		signalStopped();

	}

	public synchronized void setTagSet(TagSet tagSet) {
		this.tagSet = tagSet;
	}

	@Deprecated
	private synchronized void processScanResult(List<TagData> tags) {
		Map<String, RfidScanResult> scanResultMap = new HashMap<>();

		if (tags.isEmpty())
			currentScanData = null;

		Set<String> tagDeletions = null;
		Set<String> tagAdditions = null;

		/*
		 * determine tag additions and deletions if eventListener is registered
		 * and eventMode is ADDIONS or DELETIONS
		 */
		if (scanEventListener != null && (tagSet == TagSet.ADDITIONS || tagSet == TagSet.DELETIONS)) {
			Set<String> previousTagList = scanResultMap.keySet();
			Set<String> currentTagList = new HashSet<>();
			for (TagData tag : tags)
				currentTagList.add(DatatypeConverter.printHexBinary(tag.getEpc()));

			tagDeletions = new HashSet<>(previousTagList);
			tagDeletions.removeAll(currentTagList);

			tagAdditions = new HashSet<>(currentTagList);
			tagAdditions.removeAll(previousTagList);

			/* remove all tag deletions from the scanResult map */
			for (String epc : tagDeletions) {
				RfidScanResult rsr = scanResultMap.remove(epc);

				/* fire scan event if eventMode is DELETIONS */
				if (tagSet == TagSet.DELETIONS)
					fireScanEvent(rsr);
			}
		}

		for (TagData tag : tags) {

			String epcStr = DatatypeConverter.printHexBinary(tag.getEpc());
			RfidScanResult rsr = scanResultMap.get(epcStr);

			if (rsr == null) {
				scanResultMap.put(epcStr, rsr = new RfidScanResult());
				rsr.setCodeType(CodeTypeEnumeration.EPC);
				rsr.setScanData(new ScanData());
				rsr.getScanData().setEpc(new ScanDataEpc());
				rsr.getScanData().getEpc().setPc(DataTypeConverter.ushort(tag.getPc()));
				rsr.getScanData().getEpc().setUid(tag.getEpc());
				rsr.setTimeStamp(new Date().getTime());
			}

			RfidSighting rs = new RfidSighting();
			rs.setAntennaId(tag.getAntennaID());
			rs.setStrength(tag.getRssi());
			rs.setTimestamp(new Date().getTime());

			rs.setCurrentPowerLevel(this.powerMap.get(tag.getAntennaID()));
			rsr.getSightings().add(rs);

			/*
			 * fire event if event listener is registered, eventMode is CURRENT
			 * and event interval is elapsed
			 */
			if (this.scanEventListener != null && tagSet == TagSet.CURRENT
					&& eventsSent + Environment.EVENT_INTERVAL < new Date().getTime())
				for (RfidScanResult r : scanResultMap.values())
					fireScanEvent(r);

			/*
			 * if sightings exceeds specified maximum (sightings are reset by
			 * firing events) set sightingsLimitReached flag leading to scan
			 * process being stopped
			 */
			if (rsr.getSightings().size() >= Environment.MAXIMUM_SIGHTINGS)
				this.sightingsLimitReached = true;

			currentScanData = rsr.getScanData();
		}

		/*
		 * fire scan event for each addition if eventListener is registered and
		 * eventMode is ADDIONS
		 */
		if (scanEventListener != null && tagSet == TagSet.ADDITIONS && tagAdditions != null)
			for (String epc : tagAdditions)
				fireScanEvent(scanResultMap.get(epc));
	}

	@Deprecated
	private void fireScanEvent(RfidScanResult rsr) {
		if (scanEventListener == null)
			return;

		Map<String, Object> eventArgs = new HashMap<>();
		eventArgs.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT, rsr);
		this.scanEventListener.rfidEventOccured(this, EventType.RFID_SCAN_EVENT, new Date(), eventArgs);
		setLastScanData(rsr.getScanData());
		eventsSent = new Date().getTime();
		rsr.getSightings().clear();
	}

	private void fireScanEvents(List<RfidScanResult> scanResults) {
		if (scanEventListener != null) {
			for (RfidScanResult scanResult : scanResults) {
				Map<String, Object> eventArgs = new HashMap<>();
				eventArgs.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT, scanResult);
				this.scanEventListener.rfidEventOccured(this, EventType.RFID_SCAN_EVENT, new Date(), eventArgs);
			}
		}
		if (!scanResults.isEmpty()) {
			RfidScanResult lastScanResult = scanResults.get(scanResults.size() - 1);
			setLastScanData(lastScanResult.getScanData());
		}
		eventsSent = new Date().getTime();
	}

	private void setLastScanData(ScanData scanData) {
		this.scanDataListener.lastScanDataChanged(this, this.lastScanData, this.lastScanData = scanData);
	}

	public Collection<RfidScanResult> startInventory(ScanSettings scanSettings, Lock scanLock, List<Short> antennas,
			LastScanDataChangeListener scanDataListener) throws ConnectionException, ImplementationException {
		/*
		 * event listener to be notified of scan data changes (notified by
		 * setScanData method after scan)
		 */
		this.scanDataListener = scanDataListener;

		/*
		 * event listener to be notified of scan events (i.e. new tag appears in
		 * field)
		 */
		final Collection<RfidScanResult> scanResults = new ArrayList<>();
		this.scanEventListener = new RfidDeviceEventListener() {

			@Override
			public void rfidEventOccured(Object source, EventType event, Date timeStamp,
					Map<String, Object> eventArgs) {
				RfidScanResult rsr = (RfidScanResult) eventArgs
						.get(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT);
				boolean found = false;
				// for each existing scan result
				for (RfidScanResult scanResult : scanResults) {
					// if EPC is the same
					if (Arrays.equals(scanResult.getScanData().getEpc().getUid(),
							rsr.getScanData().getEpc().getUid())) {
						// add sightings to existing scan result
						scanResult.getSightings().addAll(rsr.getSightings());
						found = true;
						break;
					}
				}
				if (!found) {
					// add scan result as new entry
					scanResults.add(rsr);
				}
			}

			@Override
			public void scanAsyncFinished(Object source) {
			}
		};

		/* set the antennas list on with to run the inventory */
		this.antennas = antennas;

		/* set the scan settings instance variable */
		this.scanSettings = scanSettings;

		/* set the scan lock instance variable */
		this.scanLock = scanLock;

		/* create a new condition for receiving an inventory finished signal */
		this.inventoryFinished = scanLock.newCondition();

		/* build the current power map */
		this.powerMap = this.buildCurrentPowerMap();

		this.eventMaxSightingCount = null;
		this.totalMaxSightingCount = Environment.MAXIMUM_SIGHTINGS;

		/* start the inventory thread performing an inventory repeatedly */
		new Thread(this, InventoryThread.class.getSimpleName()).start();

		/* flag if inventory has finished gracefully or has to be stopped */
		boolean invFinished = false;

		/* protection against spurious wakeups */
		sigInventoryFinished = false;
		sigInventoryStopped = false;

		/* get the duration from the scan settings */
		double duration = scanSettings.getDuration();

		if (duration > 0) {
			/*
			 * wait for the given duration for the inventory thread to finish
			 */

			long now = new Date().getTime();
			long waitUntil = now + (long) duration;
			while (!sigInventoryFinished && now < waitUntil) {
				try {
					invFinished = this.inventoryFinished.awaitUntil(new Date(waitUntil));
				} catch (InterruptedException e) {
				}
				now = new Date().getTime();
			}
		} else {
			/* wait infinitely... */
			while (!sigInventoryFinished)
				try {
					this.inventoryFinished.await();
				} catch (InterruptedException e) {
				}

			invFinished = true;
		}

		/* if inventory thread did not finish within the given duration */
		if (!invFinished) {
			/*
			 * send a stop inventory request returning a new condition to be
			 * signaled once the inventory has been stopped
			 */
			Condition inventoryStopped = this.stopInventory();

			/* wait for the inventory stopped signal (max 500ms) */
			long now = new Date().getTime();
			long waitUntil = now + 500;
			while (!sigInventoryStopped && now < waitUntil) {
				try {
					inventoryStopped.awaitUntil(new Date(waitUntil));
				} catch (InterruptedException e) {
				}
				now = new Date().getTime();
			}
		}

		setLastScanData(currentScanData);

		return scanResults;
	}

	public void startInventoryAsync(ScanSettings scanSettings, List<Short> antennas,
			LastScanDataChangeListener scanDataListener, RfidDeviceEventListener scanEventListener)
			throws ConnectionException, ImplementationException {

		/*
		 * event listener to be notified of scan data changes (notified by
		 * setScanData method after scan)
		 */
		this.scanDataListener = scanDataListener;

		/*
		 * event listener to be notified of scan events (i.e. new tag appears in
		 * field)
		 */
		this.scanEventListener = scanEventListener;

		/* set the antennas list on with to run the inventory */
		this.antennas = antennas;

		/* set the scan settings instance variable */
		this.scanSettings = scanSettings;

		/* build the current power map */
		this.powerMap = this.buildCurrentPowerMap();

		this.eventMaxSightingCount = Environment.MAXIMUM_SIGHTINGS;
		this.totalMaxSightingCount = null;

		/*
		 * Start thread that calls the inventory thread and waits asynchronously
		 * for it to finish
		 */
		new Thread(new Runnable() {
			public void run() {

				InventoryThread invThread = InventoryThread.this;
				invThread.scanLock = new ReentrantLock();
				invThread.inventoryFinished = invThread.scanLock.newCondition();
				boolean invFinished = false;

				try {
					invThread.scanLock.lock();
					new Thread(invThread, InventoryThread.class.getSimpleName()).start();
					double duration = invThread.scanSettings.getDuration();
					if (duration > 0)
						invFinished = invThread.inventoryFinished.await((long) duration, TimeUnit.MILLISECONDS);
					else {
						invThread.inventoryFinished.await();
						invFinished = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					invThread.scanLock.unlock();
				}

				if (!invFinished) {

					/*
					 * send a stop inventory request returning a new condition
					 * to be signaled once the inventory has been stopped
					 */
					Condition invStopped = invThread.stopInventory();

					/* wait for the inventory stopped signal */
					try {
						invThread.scanLock.lock();
						invStopped.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						invThread.scanLock.unlock();
					}
				}
				invThread.scanEventListener.scanAsyncFinished(this);
			};
		}, "InventoryThreadObserver").start();
	}

	public void stopInventoryAsync() {
		this.stopInventory();
	}

	private HashMap<Short, Short> buildCurrentPowerMap() throws ConnectionException, ImplementationException {

		boolean unitIsMw = true;
		HashMap<Short, Short> powerMap = new HashMap<>();

		List<Capabilities> caps = this.rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
		RegulatoryCapabilities regCaps = (RegulatoryCapabilities) caps.get(0);

		List<Configuration> configs = this.rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, (short) 0,
				(short) 0, (short) 0);

		for (Configuration config : configs) {
			AntennaConfiguration aConfig = (AntennaConfiguration) config;
			short pwrIndex = aConfig.getTransmitPower();
			for (TransmitPowerTableEntry e : regCaps.getTransmitPowerTable().getEntryList())
				if (e.getIndex() == pwrIndex)
					powerMap.put(aConfig.getId(),
							unitIsMw ? (short) TxLevel.fromDBm(e.getTransmitPower()).mW : e.getTransmitPower());
		}

		return powerMap;
	}

	public boolean suspendInventory() {
		try {
			scanLock.lock();
			this.inventorySuspended = scanLock.newCondition();
			this.suspended = true;
			this.sigInventorySuspended = false;

			long now = new Date().getTime();
			long waitUntil = now + 500;
			boolean res = false;
			while (!sigInventorySuspended && waitUntil > now) {
				res = this.inventorySuspended.awaitUntil(new Date(waitUntil));
				now = new Date().getTime();
			}
			return res;
		} catch (InterruptedException e) {
			return false;
		} finally {
			this.inventorySuspended = null;
			scanLock.unlock();
		}
	}

	public boolean resumeInventory() {
		try {
			scanLock.lock();
			this.inventoryResumed = scanLock.newCondition();
			this.suspended = false;
			this.sigInventoryFinished = false;

			long now = new Date().getTime();
			long waitUntil = now + 500;
			boolean res = false;

			while (!sigInventoryResumed && waitUntil > now) {
				res = this.inventoryResumed.awaitUntil(new Date(waitUntil));
				now = new Date().getTime();
			}
			return res;
		} catch (InterruptedException e) {
			return false;
		} finally {
			this.inventoryResumed = null;
			scanLock.unlock();
		}
	}

	public Throwable getError() {
		return error;
	}

	private Condition stopInventory() {
		try {
			scanLock.lock();
			this.inventoryStopped = scanLock.newCondition();
			this.running = false;
		} finally {
			scanLock.unlock();
		}
		return this.inventoryStopped;
	}

	private void signalFinished() {
		try {
			scanLock.lock();
			sigInventoryFinished = true;
			inventoryFinished.signal();
		} finally {
			scanLock.unlock();
		}
	}

	private void signalStopped() {
		try {
			scanLock.lock();
			sigInventoryStopped = true;
			inventoryStopped.signal();
		} finally {
			scanLock.unlock();
		}
	}

	private void signalSuspended() {
		if (this.inventorySuspended == null)
			return;
		try {
			scanLock.lock();
			sigInventorySuspended = true;
			this.inventorySuspended.signal();
		} finally {
			scanLock.unlock();
		}
	}

	private void signalResumed() {
		if (this.inventoryResumed == null)
			return;
		try {
			scanLock.lock();
			sigInventoryResumed = true;
			this.inventoryResumed.signal();
		} finally {
			scanLock.unlock();
		}
	}

	@Override
	public void run() {
		this.running = true;
		inventory();
	}
}
