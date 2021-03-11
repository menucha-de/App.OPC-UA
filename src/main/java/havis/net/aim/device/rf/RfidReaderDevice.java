package havis.net.aim.device.rf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import havis.net.aim.opcua.Constants.EventType;
import havis.net.aim.opcua.Constants.Param;
import havis.net.aim.opcua.ParamChangedListener;
import havis.net.aim.opcua.RfidEventListener;
import havis.net.aim.xsd.AntennaNameIdPair;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.CodeTypesType;
import havis.net.aim.xsd.DeviceStatusEnumeration;
import havis.net.aim.xsd.ReadResultPair;
import havis.net.aim.xsd.RfidLockOperationEnumeration;
import havis.net.aim.xsd.RfidLockRegionEnumeration;
import havis.net.aim.xsd.RfidPasswordTypeEnumeration;
import havis.net.aim.xsd.RfidReaderDeviceType;
import havis.net.aim.xsd.RfidScanResultPair;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanSettings;
import havis.net.aim.xsd.TagTypesType;
import havis.opcua.message.exception.ApplicationException;

public abstract class RfidReaderDevice extends RfidReaderDeviceType {

	private final List<ParamChangedListener> paramChangedListeners = new ArrayList<>();
	private final List<RfidEventListener> rfidEventListeners = new ArrayList<>();

	public void addParamChangedListener(ParamChangedListener paramChangedListener) {
		this.paramChangedListeners.add(paramChangedListener);
	}

	public void removeParamChangedListener(ParamChangedListener paramChangedListener) {
		this.paramChangedListeners.remove(paramChangedListener);
	}

	public void addRfidEventListener(RfidEventListener rfidEventListener) {
		this.rfidEventListeners.add(rfidEventListener);
	}

	public void removeRfidEventListener(RfidEventListener rfidEventListener) {
		this.rfidEventListeners.remove(rfidEventListener);
	}

	/**
	 * Gets the RF power in dBm.
	 * <p>
	 * An exception is thrown if multiple antennas are set (
	 * {@link #getAntennaNames()}) and the RF power of the antennas differ. The
	 * RF power for multiple antennas can be set with {@link #setRfPower(byte)}.
	 * </p>
	 * 
	 * @return
	 * @throws ApplicationException
	 */
	public abstract byte getRfPower() throws ApplicationException;

	/**
	 * Sets the RF power in dBm.
	 * <p>
	 * If multiple antennas are set ({@link #getAntennaNames()}) then the RF
	 * power for each of the antennas is set.
	 * </p>
	 * 
	 * @param rfPower
	 * @throws ApplicationException
	 */
	public abstract void setRfPower(byte rfPower) throws ApplicationException;

	/**
	 * Gets the min. RSSI value.
	 * 
	 * @return
	 * @throws ApplicationException
	 */
	public abstract int getMinRssi() throws ApplicationException;

	/**
	 * Sets the min. RSSI value.
	 * 
	 * @param minRssi
	 * @throws ApplicationException
	 */
	public abstract void setMinRssi(int minRssi) throws ApplicationException;

	public abstract HaIOState getHs1() throws ApplicationException;

	public abstract void setHs1(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getHs1Direction() throws ApplicationException;

	public abstract void setHs1Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getHs2() throws ApplicationException;

	public abstract void setHs2(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getHs2Direction() throws ApplicationException;

	public abstract void setHs2Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getHs3() throws ApplicationException;

	public abstract void setHs3(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getHs3Direction() throws ApplicationException;

	public abstract void setHs3Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getHs4() throws ApplicationException;

	public abstract void setHs4(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getHs4Direction() throws ApplicationException;

	public abstract void setHs4Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getSws1Swd1() throws ApplicationException;

	public abstract void setSws1Swd1(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getSws1Swd1Direction() throws ApplicationException;

	public abstract void setSws1Swd1Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getSws2Swd2() throws ApplicationException;

	public abstract void setSws2Swd2(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getSws2Swd2Direction() throws ApplicationException;

	public abstract void setSws2Swd2Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getLs1() throws ApplicationException;

	public abstract void setLs1(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getLs1Direction() throws ApplicationException;

	public abstract void setLs1Direction(HaIODirection direction) throws ApplicationException;

	public abstract HaIOState getLs2() throws ApplicationException;

	public abstract void setLs2(HaIOState state) throws ApplicationException;

	public abstract HaIODirection getLs2Direction() throws ApplicationException;

	public abstract void setLs2Direction(HaIODirection direction) throws ApplicationException;

	/**
	 * This method starts the scan process of the AutoID Device synchronous and
	 * returns the scan results. The duration of the scan process is defined by
	 * the termination conditions in the Settings parameter. A Client shall not
	 * set all parameters to infinite for the Scan Method.
	 * 
	 * @param settings
	 *            Configuration settings for the scan execution
	 * 
	 * @return Results of the scan execution and the the status of the scan
	 *         operation.
	 * 
	 * @throws {@link
	 *             ApplicationException} if a scan is already ongoing or the
	 *             provided settings are invalid.
	 */
	public abstract RfidScanResultPair scan(ScanSettings settings) throws ApplicationException;

	/**
	 * This method starts the scan process of the AutoID Device asynchronous.
	 * The scan results are delivered through Events where the EventType is a
	 * subtype of the AutoIdScanEventType defined in 7.2. There is a subtype
	 * defined for each concrete AutoID Device types. The scan process is
	 * stopped through the method {@link #scanStop()} or if one of the
	 * termination conditions in the Settings parameter is fulfilled.
	 * 
	 * @param settings
	 *            Configuration settings for the scan execution
	 * 
	 * @return the status of the scan start operation.
	 * 
	 * @throws {@link
	 *             ApplicationException} if a scan is already active or the
	 *             provided settings are invalid.
	 */
	public abstract AutoIdOperationStatusEnumeration scanStart(ScanSettings settings) throws ApplicationException;

	/**
	 * This method stops an active scan process of the AutoID Device.
	 * 
	 * @return true if the scan operation is success stopped, false otherwise.
	 * 
	 * @throws {@link
	 *             ApplicationException} if no scan is currently active.
	 */
	public abstract boolean scanStop() throws ApplicationException;

	/**
	 * This method reads a specified area from a tag memory.
	 * 
	 * @param identifier
	 *            AutoID Identifier according to the device configuration as
	 *            returned as part of a ScanResult in a scan event or scan
	 *            method. If the ScanData is used as returned in the ScanResult,
	 *            the structure may contain information that must be ignored by
	 *            the AutoID Device. An example is the ScanDataEpc where only
	 *            the parameter UId is relevant for this Method. If the
	 *            Identifier is provided from a different source than the
	 *            ScanResult, a ScanData with a ByteString can be used to pass a
	 *            UId where the CodeType is set to 'UId'.
	 * 
	 * @param codeType
	 *            Defines the format of the ScanData in the Identifier as
	 *            string.
	 * 
	 * @param region
	 *            Region of the memory area to be accessed. If there is no bank
	 *            available this value is set to 0. This is the bank for UHF
	 *            (ISO/IEC 18000-63) or the bank (ISO/IEC 18000-3 Mode 3) or
	 *            data bank (ISO/IEC 18000-3 Mode 1) for HF or memory area
	 *            (ISO/IEC 18000-2) for LF.
	 * 
	 * @param offset
	 *            Start address of the memory area [byte counting]
	 * 
	 * @param length
	 *            Length of the memory area [byte counting]
	 * 
	 * @param password
	 *            Password for read operation (if required)
	 * 
	 * @return Returns the requested tag data and the status of the read
	 *         operation.
	 */
	public abstract ReadResultPair readTag(ScanData identifier, CodeTypeEnumeration codeType, int region, long offset,
			long length, byte[] password);

	/**
	 * This method writes data to a RFID tag.
	 * 
	 * @param identifier
	 *            AutoID Identifier according to the device configuration as
	 *            returned as part of a ScanResult in a scan event or scan
	 *            method. If the ScanData is used as returned in the ScanResult,
	 *            the structure may contain information that must be ignored by
	 *            the AutoID Device. An example is the ScanDataEpc where only
	 *            the parameter UId is relevant for this Method. If the
	 *            Identifier is provided from a different source than the
	 *            ScanResult, a ScanData with a ByteString can be used to pass a
	 *            UId where the CodeType is set to 'UId'.
	 * 
	 * @param codeType
	 *            Defines the format of the ScanData in the Identifier as
	 *            string.
	 * 
	 * @param region
	 *            Region of the memory area to be accessed. If there is no bank
	 *            available this value is set to 0. This is the bank for UHF
	 *            (ISO/IEC 18000-63) or the bank (ISO/IEC 18000-3 Mode 3) or
	 *            data bank (ISO/IEC 18000-3 Mode 1) for HF or memory area
	 *            (ISO/IEC 18000-2) for LF.
	 * 
	 * @param offset
	 *            Start address of the memory area [byte counting]
	 * 
	 * @param data
	 *            Data to be written.
	 * 
	 * @param password
	 *            Password for the write operation (if required).
	 * 
	 * @return Returns the status of the write operation.
	 */
	public abstract AutoIdOperationStatusEnumeration writeTag(ScanData identifier, CodeTypeEnumeration codeType,
			int region, long offset, byte[] data, byte[] password);

	/**
	 * This method is used to protect specific areas of the transponder memory
	 * against read and/or write access. If a user wants to access such an area,
	 * an access password is required.
	 * 
	 * @param identifier
	 *            AutoID Identifier according to the device configuration as
	 *            returned as part of a ScanResult in a scan event or scan
	 *            method. If the ScanData is used as returned in the ScanResult,
	 *            the structure may contain information that must be ignored by
	 *            the AutoID Device. An example is the ScanDataEpc where only
	 *            the parameter UId is relevant for this Method. If the
	 *            Identifier is provided from a different source than the
	 *            ScanResult, a ScanData with a ByteString can be used to pass a
	 *            UId where the CodeType is set to 'UId'.
	 * @param password
	 *            Transponder (access) password
	 * @param region
	 *            Bank of the memory area to be accessed
	 * @param lock
	 *            Specifies the lock action like write/read protection,
	 *            permanently.
	 * @param offset
	 *            Start address of the memory area [byte counting]
	 * @param length
	 *            Length of the memory area [byte counting]
	 * @param codeType
	 *            Defines the format of the ScanData in the Identifier as
	 *            string.
	 * @return Returns the result of the LOCK operation.
	 */
	public abstract AutoIdOperationStatusEnumeration lockTag(ScanData identifier, CodeTypeEnumeration codeType,
			byte[] password, RfidLockRegionEnumeration region, RfidLockOperationEnumeration lock, long offset,
			long length);

	/**
	 * This method changes the password for a specific transponder.
	 * 
	 * @param identifier
	 *            AutoID Identifier according to the device configuration as
	 *            returned as part of a ScanResult in a scan event or scan
	 *            method. If the ScanData is used as returned in the ScanResult,
	 *            the structure may contain information that must be ignored by
	 *            the AutoID Device. An example is the ScanDataEpc where only
	 *            the parameter UId is relevant for this Method. If the
	 *            Identifier is provided from a different source than the
	 *            ScanResult, a ScanData with a ByteString can be used to pass a
	 *            UId where the CodeType is set to 'UId'.
	 * 
	 * @param codeType
	 *            Defines the format of the ScanData in the Identifier as
	 *            string.
	 * 
	 * @param passwordType
	 *            Defines the operations for which the password is valid
	 * 
	 * @param accessPassword
	 *            The old password
	 * 
	 * @param newPassword
	 *            Gives the new password to the transponder
	 * 
	 * @return Returns the result of the TagPassword method.
	 */
	public abstract AutoIdOperationStatusEnumeration setTagPassword(ScanData identifier, CodeTypeEnumeration codeType,
			RfidPasswordTypeEnumeration passwordType, byte[] accessPassword, byte[] newPassword);

	/**
	 * This method kills the transponder.
	 * 
	 * @param identifier
	 *            AutoID Identifier according to the device configuration as
	 *            returned as part of a ScanResult in a scan event or scan
	 *            method. If the ScanData is used as returned in the ScanResult,
	 *            the structure may contain information that must be ignored by
	 *            the AutoID Device. An example is the ScanDataEpc where only
	 *            the parameter UId is relevant for this Method. If the
	 *            Identifier is provided from a different source than the
	 *            ScanResult, a ScanData with a ByteString can be used to pass a
	 *            UId where the CodeType is set to 'UId'.
	 * @param codeType
	 *            Defines the format of the ScanData in the Identifier as
	 *            string.
	 * @param killPassword
	 *            The kill password
	 * @return Returns the result of the TagPassword method.
	 */
	public abstract AutoIdOperationStatusEnumeration killTag(ScanData identifier, CodeTypeEnumeration codeType,
			byte[] killPassword);

	public void setAntennaNames(List<AntennaNameIdPair> list) {
		super.getAntennaNames().clear();
		super.getAntennaNames().addAll(list);
		notifyParamChange(Param.ANTENNA_NAMES, list);
	}

	@Override
	public void setAutoIdModelVersion(String autoIdModelVersion) {
		super.setAutoIdModelVersion(autoIdModelVersion);
		notifyParamChange(Param.AUTOID_MODEL_VERSION, autoIdModelVersion);
	}

	@Override
	public void setDeviceInfo(String deviceInfo) {
		super.setDeviceInfo(deviceInfo);
		notifyParamChange(Param.DEVICE_INFO, deviceInfo);
	}

	@Override
	public void setDeviceManual(String deviceManual) {
		super.setDeviceManual(deviceManual);
		notifyParamChange(Param.DEVICE_MANUAL, deviceManual);
	}

	@Override
	public void setDeviceName(String deviceName) {
		super.setDeviceName(deviceName);
		notifyParamChange(Param.DEVICE_NAME, deviceName);
	}

	@Override
	public void setDeviceRevision(String deviceRevision) {
		super.setDeviceRevision(deviceRevision);
		notifyParamChange(Param.DEVICE_REVISION, deviceRevision);
	}

	@Override
	public void setDeviceStatus(DeviceStatusEnumeration deviceStatus) {
		super.setDeviceStatus(deviceStatus);
		notifyParamChange(Param.DEVICE_STATUS, deviceStatus);
	}

	@Override
	public void setHardwareRevision(String hardwareRevision) {
		super.setHardwareRevision(hardwareRevision);
		notifyParamChange(Param.HARDWARE_REVISION, hardwareRevision);
	}

	@Override
	public void setLastScanData(ScanData lastScanData) {
		super.setLastScanData(lastScanData);
		notifyParamChange(Param.LAST_SCAN_DATA, lastScanData);
	}

	@Override
	public void setManufacturer(String manufacturer) {
		super.setManufacturer(manufacturer);
		notifyParamChange(Param.MANUFACTURER, manufacturer);
	}

	@Override
	public void setModel(String model) {
		super.setModel(model);
		notifyParamChange(Param.MODEL, model);
	}

	@Override
	public void setRevisionCounter(int revisionCounter) {
		super.setRevisionCounter(revisionCounter);
		notifyParamChange(Param.REVISION_COUNTER, revisionCounter);
	}

	@Override
	public void setSerialNumber(String serialNumber) {
		super.setSerialNumber(serialNumber);
		notifyParamChange(Param.SERIAL_NUMBER, serialNumber);
	}

	// runtime parameters

	@Override
	public void setSoftwareRevision(String softwareRevision) {
		super.setSoftwareRevision(softwareRevision);
		notifyParamChange(Param.SOFTWARE_REVISION, softwareRevision);
	}

	@Override
	public void setTagTypes(TagTypesType tagTypes) {
		super.setTagTypes(tagTypes);
		notifyParamChange(Param.TAG_TYPES, tagTypes);
	}

	@Override
	public void setCodeTypes(CodeTypesType codeTypes) {
		super.setCodeTypes(codeTypes);
		notifyParamChange(Param.CODE_TYPES, codeTypes);
	}

	protected void notifyParamChange(Param param, Object newValue) {
		for (ParamChangedListener paramChangedListener : this.paramChangedListeners)
			paramChangedListener.paramChanged(this, param, newValue);
	}

	protected void notifyRfidEvent(EventType event, Date timeStamp, Map<String, Object> eventArgs) {
		for (RfidEventListener rfidEventListener : this.rfidEventListeners)
			rfidEventListener.rfidEventOccured(this, event, timeStamp, eventArgs);
	}

}
