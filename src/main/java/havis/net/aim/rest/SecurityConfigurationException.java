package havis.net.aim.rest;

public class SecurityConfigurationException extends Exception {
	private static final long serialVersionUID = -5086313569506187468L;

	public SecurityConfigurationException() {
		super();
	}

	public SecurityConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SecurityConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityConfigurationException(String message) {
		super(message);
	}

	public SecurityConfigurationException(Throwable cause) {
		super(cause);
	}

}
