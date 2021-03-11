package havis.net.aim;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import havis.device.rf.RFDevice;
import havis.device.rf.common.CommunicationHandler;
import havis.device.rf.common.util.RFUtils;
import havis.net.aim.device.rf.AimRfidReaderDevice;
import havis.net.aim.device.rf.RfidReaderDevice;
import havis.net.aim.opcua.AimDataProvider;
import havis.net.aim.opcua.ConfigurationManager;
import havis.net.aim.opcua.Constants.Param;
import havis.net.aim.opcua.Environment;
import havis.net.aim.opcua.Serializer;
import havis.net.aim.xsd.CodeTypeEnumeration;
import havis.net.aim.xsd.DeviceStatusEnumeration;
import havis.net.aim.xsd.RfidScanResult;
import havis.net.aim.xsd.RfidSighting;
import havis.net.aim.xsd.ScanData;
import havis.net.aim.xsd.ScanDataEpc;
import havis.opcua.message.DataProvider;
import havis.opcua.message.MessageHandler;
import havis.opcua.message.common.MessageHandlerCommon;

public class MessageHandlerCLI {
	static Map<String, Object> map = new LinkedHashMap<>();
	static {
		map.put("bool_true", true);
		map.put("bool_false", false);
		map.put("string", "foo bar");
		map.put("byte", (byte) 0xaa);
		map.put("min_short", Short.MIN_VALUE);
		map.put("max_short", Short.MAX_VALUE);
		map.put("min_int", Integer.MIN_VALUE);
		map.put("max_int", Integer.MAX_VALUE);
		map.put("min_long", Long.MIN_VALUE);
		map.put("max_long", Long.MAX_VALUE);
		map.put("min_float", Float.MIN_VALUE);
		map.put("max_float", Float.MAX_VALUE);
		map.put("min_double", Double.MIN_VALUE);
		map.put("max_double", Double.MAX_VALUE);

		map.put("booleans", new Boolean[] { true, false });
		map.put("bytes", new Byte[] { (byte) 0xaa, (byte) 0xbb });
		map.put("shorts", new Short[] { Short.MIN_VALUE, Short.MAX_VALUE });
		map.put("integers", new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE });
		map.put("longs", new Long[] { Long.MIN_VALUE, Long.MAX_VALUE });
		map.put("floats", new Float[] { Float.MIN_VALUE, Float.MAX_VALUE });
		map.put("doubles", new Double[] { Double.MIN_VALUE, Double.MAX_VALUE });
	}

	public static void main(String[] args) {
		try {

			RFDevice rfDev = new CommunicationHandler();
			ConfigurationManager configurationManager = new ConfigurationManager();
			RfidReaderDevice rfidReaderDevice = new AimRfidReaderDevice(rfDev, null /* ioDevice */,
					configurationManager);
			MessageHandler handler = new MessageHandlerCommon();

			DataProvider dp = new AimDataProvider(rfidReaderDevice, handler);
			handler.open(dp);

			try (Scanner sc = new Scanner(System.in)) {
				System.out.println("Type 'q' to quit.");
				while (true) {

					System.out.print("server> ");

					String line = sc.nextLine();
					line = line.replaceAll("\\s+", "");

					if (0 == line.length())
						continue;

					if (line.equals("q")) {
						System.out.println("Stopping message handler...");
						break;
					} else if (line.equals("n"))
						handler.notify(map);

					else if (line.equals("e")) {

						RfidScanResult rsr = new RfidScanResult();
						rsr.setCodeType(CodeTypeEnumeration.EPC);
						rsr.setScanData(new ScanData());
						rsr.getScanData().setEpc(new ScanDataEpc());
						rsr.getScanData().getEpc().setPc(0x0000);
						rsr.getScanData().getEpc().setUid(new byte[] { 0x30, 0x08, 0x33, (byte) 0xb2, (byte) 0xdd,
								(byte) 0xd9, 0x01, 0x40, 0x00, 0x00, 0x00, 0x00 });
						rsr.setTimeStamp(new Date().getTime());
						RfidSighting rs = new RfidSighting();
						rs.setAntennaId(1);
						rs.setStrength(-65);
						rs.setTimestamp(new Date().getTime());
						rs.setCurrentPowerLevel(15);
						rsr.getSightings().add(rs);

						String deviceName = "mica-00";
						String message = deviceName + ":" + RFUtils.bytesToHex(rsr.getScanData().getEpc().getUid());

						Map<String, Object> paramMap = new HashMap<>();
						paramMap.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_DEVICE_NAME, deviceName);
						paramMap.put(Environment.EVENT_RFID_SCAN_EVENT_PARAM_SCAN_RESULT, Serializer.serialize(rsr));

						handler.event(Environment.EVENT_RFID_SCAN_EVENT_TYPE, Environment.INSTANCE_NAME, new Date(),
								Environment.EVENT_RFID_SCAN_EVENT_SEVERITY, message, paramMap);
					}

					else if (line.equals("scan")) {

						HashMap<String, Object> scanSettings = new HashMap<>();
						scanSettings.put("@id", "scanSettings");
						scanSettings.put("Cycles", 25);
						scanSettings.put("DataAvailable", false);
						scanSettings.put("Duration", 10 * 1000.0);
						dp.call("Scan", "rfr310", new Object[] { scanSettings });
					} else if (line.equals("n_scanning")) {
						((AimDataProvider) dp).getSubscriptionManager().paramChanged(MessageHandlerCLI.class,
								Param.DEVICE_STATUS, DeviceStatusEnumeration.SCANNING);
					} else if (line.equals("n_busy")) {
						((AimDataProvider) dp).getSubscriptionManager().paramChanged(MessageHandlerCLI.class,
								Param.DEVICE_STATUS, DeviceStatusEnumeration.BUSY);
					} else if (line.equals("n_idle")) {
						((AimDataProvider) dp).getSubscriptionManager().paramChanged(MessageHandlerCLI.class,
								Param.DEVICE_STATUS, DeviceStatusEnumeration.IDLE);
					}
				}
			}

			handler.close();
			System.out.println("Message handler stopped.");
			System.exit(0);

		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
