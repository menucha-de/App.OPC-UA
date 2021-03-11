package havis.net.aim.ui.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ToggleButton;

import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.upload.File;

public interface SecuritySectionView extends IsWidget {
	void setPresenter(Presenter presenter);
	ToggleButton getInsecure();
	ToggleButton getClientVerification();
	Button getPublicServerCertExport();
	Button getPublicServerCertImport();
	Button getPrivateServerCertImport();
	CertificatesSection getCertificatesSection(CertificateType type);

	void showMessage(MessageType messageType, String message);

	public interface Presenter {
		void onInsecureChange(boolean value);
		void onClientVerificationChange(boolean value);
		void onUploadPublicServerCert(File file);
		void onUploadPrivateServerCert(File file);
		void onDownloadPublicServerCert();
		void onDownloadCertificate(String name, CertificateType type);
		void onUploadCertificate(File file, String name, CertificateType type);
		void onDeleteCertificate(String name, CertificateType type);
		void onListCertificates(CertificateType type);
	}
}
