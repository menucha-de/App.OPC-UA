package havis.net.aim.device.rf;

import havis.device.rf.exception.CommunicationException;
import havis.device.rf.exception.ConnectionException;
import havis.device.rf.exception.ImplementationException;
import havis.device.rf.exception.ParameterException;
import havis.device.rf.tag.result.KillResult;
import havis.device.rf.tag.result.LockResult;
import havis.device.rf.tag.result.ReadResult;
import havis.device.rf.tag.result.WriteResult;
import havis.net.aim.xsd.AutoIdOperationStatusEnumeration;

public class Mapper {

	protected static AutoIdOperationStatusEnumeration mapReadResultToStatus(ReadResult.Result res) {
		switch(res) {
			case INCORRECT_PASSWORD_ERROR:
				return AutoIdOperationStatusEnumeration.PASSWORD_ERROR;			
				
			case MEMORY_LOCKED_ERROR:
				return AutoIdOperationStatusEnumeration.PERMISSON_ERROR;
				
			case MEMORY_OVERRUN_ERROR:
				return AutoIdOperationStatusEnumeration.OUT_OF_RANGE_ERROR;
							
			case NO_RESPONSE_FROM_TAG:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;
				
			case NON_SPECIFIC_READER_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
				
			case NON_SPECIFIC_TAG_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;			
				
			case SUCCESS:
				return AutoIdOperationStatusEnumeration.SUCCESS;
		}
		return null;
	}
	
	protected static AutoIdOperationStatusEnumeration mapWriteResultToStatus(WriteResult.Result res) {
		switch(res) {
			case SUCCESS:
				return AutoIdOperationStatusEnumeration.SUCCESS;
				
			case INCORRECT_PASSWORD_ERROR:
				return AutoIdOperationStatusEnumeration.PASSWORD_ERROR;
				
			case MEMORY_LOCKED_ERROR:
				return AutoIdOperationStatusEnumeration.PERMISSON_ERROR;
				
			case MEMORY_OVERRUN_ERROR:
				return AutoIdOperationStatusEnumeration.OUT_OF_RANGE_ERROR;
				
			case NO_RESPONSE_FROM_TAG:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;
				
			case NON_SPECIFIC_READER_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
									
			case NON_SPECIFIC_TAG_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
			
			case INSUFFICIENT_POWER:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;
		}
		return null;
	}
	
	protected static AutoIdOperationStatusEnumeration mapLockResultToStatus(LockResult.Result res) {
		switch(res) {
			case SUCCESS:
				return AutoIdOperationStatusEnumeration.SUCCESS;
				
			case INCORRECT_PASSWORD_ERROR:
				return AutoIdOperationStatusEnumeration.PASSWORD_ERROR;
				
			case MEMORY_LOCKED_ERROR:
				return AutoIdOperationStatusEnumeration.PERMISSON_ERROR;
				
			case MEMORY_OVERRUN_ERROR:
				return AutoIdOperationStatusEnumeration.OUT_OF_RANGE_ERROR;
				
			case NO_RESPONSE_FROM_TAG:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;
				
			case NON_SPECIFIC_READER_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
									
			case NON_SPECIFIC_TAG_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
			
			case INSUFFICIENT_POWER:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;			
		}
		return null;
	}	
	
	protected static AutoIdOperationStatusEnumeration mapKillResultToStatus(KillResult.Result res) {
		switch(res) {
			case SUCCESS:
				return AutoIdOperationStatusEnumeration.SUCCESS;
				
			case INCORRECT_PASSWORD_ERROR:
				return AutoIdOperationStatusEnumeration.PASSWORD_ERROR;
				
			case NO_RESPONSE_FROM_TAG:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;
				
			case NON_SPECIFIC_READER_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
									
			case NON_SPECIFIC_TAG_ERROR:
				return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
			
			case INSUFFICIENT_POWER:
				return AutoIdOperationStatusEnumeration.TAG_HAS_LOW_BATTERY;
			
			case ZERO_KILL_PASSWORD_ERROR:
				return AutoIdOperationStatusEnumeration.PERMISSON_ERROR;
		}
		return null;
	}
	
	protected static AutoIdOperationStatusEnumeration mapScanError(Throwable error) {
		if (error instanceof ParameterException) { 
			return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
		}
		else if (error instanceof CommunicationException) { 
			return AutoIdOperationStatusEnumeration.RF_COMMUNICATION_ERROR;
		}
		else if (error instanceof ConnectionException) { 
			return AutoIdOperationStatusEnumeration.DEVICE_NOT_READY;
		}
		else if (error instanceof ImplementationException) { 
			return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
		}
		
		return AutoIdOperationStatusEnumeration.MISC_ERROR_TOTAL;
	}



	
}
