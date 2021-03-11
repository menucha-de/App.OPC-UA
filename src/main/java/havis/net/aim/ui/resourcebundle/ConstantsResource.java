package havis.net.aim.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Messages;

public interface ConstantsResource extends Messages {
	
	static final ConstantsResource INSTANCE = GWT.create(ConstantsResource.class);

	String clientVerification();
	String export();
	String insecure();
	String opcForRFID();
	String publicServerCert();
	String privateServerCert();
	String tagSet();
}
