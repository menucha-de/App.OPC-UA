package havis.net.aim.opcua;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import havis.device.rf.common.util.RFUtils;
import havis.net.aim.xsd.AntennaNameIdPair;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.CodeTypesType;
import havis.net.aim.xsd.ReadResultPair;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.RfidScanResultPair;
import havis.net.aim.xsd.RfidSighting;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanDataEpc;
import havis.net.aim.xsd.TagTypeEnumeration;
import havis.net.aim.xsd.TagTypesType;

public class SerializerTest {

	@Test
	public void testSerializeListOfAntennaNameIdPair() {
		AntennaNameIdPair anip1 = new AntennaNameIdPair();
		AntennaNameIdPair anip2 = new AntennaNameIdPair();
		
		anip1.setAntennaId(1);
		anip1.setAntennaName("A1");

		anip2.setAntennaId(2);
		anip2.setAntennaName("A2");
		
		List<AntennaNameIdPair> antList = new ArrayList<>();
		antList.add(anip1);
		antList.add(anip2);
		
		Map<String, Object>[] antMaps = Serializer.serialize(antList);
		
		assertEquals(2, antMaps.length);
		assertEquals("antenna_0",antMaps[0].get("@id"));
		assertEquals("A1",antMaps[0].get("AntennaName"));
		assertEquals(1,antMaps[0].get("AntennaId"));
		assertEquals("antenna_1",antMaps[1].get("@id"));
		assertEquals("A2",antMaps[1].get("AntennaName"));
		assertEquals(2,antMaps[1].get("AntennaId"));		
	}

	
	private RfidScanResult randomResult() {
		RfidScanResult res = new RfidScanResult();
		res.setCodeType(CodeTypeEnumeration.EPC);		
		res.setTimeStamp(new Date().getTime());
		
		ScanData sd = new ScanData();		
		sd.setByteString(
			new byte[] { 
				(byte)((Math.random()*255)+0.5),  
				(byte)((Math.random()*255)+0.5), 
				(byte)((Math.random()*255)+0.5), 
				(byte)((Math.random()*255)+0.5) 
			});
		
		sd.setString(RFUtils.bytesToHex(sd.getByteString()));
		ScanDataEpc epc = new ScanDataEpc();
		epc.setPc((int)(Math.random()*Short.MAX_VALUE));
		epc.setUid( new byte[] { 
			(byte)((Math.random()*255)+0.5),  
			(byte)((Math.random()*255)+0.5), 
			(byte)((Math.random()*255)+0.5), 
			(byte)((Math.random()*255)+0.5) 
		});		
		sd.setEpc(epc);
		res.setScanData(sd);
		
		RfidSighting sght1 = new RfidSighting();
		sght1.setAntennaId((int)(Math.random()+1.5));
		sght1.setCurrentPowerLevel((int)((Math.random()*27)+0.5));
		sght1.setStrength((int)((-Math.random()*64)-0.5));		
		sght1.setTimestamp(new Date().getTime());
		
		RfidSighting sght2 = new RfidSighting();
		sght2.setAntennaId((int)(Math.random()+1.5));
		sght2.setCurrentPowerLevel((int)((Math.random()*27)+0.5));
		sght2.setStrength((int)((-Math.random()*64)-0.5));		
		sght2.setTimestamp(new Date().getTime());
		
		res.getSightings().add(sght1);
		res.getSightings().add(sght2);
				
		return res;		
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testSerializeRfidScanResultPair() {
		
		RfidScanResultPair rsrp = new RfidScanResultPair();
		rsrp.setStatus(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY);
		
		RfidScanResult rsr1 = randomResult();
		RfidScanResult rsr2 = randomResult();
		
		rsrp.getResults().add(rsr1);
		rsrp.getResults().add(rsr2);
		
		Object[] objects = Serializer.serialize(rsrp);
		
		assertEquals(2,  objects.length);
		assertEquals(rsrp.getStatus().ordinal(), objects[1]);
		
		/* RFIDScanResult maps */
		Map[] rsrpMaps = (Map[]) objects[0];
		assertEquals(2, rsrpMaps.length);
				
		/* RFIDScanResult 1 */
		Map<String, Object> rsrpMap1 = rsrpMaps[0];
		assertEquals("EPC", rsrpMap1.get("CodeType"));
		assertEquals((rsr1.getTimeStamp() + 11644473600L * 1000L) * 10000, rsrpMap1.get("Timestamp"));
		
		Map<String, Object> scanDataMap1 = (Map<String, Object>) rsrpMap1.get("ScanData");
		Map<String, Object> scanDataEpcMap1 = (Map<String, Object>) scanDataMap1.get("Epc"); 

		assertEquals(rsr1.getScanData().getString(), scanDataMap1.get("String"));
		
		Byte[] bB = (Byte[])scanDataMap1.get("ByteString");
		byte[] bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		assertArrayEquals(rsr1.getScanData().getByteString(), bb);
		
		assertEquals(rsr1.getScanData().getEpc().getPc(), scanDataEpcMap1.get("PC"));
		
		bB = (Byte[])scanDataEpcMap1.get("UId");
		bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		
		assertArrayEquals(rsr1.getScanData().getEpc().getUid(), bb); 
		
		Map[] rsr1Sightings = (Map[])rsrpMap1.get("Sighting");
		assertEquals(2, rsr1Sightings.length);
		
		Map<String, Object> rsr1Sighting1 = (Map<String, Object>) rsr1Sightings[0];
		Map<String, Object> rsr1Sighting2 = (Map<String, Object>) rsr1Sightings[1];

		assertEquals(rsr1.getSightings().get(0).getAntennaId(), rsr1Sighting1.get("Antenna"));
		assertEquals(rsr1.getSightings().get(0).getCurrentPowerLevel(), rsr1Sighting1.get("CurrentPowerLevel"));
		assertEquals(rsr1.getSightings().get(0).getStrength(), rsr1Sighting1.get("Strength"));
		assertEquals((rsr1.getSightings().get(0).getTimestamp() + 11644473600L * 1000L) * 10000, rsr1Sighting1.get("Timestamp"));
		
		assertEquals(rsr1.getSightings().get(1).getAntennaId(), rsr1Sighting2.get("Antenna"));
		assertEquals(rsr1.getSightings().get(1).getCurrentPowerLevel(), rsr1Sighting2.get("CurrentPowerLevel"));
		assertEquals(rsr1.getSightings().get(1).getStrength(), rsr1Sighting2.get("Strength"));
		assertEquals((rsr1.getSightings().get(1).getTimestamp() + 11644473600L * 1000L) * 10000, rsr1Sighting2.get("Timestamp"));
		
		/* RFIDScanResult 2 */
		Map<String, Object> rsrpMap2 = rsrpMaps[1];
		assertEquals("EPC", rsrpMap2.get("CodeType"));
		assertEquals((rsr2.getTimeStamp() + 11644473600L * 1000L) * 10000, rsrpMap2.get("Timestamp"));
		
		Map<String, Object> scanDataMap2 = (Map<String, Object>) rsrpMap2.get("ScanData");
		Map<String, Object> scanDataEpcMap2 = (Map<String, Object>) scanDataMap2.get("Epc"); 

		assertEquals(rsr2.getScanData().getString(), scanDataMap2.get("String"));
		
		bB = (Byte[])scanDataMap2.get("ByteString");
		bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		assertArrayEquals(rsr2.getScanData().getByteString(), bb);
		
		assertEquals(rsr2.getScanData().getEpc().getPc(), scanDataEpcMap2.get("PC"));
		
		bB = (Byte[])scanDataEpcMap2.get("UId");
		bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		
		assertArrayEquals(rsr2.getScanData().getEpc().getUid(), bb); 
		
		Map[] rsr2Sightings = (Map[])rsrpMap2.get("Sighting");
		assertEquals(2, rsr2Sightings.length);
		
		Map<String, Object> rsr2Sighting1 = (Map<String, Object>) rsr2Sightings[0];
		Map<String, Object> rsr2Sighting2 = (Map<String, Object>) rsr2Sightings[1];

		assertEquals(rsr2.getSightings().get(0).getAntennaId(), rsr2Sighting1.get("Antenna"));
		assertEquals(rsr2.getSightings().get(0).getCurrentPowerLevel(), rsr2Sighting1.get("CurrentPowerLevel"));
		assertEquals(rsr2.getSightings().get(0).getStrength(), rsr2Sighting1.get("Strength"));
		assertEquals((rsr2.getSightings().get(0).getTimestamp() + 11644473600L * 1000L) * 10000, rsr2Sighting1.get("Timestamp"));
		
		assertEquals(rsr2.getSightings().get(1).getAntennaId(), rsr2Sighting2.get("Antenna"));
		assertEquals(rsr2.getSightings().get(1).getCurrentPowerLevel(), rsr2Sighting2.get("CurrentPowerLevel"));
		assertEquals(rsr2.getSightings().get(1).getStrength(), rsr2Sighting2.get("Strength"));
		assertEquals((rsr2.getSightings().get(1).getTimestamp() + 11644473600L * 1000L) * 10000, rsr2Sighting2.get("Timestamp"));
				
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSerializeRfidScanResult() {
		
		RfidScanResult rsr = randomResult();
		
		Map<String, Object> rsrMap = Serializer.serialize(rsr);
		
		assertEquals("EPC", rsrMap.get("CodeType"));
		assertEquals((rsr.getTimeStamp() + 11644473600L * 1000L) * 10000, rsrMap.get("Timestamp"));
		
		Map<String, Object> scanDataMap = (Map<String, Object>) rsrMap.get("ScanData");
		Map<String, Object> scanDataEpcMap = (Map<String, Object>) scanDataMap.get("Epc"); 

		assertEquals(rsr.getScanData().getString(), scanDataMap.get("String"));
		
		Byte[] bB = (Byte[])scanDataMap.get("ByteString");
		byte[] bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		assertArrayEquals(rsr.getScanData().getByteString(), bb);
		
		assertEquals(rsr.getScanData().getEpc().getPc(), scanDataEpcMap.get("PC"));
		
		bB = (Byte[])scanDataEpcMap.get("UId");
		bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		
		assertArrayEquals(rsr.getScanData().getEpc().getUid(), bb); 
		
		@SuppressWarnings("rawtypes")
		Map[] rsrSightings = (Map[])rsrMap.get("Sighting");
		assertEquals(2, rsrSightings.length);
		
		Map<String, Object> rsrSighting1 = (Map<String, Object>) rsrSightings[0];
		Map<String, Object> rsrSighting2 = (Map<String, Object>) rsrSightings[1];

		assertEquals(rsr.getSightings().get(0).getAntennaId(), rsrSighting1.get("Antenna"));
		assertEquals(rsr.getSightings().get(0).getCurrentPowerLevel(), rsrSighting1.get("CurrentPowerLevel"));
		assertEquals(rsr.getSightings().get(0).getStrength(), rsrSighting1.get("Strength"));
		assertEquals((rsr.getSightings().get(0).getTimestamp() + 11644473600L * 1000L) * 10000, rsrSighting1.get("Timestamp"));
		
		assertEquals(rsr.getSightings().get(1).getAntennaId(), rsrSighting2.get("Antenna"));
		assertEquals(rsr.getSightings().get(1).getCurrentPowerLevel(), rsrSighting2.get("CurrentPowerLevel"));
		assertEquals(rsr.getSightings().get(1).getStrength(), rsrSighting2.get("Strength"));
		assertEquals((rsr.getSightings().get(1).getTimestamp() + 11644473600L * 1000L) * 10000, rsrSighting2.get("Timestamp"));
		
		assertEquals("rfidScanResult_0", rsrMap.get("@id"));		
	}

	@Test
	public void testSerializeRfidScanResultInt() {
		RfidScanResult rsr = randomResult();
	
		Map<String, Object> rsrMap = Serializer.serialize(rsr, 42);
		assertEquals("rfidScanResult_42", rsrMap.get("@id"));
	}

	@Test
	public void testSerializeReadResultPair() {
		
		ReadResultPair rrp = new ReadResultPair();
		rrp.setStatus(AutoIdOperationStatusEnumeration.SUCCESS);
		rrp.setResultData(new byte[] { (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd });
		
		Object[] objects = Serializer.serialize(rrp);
		
		Byte[] bB = (Byte[]) objects[0];
		byte[] bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++)
			bb[i] = bB[i];
		
		assertArrayEquals(rrp.getResultData(), bb);
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS.ordinal(), objects[1]);
	}

	@Test
	public void testSerializeAutoIdOperationStatusEnumeration() {		
		for (AutoIdOperationStatusEnumeration e : AutoIdOperationStatusEnumeration.values())
			assertEquals(e.ordinal(), Serializer.serialize(e)[0]);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSerializeScanData() {
		
		ScanData sd = randomResult().getScanData();				
		Map<String, Object> scanDataMap = (Map<String, Object>) Serializer.serialize(sd);
		Map<String, Object> scanDataEpcMap = (Map<String, Object>) scanDataMap.get("Epc"); 

		Byte[] bB = (Byte[])scanDataMap.get("ByteString");
		byte[] bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		assertArrayEquals(sd.getByteString(), bb);
		
		assertEquals(sd.getEpc().getPc(), scanDataEpcMap.get("PC"));
		
		bB = (Byte[])scanDataEpcMap.get("UId");
		bb = new byte[bB.length];
		for (int i = 0; i < bb.length; i++) bb[i] = bB[i];
		
		assertArrayEquals(sd.getEpc().getUid(), bb);
	}

	@Test
	public void testSerializeCodeTypesType() {		
		CodeTypesType ctt = new CodeTypesType();
		ctt.getCodeType().add(CodeTypeEnumeration.EPC);		
		ctt.getCodeType().add(CodeTypeEnumeration.EPC);
		
		Long[] ctts = Serializer.serialize(ctt);		
		assertEquals(2, ctts.length);
		
		assertEquals((long)CodeTypeEnumeration.EPC.ordinal(), (long)ctts[0]);
		assertEquals((long)CodeTypeEnumeration.EPC.ordinal(), (long)ctts[1]);
		
	}

	@Test
	public void testSerializeCodeTypeEnumerationArray() {
		
		CodeTypeEnumeration[] ctes = new CodeTypeEnumeration[] {
			CodeTypeEnumeration.EPC,
			CodeTypeEnumeration.EPC,
			CodeTypeEnumeration.EPC
		};
		
		String[] str = Serializer.serialize(ctes);
		assertEquals(ctes.length, str.length);
		assertEquals(CodeTypeEnumeration.EPC.toString(), str[0]);
		assertEquals(CodeTypeEnumeration.EPC.toString(), str[1]);
		assertEquals(CodeTypeEnumeration.EPC.toString(), str[2]);
				
	}

	@Test
	public void testSerializeTagTypesType() {
		
		TagTypesType ttt = new TagTypesType();
		ttt.getTagType().add(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1);
		ttt.getTagType().add(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1);
		ttt.getTagType().add(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1);
		
		Long[] ttts = Serializer.serialize(ttt);		
		assertEquals(ttt.getTagType().size(), ttts.length);
		
		assertEquals((long)TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.ordinal(), (long)ttts[0]);
		assertEquals((long)TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.ordinal(), (long)ttts[1]);
		assertEquals((long)TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.ordinal(), (long)ttts[2]);					
	}

	@Test
	public void testSerializeTagTypeEnumerationArray() {
		
		TagTypeEnumeration[] tagTypes = new TagTypeEnumeration[] {
				TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1,
				TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1,
				TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1
			};
			
			String[] str = Serializer.serialize(tagTypes);
			assertEquals(tagTypes.length, str.length);
			assertEquals(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.value(), str[0]);
			assertEquals(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.value(), str[1]);
			assertEquals(TagTypeEnumeration.EPC_CLASS_1_GEN_2_V_1.value(), str[2]);		
	}

	@Test
	public void testBoxByteArray() {
		
	}

}
