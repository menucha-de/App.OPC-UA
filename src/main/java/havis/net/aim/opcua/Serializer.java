package havis.net.aim.opcua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import havis.net.aim.xsd.AntennaNameIdPair;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.CodeTypesType;
import havis.net.aim.xsd.ReadResultPair;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.RfidScanResultPair;
import havis.net.aim.xsd.RfidSighting;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.TagTypeEnumeration;
import havis.net.aim.xsd.TagTypesType;

public class Serializer {
	public static Map<String, Object>[] serialize(List<AntennaNameIdPair> antennaNames) {		
		
		@SuppressWarnings("unchecked")
		Map<String, Object>[] result = new Map[antennaNames.size()];
		
		for (int i = 0; i < antennaNames.size(); i++) {
			Map<String, Object> antMap = new HashMap<>();
			antMap.put("@id", Environment.ANTENNANAMEIDPAIR_ID + i);
			antMap.put(Environment.ANTENNANAMEIDPAIR_ANTENNAID, antennaNames.get(i).getAntennaId());
			antMap.put(Environment.ANTENNANAMEIDPAIR_ANTENNANAME, antennaNames.get(i).getAntennaName());					
			result[i] = antMap;					
		}
		
		return result;
	}

	public static Object[] serialize(RfidScanResultPair rscp) {		
		Object[] ret = new Object[2];		
		@SuppressWarnings("unchecked")
		Map<String, Object>[] arrayOfResults = new HashMap[rscp.getResults().size()];
		
		for (int iRfidScanResult = 0; iRfidScanResult < rscp.getResults().size(); iRfidScanResult++) {
			
			RfidScanResult result =  rscp.getResults().get(iRfidScanResult);			
			arrayOfResults[iRfidScanResult] = serialize(result, iRfidScanResult);
			
		}
		
		ret[0] = arrayOfResults;
		ret[1] = rscp.getStatus().ordinal();
		return ret;
	}

	public static Map<String, Object> serialize(RfidScanResult result) {
		return serialize(result, 0); 
	}
	
	public static Map<String, Object> serialize(RfidScanResult result, int iRfidScanResult) {
		Map<String, Object> resultMap = new HashMap<>();			
		resultMap.put("@id", Environment.RFIDSCANRESULT_ID + iRfidScanResult);
		
		@SuppressWarnings("unchecked")
		Map<String, Object>[] arrayOfSightings = new HashMap[result.getSightings().size()];
					
		for (int iSighting = 0; iSighting < result.getSightings().size(); iSighting++) {
			RfidSighting sighting = result.getSightings().get(iSighting);				
			Map<String, Object> sightingMap = new HashMap<>();
			sightingMap.put("@id", Environment.RFIDSIGHTING_ID + iRfidScanResult + "_" + iSighting);
			sightingMap.put(Environment.RFIDSIGHTING_ANTENNA, sighting.getAntennaId());
			sightingMap.put(Environment.RFIDSIGHTING_STRENGTH, sighting.getStrength());
			sightingMap.put(Environment.RFIDSIGHTING_TIMESTAMP, toWindowsTimestamp(sighting.getTimestamp()));
			sightingMap.put(Environment.RFIDSIGHTING_CURRENTPOWERLEVEL, sighting.getCurrentPowerLevel());				
			arrayOfSightings[iSighting] = sightingMap;					
		}
		
		resultMap.put(Environment.RFIDSCANRESULT_SIGHTING, arrayOfSightings);
		resultMap.put(Environment.RFIDSCANRESULT_CODETYPE, result.getCodeType().name());
		
		Map<String, Object> scanDataMap = new HashMap<>();
		scanDataMap.put("@id", Environment.SCANDATA_ID + iRfidScanResult);
		
		if (result.getScanData().getByteString() != null)
			scanDataMap.put(Environment.SCANDATA_BYTESTRING, boxByteArray(result.getScanData().getByteString()));				
		
		if (result.getScanData().getString() != null)
			scanDataMap.put(Environment.SCANDATA_STRING, result.getScanData().getString());
					
		if (result.getScanData().getEpc() != null) {
			Map<String, Object> scanDataEpcMap = new HashMap<>();
			scanDataEpcMap.put("@id", Environment.SCANDATAEPC_ID  + iRfidScanResult);
			scanDataEpcMap.put(Environment.SCANDATAEPC_PC, result.getScanData().getEpc().getPc());
			scanDataEpcMap.put(Environment.SCANDATAEPC_UID, boxByteArray(result.getScanData().getEpc().getUid()));
			scanDataEpcMap.put(Environment.SCANDATAEPC_XPC_W1, 0);
			scanDataEpcMap.put(Environment.SCANDATAEPC_XPC_W2, 0);
			scanDataMap.put(Environment.SCANDATA_EPC, scanDataEpcMap);
		}
		
		resultMap.put(Environment.RFIDSCANRESULT_SCANDATA, scanDataMap);
		resultMap.put(Environment.RFIDSCANRESULT_TIMESTAMP, toWindowsTimestamp(result.getTimeStamp()));
		
		return resultMap;
	}
	
	
	/* milliseconds between 1/1/1601 and 1/1/1970 */
	private static final long ERA_OFFSET = 11644473600L * 1000L;
	
	private static long toWindowsTimestamp(long utcInMillies) {
		/* 
		 * Windows time stamp := number of 100 nanosecs from 1/1/1601
		 * Formula: ([UTC-Time in millisecs] + [(1/1/1970 - 1/1/1601) in millisecs]) x [1000 -> microsecs] x [1000 -> nanosecs] / [100 -> 100th nanos] 
		 * */
		return (utcInMillies + ERA_OFFSET) * 10000;		
	}
	
	public static Object[] serialize(ReadResultPair rrp) {		
		Byte[] readData = rrp.getResultData() == null ? new Byte[] {} : new Byte[rrp.getResultData().length];
		
		for (int i = 0; i < readData.length; i++)
			readData[i] = rrp.getResultData()[i];
		
		return new Object[] { readData, rrp.getStatus().ordinal() };		
	}

	public static Object[] serialize(AutoIdOperationStatusEnumeration status) {
		return new Object[] { status.ordinal() };
	}

	public static Object serialize(ScanData lastScanData) {
		
		Map<String, Object> scanDataMap = new HashMap<>();
		scanDataMap.put("@id", Environment.LASTSCANDATA_ID + "0");
		
		if (lastScanData != null && lastScanData.getByteString() != null)
			scanDataMap.put(Environment.SCANDATA_BYTESTRING, boxByteArray(lastScanData.getByteString()));				
		
		if (lastScanData != null && lastScanData.getString() != null)
			scanDataMap.put(Environment.SCANDATA_STRING, lastScanData.getString());
					
		if (lastScanData != null && lastScanData.getEpc() != null) {
			Map<String, Object> scanDataEpcMap = new HashMap<>();
			scanDataEpcMap.put("@id", Environment.SCANDATAEPC_ID + "0");
			scanDataEpcMap.put(Environment.SCANDATAEPC_PC, lastScanData.getEpc().getPc());
			scanDataEpcMap.put(Environment.SCANDATAEPC_UID, boxByteArray(lastScanData.getEpc().getUid()));
			scanDataEpcMap.put(Environment.SCANDATAEPC_XPC_W1, 0);
			scanDataEpcMap.put(Environment.SCANDATAEPC_XPC_W2, 0);
			scanDataMap.put(Environment.SCANDATA_EPC, scanDataEpcMap);
		}
		
		return scanDataMap;
	}
	
	public static Long[] serialize(CodeTypesType codeTypes) {
		List<Long> codeTypeIndices = new ArrayList<>();
		for (CodeTypeEnumeration codeType : codeTypes.getCodeType()) {
			codeTypeIndices.add(Long.valueOf(codeType.ordinal()));
		}
		return codeTypeIndices.toArray(new Long[codeTypeIndices.size()]);
	}
	
	public static String[] serialize(CodeTypeEnumeration[] values) {
		String[] ret = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			ret[i] = values[i].value();
		}
		return ret;
	}
	
	public static Long[] serialize(TagTypesType tagTypes) {
		List<Long> tagTypeIndices = new ArrayList<>();
		for (TagTypeEnumeration tagType : tagTypes.getTagType()) {
			tagTypeIndices.add(Long.valueOf(tagType.ordinal()));
		}
		return tagTypeIndices.toArray(new Long[tagTypeIndices.size()]);
	}
	
	public static String[] serialize(TagTypeEnumeration[] values) {
		String[] ret = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			ret[i] = values[i].value();
		}
		return ret;
	}	
	
	public static Byte[] boxByteArray(byte[] bytes) {
		Byte[] ret = new Byte[bytes.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = bytes[i];
		return ret;
	}
}
