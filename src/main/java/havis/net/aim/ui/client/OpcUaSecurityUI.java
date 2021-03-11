package havis.net.aim.ui.client;

import havis.net.ui.shared.resourcebundle.ResourceBundle;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class OpcUaSecurityUI extends Composite implements EntryPoint {

	@UiField
	ServerSection serverSection;
	@UiField
	SecuritySection securitySection;
	@UiField
	ConfigSection configSection;

	private static OpcUaSecurityUiBinder uiBinder = GWT.create(OpcUaSecurityUiBinder.class);
	private ResourceBundle res = ResourceBundle.INSTANCE;

	interface OpcUaSecurityUiBinder extends UiBinder<Widget, OpcUaSecurityUI> {
	}

	public OpcUaSecurityUI() {
		initWidget(uiBinder.createAndBindUi(this));
		res.css().ensureInjected();
		new SecuritySectionPresenter(securitySection);
		new ServerSectionPresenter(serverSection);
		new ConfigSectionPresenter(configSection);
	}

	@Override
	public void onModuleLoad() {
		RootPanel.get().add(this);
	}
}
