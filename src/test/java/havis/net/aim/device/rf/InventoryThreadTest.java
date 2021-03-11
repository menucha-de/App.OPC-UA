package havis.net.aim.device.rf;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

import havis.device.rf.RFDevice;
import havis.device.rf.capabilities.Capabilities;
import havis.device.rf.capabilities.CapabilityType;
import havis.device.rf.capabilities.RegulatoryCapabilities;
import havis.device.rf.capabilities.TransmitPowerTable;
import havis.device.rf.capabilities.TransmitPowerTableEntry;
import havis.device.rf.common.util.RFUtils;
import havis.device.rf.configuration.AntennaConfiguration;
import havis.device.rf.configuration.ConfigurationType;
import havis.device.rf.configuration.ConnectType;
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
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanSettings;
import havis.net.aim.xsd.TagSet;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

public class InventoryThreadTest {

	@Mocked
	RFDevice rfDevice;
	@Mocked
	LastScanDataChangeListener scanDataListener;
	@Mocked
	RfidDeviceEventListener scanEventListener;

	@Test
	public void testInventoryThread() {
		InventoryThread it = new InventoryThread(rfDevice);
		assertEquals(rfDevice, getField(it, "rfDevice"));
	}

	@Test
	public void testSetTagSet() {
		InventoryThread it = new InventoryThread(rfDevice);
		assertEquals(TagSet.CURRENT, getField(it, "tagSet"));
		it.setTagSet(TagSet.ADDITIONS);
		assertEquals(TagSet.ADDITIONS, getField(it, "tagSet"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStartInventory() throws ParameterException, CommunicationException, ImplementationException,
			ConnectionException, InterruptedException {

		final String epc1 = "300833B2DDD9004433221101";
		final String epc2 = "300833B2DDD9004433221102";

		TagData td1 = new TagData();
		td1.setAntennaID((short) 1);
		td1.setCrc((short) 1234);
		td1.setEpc(RFUtils.hexToBytes(epc1));
		td1.setPc((short) 3400);
		td1.setRssi(-64);

		TagData td2 = new TagData();
		td2.setAntennaID((short) 1);
		td2.setCrc((short) 0x8000); // ushort value
		td2.setEpc(RFUtils.hexToBytes(epc2));
		td2.setPc((short) 0x8001); // ushort value
		td2.setRssi(-64);

		final List<TagData> tags = new ArrayList<>();
		tags.add(td1);
		tags.add(td2);

		RegulatoryCapabilities regCaps = new RegulatoryCapabilities();
		final List<Capabilities> capabilities = new ArrayList<>();
		capabilities.add(regCaps);
		TransmitPowerTableEntry e = new TransmitPowerTableEntry();
		e.setIndex((short) 1);
		e.setTransmitPower((short) 19);
		regCaps.setTransmitPowerTable(new TransmitPowerTable());
		regCaps.getTransmitPowerTable().getEntryList().add(e);

		AntennaConfiguration aCfg = new AntennaConfiguration();
		aCfg.setConnect(ConnectType.TRUE);
		aCfg.setId((short) 1);
		aCfg.setTransmitPower((short) 1);
		final List<AntennaConfiguration> antennaConfigurations = new ArrayList<>();
		antennaConfigurations.add(aCfg);

		new Expectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new Delegate<RFDevice>() {
					@SuppressWarnings("unused")
					List<TagData> execute(List<Short> a, List<Filter> b, List<TagOperation> c) {
						synchronized (tags) {
							// slow down the execution
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							return tags;
						}
					}
				};

				rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
				result = capabilities;

				rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, (short) 0, (short) 0, (short) 0);
				result = antennaConfigurations;
			}
		};

		InventoryThread it = new InventoryThread(rfDevice);

		ScanSettings scs = new ScanSettings();
		scs.setCycles(10);
		scs.setDataAvailable(false);
		scs.setDuration(0);

		Lock scanLock = new ReentrantLock();
		scanLock.lock();

		List<Short> antennas = new ArrayList<>();
		antennas.add((short) 0);
		List<RfidScanResult> lastScanResults = new ArrayList<>(
				it.startInventory(scs, scanLock, antennas, scanDataListener));
		scanLock.unlock();

		assertEquals(2, lastScanResults.size());
		RfidScanResult scanResult1 = lastScanResults.get(0);
		RfidScanResult scanResult2 = lastScanResults.get(1);
		assertEquals(epc1, DatatypeConverter.printHexBinary(scanResult1.getScanData().getEpc().getUid()));
		assertEquals(epc2, DatatypeConverter.printHexBinary(scanResult2.getScanData().getEpc().getUid()));
		assertEquals(scs.getCycles(), scanResult1.getSightings().size());
		assertEquals(scs.getCycles(), scanResult2.getSightings().size());
		assertEquals(3400, scanResult1.getScanData().getEpc().getPc());
		assertEquals(0x8001, scanResult2.getScanData().getEpc().getPc());

		new Verifications() {
			{
				ScanData scanData = null;
				scanDataListener.lastScanDataChanged(any, (ScanData) any, scanData = withCapture());
				String epc = RFUtils.bytesToHex(scanData.getEpc().getUid());
				assertTrue(epc.equals(epc1) || epc.equals(epc2));
			}
		};

		scs.setCycles(0);
		scs.setDuration(200);

		long now = new Date().getTime();
		long soon = now + (long) scs.getDuration() + 100;

		scanLock.lock();
		lastScanResults = new ArrayList<>(it.startInventory(scs, scanLock, antennas, scanDataListener));
		scanLock.unlock();

		assertEquals(2, lastScanResults.size());
		scanResult1 = lastScanResults.get(0);
		scanResult2 = lastScanResults.get(1);
		assertEquals(epc1, DatatypeConverter.printHexBinary(scanResult1.getScanData().getEpc().getUid()));
		assertEquals(epc2, DatatypeConverter.printHexBinary(scanResult2.getScanData().getEpc().getUid()));

		long ts0 = scanResult1.getSightings().get(0).getTimestamp();
		long tsN = scanResult1.getSightings().get(scanResult1.getSightings().size() - 1).getTimestamp();

		assertTrue((tsN - ts0) < scs.getDuration());
		assertTrue(ts0 >= now);
		assertTrue(tsN < soon);

		ts0 = scanResult2.getSightings().get(0).getTimestamp();
		tsN = scanResult2.getSightings().get(scanResult2.getSightings().size() - 1).getTimestamp();

		assertTrue((tsN - ts0) < scs.getDuration());
		assertTrue(ts0 >= now);
		assertTrue(tsN < soon);

		scs.setCycles(0);
		scs.setDuration(0);
		scs.setDataAvailable(true);

		scanLock.lock();
		lastScanResults = new ArrayList<>(it.startInventory(scs, scanLock, antennas, scanDataListener));
		scanLock.unlock();

		assertEquals(2, lastScanResults.size());
		scanResult1 = lastScanResults.get(0);
		scanResult2 = lastScanResults.get(1);
		assertEquals(epc1, DatatypeConverter.printHexBinary(scanResult1.getScanData().getEpc().getUid()));
		assertEquals(epc2, DatatypeConverter.printHexBinary(scanResult2.getScanData().getEpc().getUid()));
		assertEquals(1, scanResult1.getSightings().size());
		assertEquals(1, scanResult2.getSightings().size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStartStopInventoryAsync() throws ParameterException, CommunicationException,
			ImplementationException, ConnectionException, InterruptedException {
		final String epc1 = "300833B2DDD9004433221101";
		final String epc2 = "300833B2DDD9004433221102";

		TagData td1 = new TagData();
		td1.setAntennaID((short) 1);
		td1.setCrc((short) 1234);
		td1.setEpc(RFUtils.hexToBytes(epc1));
		td1.setPc((short) 3400);
		td1.setRssi(-64);

		TagData td2 = new TagData();
		td2.setAntennaID((short) 1);
		td2.setCrc((short) 0x8000); // ushort value
		td2.setEpc(RFUtils.hexToBytes(epc2));
		td2.setPc((short) 0x8001); // ushort value
		td2.setRssi(-64);

		final List<TagData> tags = new ArrayList<>();
		tags.add(td1);
		tags.add(td2);
		final List<TagData> addTags = new ArrayList<>();
		final List<TagData> removeTags = new ArrayList<>();

		RegulatoryCapabilities regCaps = new RegulatoryCapabilities();
		final List<Capabilities> capabilities = new ArrayList<>();
		capabilities.add(regCaps);
		TransmitPowerTableEntry e = new TransmitPowerTableEntry();
		e.setIndex((short) 1);
		e.setTransmitPower((short) 19);
		regCaps.setTransmitPowerTable(new TransmitPowerTable());
		regCaps.getTransmitPowerTable().getEntryList().add(e);

		AntennaConfiguration aCfg = new AntennaConfiguration();
		aCfg.setConnect(ConnectType.TRUE);
		aCfg.setId((short) 1);
		aCfg.setTransmitPower((short) 1);
		final List<AntennaConfiguration> antennaConfigurations = new ArrayList<>();
		antennaConfigurations.add(aCfg);

		InventoryThread it = new InventoryThread(rfDevice);

		new Expectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new Delegate<RFDevice>() {
					@SuppressWarnings("unused")
					List<TagData> execute(List<Short> a, List<Filter> b, List<TagOperation> c) {
						synchronized (tags) {
							// add + remove tags
							tags.addAll(addTags);
							addTags.clear();
							tags.removeAll(removeTags);
							removeTags.clear();
							// slow down the execution
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// return a deep copy of the tags
							List<TagData> tagsCopy = new ArrayList<>();
							for (TagData tag : tags) {
								TagData tagCopy = new TagData();
								tagCopy.setAntennaID(tag.getAntennaID());
								tagCopy.setCrc(tag.getCrc());
								tagCopy.setEpc(Arrays.copyOf(tag.getEpc(), tag.getEpc().length));
								tagCopy.setPc(tag.getPc());
								tagCopy.setRssi(tag.getRssi());
								tagsCopy.add(tagCopy);
							}
							return tagsCopy;
						}
					}
				};

				rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
				result = capabilities;

				rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, (short) 0, (short) 0, (short) 0);
				result = antennaConfigurations;

			}
		};

		ScanSettings scanSettings = new ScanSettings();
		scanSettings.setCycles(10);
		scanSettings.setDataAvailable(false);
		scanSettings.setDuration(0);
		List<Short> antennas = new ArrayList<>();
		antennas.add((short) 0);

		it.setTagSet(TagSet.CURRENT);
		assertTrue(startAndWait(it, scanSettings, antennas));
		Thread.sleep(Environment.EVENT_INTERVAL);
		assertTrue(stopAndWait(it));

		new Verifications() {
			{
				Map<String, Object> eventArgs = null;
				scanEventListener.rfidEventOccured(any, EventType.RFID_SCAN_EVENT, withInstanceOf(Date.class),
						eventArgs = withCapture());
				times = 4; // 2x after first RF execute + 2x at end of inventory
				RfidScanResult scanRes = (RfidScanResult) eventArgs.get("#6042");
				assertEquals(9, scanRes.getSightings().size());
				String epc = RFUtils.bytesToHex(scanRes.getScanData().getEpc().getUid());
				assertTrue(epc.equals(epc1) || epc.equals(epc2));
			}
		};

		scanSettings.setCycles(0);
		tags.clear();
		it.setTagSet(TagSet.ADDITIONS);
		assertTrue(startAndWait(it, scanSettings, antennas));
		Thread.sleep(500);
		synchronized (tags) {
			addTags.add(td1);
		}
		Thread.sleep(500);
		assertTrue(stopAndWait(it));

		new Verifications() {
			{
				Map<String, Object> eventArgs = null;
				scanEventListener.rfidEventOccured(any, EventType.RFID_SCAN_EVENT, withInstanceOf(Date.class),
						eventArgs = withCapture());
				times = 5; // 1x
				RfidScanResult scanRes = (RfidScanResult) eventArgs.get("#6042");
				String epc = RFUtils.bytesToHex(scanRes.getScanData().getEpc().getUid());
				assertEquals(epc1, epc);
			}
		};

		it.setTagSet(TagSet.DELETIONS);

		assertTrue(startAndWait(it, scanSettings, antennas));
		Thread.sleep(500);
		synchronized (tags) {
			removeTags.add(td1);
		}
		Thread.sleep(500);
		assertTrue(stopAndWait(it));

		new Verifications() {
			{
				Map<String, Object> eventArgs = null;
				scanEventListener.rfidEventOccured(withInstanceOf(InventoryThread.class), EventType.RFID_SCAN_EVENT,
						withInstanceOf(Date.class), eventArgs = withCapture());
				times = 6; // 1x
				RfidScanResult scanRes = (RfidScanResult) eventArgs
						.get(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT);
				String epc = RFUtils.bytesToHex(scanRes.getScanData().getEpc().getUid());
				assertEquals(epc1, epc);
			}
		};

		scanSettings.setCycles(0);
		scanSettings.setDuration(100.0);
		scanSettings.setDataAvailable(false);
		assertTrue(startAndWait(it, scanSettings, antennas));
		Thread.sleep(1000);
		assertFalse((boolean) getField(it, "running"));
	}

	private boolean startAndWait(InventoryThread it, ScanSettings scanSettings, List<Short> antennas)
			throws ConnectionException, ImplementationException, InterruptedException {
		synchronized (it) {
			it.startInventoryAsync(scanSettings, antennas, scanDataListener, scanEventListener);
			int retry = 0;
			for (retry = 0; retry < 5; retry++) {
				if ((boolean) getField(it, "running"))
					break;
				Thread.sleep(50);
			}
		}
		return (boolean) getField(it, "running");
	}

	private boolean stopAndWait(InventoryThread it)
			throws ConnectionException, ImplementationException, InterruptedException {
		synchronized (it) {
			it.stopInventoryAsync();
			int retry = 0;
			for (retry = 0; retry < 5; retry++) {
				if (!(boolean) getField(it, "running"))
					break;
				Thread.sleep(50);
			}
		}
		return !(boolean) getField(it, "running");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSuspendResumeInventory() throws ParameterException, CommunicationException, ImplementationException,
			ConnectionException, InterruptedException {

		final String epc1 = "300833B2DDD9004433221101";
		final String epc2 = "300833B2DDD9004433221102";

		TagData td1 = new TagData();
		td1.setAntennaID((short) 1);
		td1.setCrc((short) 1234);
		td1.setEpc(RFUtils.hexToBytes(epc1));
		td1.setPc((short) 3400);
		td1.setRssi(-64);

		TagData td2 = new TagData();
		td2.setAntennaID((short) 1);
		td2.setCrc((short) 0x8000); // ushort value
		td2.setEpc(RFUtils.hexToBytes(epc2));
		td2.setPc((short) 0x8001); // ushort value
		td2.setRssi(-64);

		final List<TagData> tags = new ArrayList<>();
		tags.add(td1);
		tags.add(td2);

		RegulatoryCapabilities regCaps = new RegulatoryCapabilities();
		final List<Capabilities> capabilities = new ArrayList<>();
		capabilities.add(regCaps);
		TransmitPowerTableEntry e = new TransmitPowerTableEntry();
		e.setIndex((short) 1);
		e.setTransmitPower((short) 19);
		regCaps.setTransmitPowerTable(new TransmitPowerTable());
		regCaps.getTransmitPowerTable().getEntryList().add(e);

		AntennaConfiguration aCfg = new AntennaConfiguration();
		aCfg.setConnect(ConnectType.TRUE);
		aCfg.setId((short) 1);
		aCfg.setTransmitPower((short) 1);
		final List<AntennaConfiguration> antennaConfigurations = new ArrayList<>();
		antennaConfigurations.add(aCfg);

		new NonStrictExpectations() {
			{
				rfDevice.execute(withInstanceOf(List.class), withInstanceOf(List.class), withInstanceOf(List.class));
				result = new Delegate<RFDevice>() {
					@SuppressWarnings("unused")
					List<TagData> execute(List<Short> a, List<Filter> b, List<TagOperation> c) {
						synchronized (tags) {
							// slow down the execution
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							return tags;
						}
					}
				};

				rfDevice.getCapabilities(CapabilityType.REGULATORY_CAPABILITIES);
				result = capabilities;

				rfDevice.getConfiguration(ConfigurationType.ANTENNA_CONFIGURATION, (short) 0, (short) 0, (short) 0);
				result = antennaConfigurations;
			}
		};

		final ScanSettings scs = new ScanSettings();
		scs.setCycles(100);
		scs.setDuration(0.0);
		scs.setDataAvailable(false);

		final InventoryThread it = new InventoryThread(rfDevice);

		final List<Short> antennas = new ArrayList<>();
		antennas.add((short) 0);
		final Lock scanLock = new ReentrantLock();

		Thread inventoryWorker = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					scanLock.lock();
					it.startInventory(scs, scanLock, antennas, scanDataListener);
					scanLock.unlock();
				} catch (ConnectionException | ImplementationException e) {
					Assert.fail(e.getMessage());
				}
			}

		}, "inventoryWorker");

		synchronized (it) {
			inventoryWorker.start();
			int retry = 0;
			for (retry = 0; retry < 5; retry++) {
				if ((boolean) getField(it, "running"))
					break;
				Thread.sleep(100);
			}
		}
		assertTrue((boolean) getField(it, "running"));

		it.suspendInventory();
		int cyclesAfterSuspend = getField(it, "cycles");
		Thread.sleep(100);
		int cyclesAfterWait = getField(it, "cycles");
		assertTrue(cyclesAfterSuspend == cyclesAfterWait);
		it.resumeInventory();
		int cyclesAfterResume = getField(it, "cycles");
		Thread.sleep(100);
		cyclesAfterWait = getField(it, "cycles");
		inventoryWorker.join();
		assertTrue(cyclesAfterResume < cyclesAfterWait);
	}

	@Test
	public void testGetError() {
		InventoryThread it = new InventoryThread(rfDevice);
		assertNull(it.getError());
		Exception error = new Exception("foo");
		setField(it, "error", error);
		assertEquals(error, it.getError());
	}
}
