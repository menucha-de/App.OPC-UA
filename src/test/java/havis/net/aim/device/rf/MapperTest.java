package havis.net.aim.device.rf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import havis.device.rf.exception.CommunicationException;
import havis.device.rf.exception.ConnectionException;
import havis.device.rf.exception.ImplementationException;
import havis.device.rf.exception.ParameterException;
import havis.device.rf.tag.result.KillResult;
import havis.device.rf.tag.result.LockResult;
import havis.device.rf.tag.result.ReadResult;
import havis.device.rf.tag.result.WriteResult;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;

public class MapperTest {

	@Test
	public void testMapReadResultToStatus() {
		assertEquals(AutoIdOperationStatusEnumeration.PASSWORD_ERROR, Mapper.mapReadResultToStatus(ReadResult.Result.INCORRECT_PASSWORD_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.PERMISSON_ERROR, Mapper.mapReadResultToStatus(ReadResult.Result.MEMORY_LOCKED_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.OUT_OF_RANGE_ERROR, Mapper.mapReadResultToStatus(ReadResult.Result.MEMORY_OVERRUN_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapReadResultToStatus(ReadResult.Result.NO_RESPONSE_FROM_TAG));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapReadResultToStatus(ReadResult.Result.NON_SPECIFIC_READER_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapReadResultToStatus(ReadResult.Result.NON_SPECIFIC_TAG_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, Mapper.mapReadResultToStatus(ReadResult.Result.SUCCESS));
	}
	
	@Test
	public void testMapWriteResultToStatus() {
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, Mapper.mapWriteResultToStatus(WriteResult.Result.SUCCESS));
		assertEquals(AutoIdOperationStatusEnumeration.PASSWORD_ERROR, Mapper.mapWriteResultToStatus(WriteResult.Result.INCORRECT_PASSWORD_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.PERMISSON_ERROR, Mapper.mapWriteResultToStatus(WriteResult.Result.MEMORY_LOCKED_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.OUT_OF_RANGE_ERROR, Mapper.mapWriteResultToStatus(WriteResult.Result.MEMORY_OVERRUN_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapWriteResultToStatus(WriteResult.Result.NO_RESPONSE_FROM_TAG));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapWriteResultToStatus(WriteResult.Result.NON_SPECIFIC_READER_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapWriteResultToStatus(WriteResult.Result.NON_SPECIFIC_TAG_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapWriteResultToStatus(WriteResult.Result.INSUFFICIENT_POWER));
	}
	
	@Test
	public void testMapLockResultToStatus() {
		assertEquals(AutoIdOperationStatusEnumeration.PASSWORD_ERROR, Mapper.mapLockResultToStatus(LockResult.Result.INCORRECT_PASSWORD_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.PERMISSON_ERROR, Mapper.mapLockResultToStatus(LockResult.Result.MEMORY_LOCKED_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.OUT_OF_RANGE_ERROR, Mapper.mapLockResultToStatus(LockResult.Result.MEMORY_OVERRUN_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapLockResultToStatus(LockResult.Result.NO_RESPONSE_FROM_TAG));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapLockResultToStatus(LockResult.Result.NON_SPECIFIC_READER_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapLockResultToStatus(LockResult.Result.NON_SPECIFIC_TAG_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapLockResultToStatus(LockResult.Result.INSUFFICIENT_POWER));
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, Mapper.mapLockResultToStatus(LockResult.Result.SUCCESS));
	}
	
	@Test
	public void testMapKillResultToStatus() {
		assertEquals(AutoIdOperationStatusEnumeration.SUCCESS, Mapper.mapKillResultToStatus(KillResult.Result.SUCCESS));
		assertEquals(AutoIdOperationStatusEnumeration.PASSWORD_ERROR, Mapper.mapKillResultToStatus(KillResult.Result.INCORRECT_PASSWORD_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapKillResultToStatus(KillResult.Result.NO_RESPONSE_FROM_TAG));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapKillResultToStatus(KillResult.Result.NON_SPECIFIC_READER_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapKillResultToStatus(KillResult.Result.NON_SPECIFIC_TAG_ERROR));
		assertEquals(AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY, Mapper.mapKillResultToStatus(KillResult.Result.INSUFFICIENT_POWER));
		assertEquals(AutoIdOperationStatusEnumeration.PERMISSON_ERROR, Mapper.mapKillResultToStatus(KillResult.Result.ZERO_KILL_PASSWORD_ERROR));		
	}
	
	@Test
	public void testMapScanError() {
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapScanError(new ParameterException()));
		assertEquals(AutoIdOperationStatusEnumeration.RF_COMMUNICATION_ERROR, Mapper.mapScanError(new CommunicationException()));
		assertEquals(AutoIdOperationStatusEnumeration.DEVICE_NOT_READY, Mapper.mapScanError(new ConnectionException()));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapScanError(new ImplementationException()));
		assertEquals(AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL, Mapper.mapScanError(new Exception()));
		
	}
}
