package havis.net.aim.opcua;

public class DeserializerException extends Exception {
	private static final long serialVersionUID = -3267744999279274225L;
	
	public DeserializerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeserializerException(String message) {
		super(message);
	}
}
