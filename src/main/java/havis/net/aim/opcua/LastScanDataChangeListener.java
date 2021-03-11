package havis.net.aim.opcua;

import havis.net.aim.xsd.ScanData;

public interface LastScanDataChangeListener {
	void lastScanDataChanged(Object source, ScanData oldScanData, ScanData newScanData);
}
