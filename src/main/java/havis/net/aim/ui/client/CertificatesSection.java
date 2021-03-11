package havis.net.aim.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.aim.ui.client.SecuritySectionView.Presenter;
import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.upload.File;
import havis.net.ui.shared.client.upload.FileList;
import havis.net.ui.shared.client.upload.MultipleFileUpload;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class CertificatesSection extends ConfigurationSection {

	@UiField
	FlowPanel certificatesList;
	@UiField
	FocusPanel focus;
	@UiField
	Label refreshButton;
	@UiField
	Label addButton;
	@UiField
	Label importButton;
	@UiField
	ToggleButton extend;
	@UiField
	MultipleFileUpload upload;

	private static TrustedCertificatesSectionUiBinder uiBinder = GWT.create(TrustedCertificatesSectionUiBinder.class);
	private ResourceBundle res = ResourceBundle.INSTANCE;
	private Presenter presenter;
	Map<String, CertificateItem> certificates = new HashMap<>();
	private CertificateType type;
	private boolean importEnabled = false;
	private boolean exportEnabled = false;

	interface TrustedCertificatesSectionUiBinder extends UiBinder<Widget, CertificatesSection> {
	}

	@UiConstructor
	public CertificatesSection(String name, CertificateType type) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
		this.type = type;
		upload.setAccept(type.getExtension());
	}

	@UiHandler("addButton")
	void onAdd(ClickEvent event) {
		uploadCertificate();
	}

	@UiHandler("importButton")
	void onImport(ClickEvent event) {
		uploadCertificate();
	}

	private void uploadCertificate() {
		if (importEnabled) {
			upload.click();
		}
	}

	@UiHandler("upload")
	void onChooseFile(ChangeEvent event) {
		FileList fl = upload.getFileList();
		File file = fl.html5Item(0);
		presenter.onUploadCertificate(file, file.getName(), type);
		upload.reset();
	}

	@UiHandler("focus")
	void onMouseOver(MouseOverEvent event) {
		extend.setValue(true, true);
	}

	@UiHandler("focus")
	void onMouseOut(MouseOutEvent event) {
		extend.setValue(false, true);
	}

	@UiHandler("extend")
	void onExtend(ValueChangeEvent<Boolean> event) {
		importButton.setStyleName(res.css().closed(), !event.getValue());
	}

	@UiHandler("refreshButton")
	void onRefresh(ClickEvent event) {
		presenter.onListCertificates(type);
	}

	void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	void addCertificate(String name) {
		CertificateItem item = new CertificateItem(name, presenter, type);
		certificatesList.add(item);
		certificates.put(name, item);
	}

	void deleteCertificate(String name) {
		CertificateItem item = certificates.get(name);
		if (item != null) {
			certificatesList.remove(item);
			certificates.remove(name);
		}
	}

	void clearCertificates() {
		certificatesList.clear();
		certificates.clear();
	}

	public boolean isImportEnabled() {
		return importEnabled;
	}

	public void setImportEnabled(boolean importEnabled) {
		this.importEnabled = importEnabled;
	}

	public boolean isExportEnabled() {
		return exportEnabled;
	}

	public void setExportEnabled(boolean exportEnabled) {
		this.exportEnabled = exportEnabled;
	}
}
