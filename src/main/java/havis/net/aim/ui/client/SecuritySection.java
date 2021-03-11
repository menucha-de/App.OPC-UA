package havis.net.aim.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.aim.ui.resourcebundle.AppResources;
import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.event.MessageEvent;
import havis.net.ui.shared.client.event.MessageEvent.Handler;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.upload.File;
import havis.net.ui.shared.client.upload.FileList;
import havis.net.ui.shared.client.upload.MultipleFileUpload;
import havis.net.ui.shared.client.widgets.CustomMessageWidget;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class SecuritySection extends ConfigurationSection implements SecuritySectionView, MessageEvent.HasHandlers {

	@UiField
	FlowPanel configRows;
	@UiField
	ToggleButton insecure;
	@UiField
	ToggleButton clientVerification;
	@UiField
	Button publicServerCertExport;
	@UiField
	Button publicServerCertImport;
	@UiField
	Button privateServerCertImport;
	@UiField
	MultipleFileUpload upload;
	@UiField
	CertificatesSection trustedCertificatesSection;
	@UiField
	CertificatesSection revocationListsSection;
	@UiField
	CustomMessageWidget message;

	private AppResources appRes = AppResources.INSTANCE;
	private ResourceBundle res = ResourceBundle.INSTANCE;

	private static SecuritySectionUiBinder uiBinder = GWT.create(SecuritySectionUiBinder.class);

	interface SecuritySectionUiBinder extends UiBinder<Widget, SecuritySection> {
	}

	private Presenter presenter;
	private CertificateType uploadType;

	@UiConstructor
	public SecuritySection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
		addMessageEventHandler(new MessageEvent.Handler() {

			@Override
			public void onMessage(MessageEvent event) {
				message.showMessage(event.getMessage(), event.getMessageType());
			}

			@Override
			public void onClear(MessageEvent event) {
				message.showMessage(null, MessageEvent.MessageType.CLEAR);
			}
		});

		appRes.css().ensureInjected();
		res.css().ensureInjected();
	}

	@UiHandler("insecure")
	void onInsecureChange(ValueChangeEvent<Boolean> event) {
		presenter.onInsecureChange(event.getValue());
	}

	@UiHandler("clientVerification")
	void onClientVerificationChange(ValueChangeEvent<Boolean> event) {
		presenter.onClientVerificationChange(event.getValue());
	}

	@UiHandler("upload")
	void onChooseFile(ChangeEvent event) {
		FileList fl = upload.getFileList();
		File file = fl.html5Item(0);
		switch (uploadType) {
		case PRIVATE:
			presenter.onUploadPrivateServerCert(file);
			break;
		case PUBLIC:
			presenter.onUploadPublicServerCert(file);
			break;
		case CERTS:
		case CRL:
			presenter.onUploadCertificate(file, file.getName(), uploadType);
			break;
		default:
			return;
		}
		uploadType = null;
		upload.reset();
	}

	@UiHandler("publicServerCertImport")
	void onPublicServerCertImportClick(ClickEvent event) {
		uploadType = CertificateType.PUBLIC;
		upload.setAccept(".der");
		upload.click();
	}

	@UiHandler("publicServerCertExport")
	void onPublicServerCertExportClick(ClickEvent event) {
		presenter.onDownloadPublicServerCert();
	}

	@UiHandler("privateServerCertImport")
	void onPrivateServerCertImportClick(ClickEvent event) {
		uploadType = CertificateType.PRIVATE;
		upload.setAccept(".pem");
		upload.click();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		trustedCertificatesSection.setPresenter(presenter);
		revocationListsSection.setPresenter(presenter);
	}

	@Override
	public void showMessage(MessageType messageType, String message) {
		fireEvent(new MessageEvent(messageType, message));
	}

	@Override
	public ToggleButton getInsecure() {
		return insecure;
	}

	@Override
	public ToggleButton getClientVerification() {
		return clientVerification;
	}

	@Override
	public Button getPublicServerCertExport() {
		return publicServerCertExport;
	}

	@Override
	public Button getPublicServerCertImport() {
		return publicServerCertImport;
	}

	@Override
	public Button getPrivateServerCertImport() {
		return privateServerCertImport;
	}

	@Override
	public CertificatesSection getCertificatesSection(CertificateType type) {
		switch (type) {
		case CERTS:
			return trustedCertificatesSection;
		case CRL:
			return revocationListsSection;
		default:
			return null;
		}
	}

	@Override
	public HandlerRegistration addMessageEventHandler(Handler handler) {
		return addHandler(handler, MessageEvent.getType());
	}
}
