package havis.net.aim.opcua;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import havis.net.aim.device.rf.HaIODirection;
import havis.net.aim.device.rf.HaIOState;
import havis.net.aim.xsd.AntennaNameIdPair;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.CodeTypesType;
import havis.net.aim.xsd.RfidLockOperationEnumeration;
import havis.net.aim.xsd.RfidLockRegionEnumeration;
import havis.net.aim.xsd.RfidPasswordTypeEnumeration;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanDataEpc;
import havis.net.aim.xsd.ScanSettings;
import havis.net.aim.xsd.TagTypeEnumeration;
import havis.net.aim.xsd.TagTypesType;

public class Deserializer {

	public static ScanSettings deserializeScanSettings(Object object) {
		if (!(object instanceof Map)) {
			// TODO:
			return null;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> scanSettingsMap = (Map<String, Object>) object;

		ScanSettings scs = new ScanSettings();
		if (scanSettingsMap.containsKey(Environment.SCANSETTINGS_CYCLES))
			scs.setCycles((int) scanSettingsMap.get(Environment.SCANSETTINGS_CYCLES));

		if (scanSettingsMap.containsKey(Environment.SCANSETTINGS_DATAAVAILABLE))
			scs.setDataAvailable((boolean) scanSettingsMap.get(Environment.SCANSETTINGS_DATAAVAILABLE));

		if (scanSettingsMap.containsKey(Environment.SCANSETTINGS_DURATION))
			scs.setDuration((double) scanSettingsMap.get(Environment.SCANSETTINGS_DURATION));

		return scs;
	}

	public static ScanData deserializeScanData(Object object) {
		@SuppressWarnings("unchecked")
		Map<String, Object> scanDataMap = (Map<String, Object>) object;

		ScanData scd = new ScanData();
		if (scanDataMap.containsKey(Environment.SCANDATA_BYTESTRING))
			scd.setByteString(unboxByteArray((Byte[]) scanDataMap.get(Environment.SCANDATA_BYTESTRING)));

		if (scanDataMap.containsKey(Environment.SCANDATA_STRING))
			scd.setString((String) (Environment.SCANDATA_STRING));

		if (scanDataMap.containsKey(Environment.SCANDATA_EPC)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> scanDataEpcMap = (Map<String, Object>) scanDataMap.get(Environment.SCANDATA_EPC);

			ScanDataEpc sde = new ScanDataEpc();
			if (scanDataEpcMap.containsKey(Environment.SCANDATAEPC_PC))
				sde.setPc((int) scanDataEpcMap.get(Environment.SCANDATAEPC_PC));

			if (scanDataEpcMap.containsKey(Environment.SCANDATAEPC_UID))
				sde.setUid(unboxByteArray((Byte[]) scanDataEpcMap.get(Environment.SCANDATAEPC_UID)));

			scd.setEpc(sde);
		}
		return scd;
	}

	public static List<AntennaNameIdPair> deserializeAntennaNameList(Object object) {

		@SuppressWarnings("unchecked")
		Map<String, Object>[] antennaMaps = (Map<String, Object>[]) object;

		List<AntennaNameIdPair> ret = new ArrayList<>();

		for (Map<String, Object> antennaMap : antennaMaps) {
			AntennaNameIdPair ant = new AntennaNameIdPair();
			ant.setAntennaId((Integer) antennaMap.get(Environment.ANTENNANAMEIDPAIR_ANTENNAID));
			ant.setAntennaName((String) antennaMap.get(Environment.ANTENNANAMEIDPAIR_ANTENNANAME));
			ret.add(ant);
		}
		return ret;
	}

	public static byte[] unboxByteArray(Byte[] bytes) {
		if (bytes == null)
			return null;
		byte[] ret = new byte[bytes.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = bytes[i];
		return ret;
	}

	public static RfidPasswordTypeEnumeration deserialzePasswordType(int pswType) {
		if (pswType == RfidPasswordTypeEnumeration.KILL.ordinal())
			return RfidPasswordTypeEnumeration.KILL;
		if (pswType == RfidPasswordTypeEnumeration.ACCESS.ordinal())
			return RfidPasswordTypeEnumeration.ACCESS;
		if (pswType == RfidPasswordTypeEnumeration.READ.ordinal())
			return RfidPasswordTypeEnumeration.READ;
		if (pswType == RfidPasswordTypeEnumeration.WRITE.ordinal())
			return RfidPasswordTypeEnumeration.WRITE;
		return null;
	}

	public static RfidLockRegionEnumeration deserialzeLockRegion(int lockReg) {
		if (lockReg == RfidLockRegionEnumeration.KILL.ordinal())
			return RfidLockRegionEnumeration.KILL;
		if (lockReg == RfidLockRegionEnumeration.ACCESS.ordinal())
			return RfidLockRegionEnumeration.ACCESS;
		if (lockReg == RfidLockRegionEnumeration.TID.ordinal())
			return RfidLockRegionEnumeration.TID;
		if (lockReg == RfidLockRegionEnumeration.EPC.ordinal())
			return RfidLockRegionEnumeration.EPC;
		if (lockReg == RfidLockRegionEnumeration.USER.ordinal())
			return RfidLockRegionEnumeration.USER;
		return null;
	}

	public static RfidLockOperationEnumeration deserialzeLockType(int lockOp) {
		if (lockOp == RfidLockOperationEnumeration.LOCK.ordinal())
			return RfidLockOperationEnumeration.LOCK;
		if (lockOp == RfidLockOperationEnumeration.UNLOCK.ordinal())
			return RfidLockOperationEnumeration.UNLOCK;
		if (lockOp == RfidLockOperationEnumeration.PERMANENTLOCK.ordinal())
			return RfidLockOperationEnumeration.PERMANENTLOCK;
		if (lockOp == RfidLockOperationEnumeration.PERMANENTUNLOCK.ordinal())
			return RfidLockOperationEnumeration.PERMANENTUNLOCK;
		return null;
	}

	public static CodeTypesType deserializeCodeTypes(Long[] codeTypeIndices) throws DeserializerException {
		CodeTypesType ctt = new CodeTypesType();
		for (Long codeTypeIndex : codeTypeIndices) {
			if (codeTypeIndex != null) {
				if (codeTypeIndex >= CodeTypeEnumeration.values().length) {
					throw new DeserializerException("Invalid index for code type: " + codeTypeIndex + "/"
							+ CodeTypeEnumeration.values().length);
				}
				ctt.getCodeType().add(CodeTypeEnumeration.values()[codeTypeIndex.intValue()]);
			}
		}
		return ctt;
	}

	public static TagTypesType deserializeTagTypes(Long[] tagTypeIndices) throws DeserializerException {
		TagTypesType ttt = new TagTypesType();
		for (Long tagTypeIndex : tagTypeIndices) {
			if (tagTypeIndex != null) {
				if (tagTypeIndex >= TagTypeEnumeration.values().length) {
					throw new DeserializerException(
							"Invalid index for tag type: " + tagTypeIndex + "/" + TagTypeEnumeration.values().length);
				}
				ttt.getTagType().add(TagTypeEnumeration.values()[tagTypeIndex.intValue()]);
			}
		}
		return ttt;
	}

	public static HaIOState deserializeHaIOState(int state) throws DeserializerException {
		HaIOState s = HaIOState.fromValue(state);
		if (s == null) {
			throw new DeserializerException("Unknown IO state " + state);
		}
		return s;
	}

	public static HaIODirection deserializeHaIODirection(int direction) throws DeserializerException {
		HaIODirection d = HaIODirection.fromValue(direction);
		if (d == null) {
			throw new DeserializerException("Unknown IO direction " + direction);
		}
		return d;
	}
}
