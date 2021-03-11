package havis.net.aim.device.rf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import havis.device.rf.tag.TagData;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.RfidSighting;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanDataEpc;
import havis.net.aim.xsd.TagSet;

public class InventoryTagProcessor {

	private TagSet tagSet;
	private Map<Short, Short> powerMap;

	private Map<String, RfidScanResult> currentScanResultMap = new HashMap<>();
	private Map<String, RfidScanResult> lastScanResultMap = new HashMap<>();
	private Integer eventMaxSightingCount;
	private Integer totalMaxSightingCount;
	private Map<String, Integer> totalSightingCounts = new HashMap<>();

	public InventoryTagProcessor(TagSet tagSet, Map<Short, Short> powerMap, Integer eventMaxSightingCount,
			Integer totalMaxSightingCount) {
		this.tagSet = tagSet;
		this.powerMap = powerMap;
		this.eventMaxSightingCount = eventMaxSightingCount;
		this.totalMaxSightingCount = totalMaxSightingCount;
	}

	public Map<String, RfidScanResult> getCurrentScanResultMap() {
		return currentScanResultMap;
	}

	public Map<String, RfidScanResult> getLastScanResultMap() {
		return lastScanResultMap;
	}

	public List<RfidScanResult> generateScanEvents() {
		List<RfidScanResult> scanEvents = new ArrayList<>();
		switch (tagSet) {
		case CURRENT:
			scanEvents.addAll(currentScanResultMap.values());
			break;
		case ADDITIONS:
			HashSet<String> tagAdditions = new HashSet<>(currentScanResultMap.keySet());
			tagAdditions.removeAll(lastScanResultMap.keySet());
			for (String tag : tagAdditions) {
				scanEvents.add(currentScanResultMap.get(tag));
			}
			break;
		case DELETIONS:
			HashSet<String> tagDeletions = new HashSet<>(lastScanResultMap.keySet());
			tagDeletions.removeAll(currentScanResultMap.keySet());
			for (String tag : tagDeletions) {
				scanEvents.add(lastScanResultMap.get(tag));
			}
			break;
		}

		lastScanResultMap = currentScanResultMap;
		currentScanResultMap = new HashMap<>();

		return scanEvents;
	}

	public boolean process(List<TagData> tags) {
		if (tags == null || tags.isEmpty()) {
			return false;
		}

		boolean sightingsLimitReached = false;
		for (TagData tag : tags) {
			String epcStr = DatatypeConverter.printHexBinary(tag.getEpc());
			RfidScanResult rsr = currentScanResultMap.get(epcStr);
			if (rsr == null) {
				rsr = new RfidScanResult();
				rsr.setCodeType(CodeTypeEnumeration.EPC);
				rsr.setScanData(new ScanData());
				rsr.getScanData().setEpc(new ScanDataEpc());
				rsr.getScanData().getEpc().setPc(DataTypeConverter.ushort(tag.getPc()));
				rsr.getScanData().getEpc().setUid(tag.getEpc());
				rsr.setTimeStamp(new Date().getTime());
				currentScanResultMap.put(epcStr, rsr);
			}

			RfidSighting rs = new RfidSighting();
			rs.setAntennaId(tag.getAntennaID());
			rs.setStrength(tag.getRssi());
			rs.setTimestamp(new Date().getTime());

			rs.setCurrentPowerLevel(powerMap.get(tag.getAntennaID()));
			rsr.getSightings().add(rs);

			if (totalMaxSightingCount != null) {
				Integer tsc = totalSightingCounts.get(epcStr);
				if (tsc == null) {
					tsc = 0;
				}
				tsc += rsr.getSightings().size();
				totalSightingCounts.put(epcStr, tsc);
				if (tsc >= totalMaxSightingCount) {
					sightingsLimitReached = true;
				}
			}
			if (eventMaxSightingCount != null && rsr.getSightings().size() >= eventMaxSightingCount) {
				sightingsLimitReached = true;
			}
		}
		return sightingsLimitReached;
	}
}
