package havis.net.aim.ui.client;

import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.xml.XMLHttpRequest;
import havis.net.aim.rest.async.OpcUaSecurityServiceAsync;
import havis.net.aim.ui.client.SecuritySectionView.Presenter;
import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.upload.File;
import havis.net.ui.shared.client.widgets.LoadingSpinner;
import havis.net.ui.shared.data.HttpMethod;

public class SecuritySectionPresenter implements Presenter {

	private SecuritySectionView view;
	private OpcUaSecurityServiceAsync service = GWT.create(OpcUaSecurityServiceAsync.class);
	private LoadingSpinner spinner = new LoadingSpinner(); 

	public SecuritySectionPresenter(SecuritySectionView view) {
		this.view = view;
		this.view.setPresenter(this);
		getPermissions();
	}

	private void getPermissions() {
		service.optionsConnectionInsecure(new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
				view.getInsecure().setEnabled(HttpMethod.PUT.isAllowed(method.getResponse()));
				service.getConnectionInsecure(new MethodCallback<Boolean>() {

					@Override
					public void onSuccess(Method method, Boolean response) {
						view.getInsecure().setValue(response);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						view.showMessage(MessageType.ERROR, "Failed to load data");
					}
				});
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				view.showMessage(MessageType.ERROR, "Failed to load data");
			}
		});
		service.optionsConnectionClientverification(new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
				view.getClientVerification().setEnabled(HttpMethod.PUT.isAllowed(method.getResponse()));
				service.getConnectionClientverification(new MethodCallback<Boolean>() {

					@Override
					public void onSuccess(Method method, Boolean response) {
						view.getClientVerification().setValue(response);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						view.showMessage(MessageType.ERROR, "Failed to load data");
					}
				});
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				view.showMessage(MessageType.ERROR, "Failed to load data");
			}
		});
		service.optionsServerCertificate(new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
				view.getPublicServerCertExport().setEnabled(HttpMethod.GET.isAllowed(method.getResponse()));
				view.getPublicServerCertImport().setEnabled(HttpMethod.POST.isAllowed(method.getResponse()));
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				view.showMessage(MessageType.ERROR, "Failed to load data");
			}
		});
		service.optionsServerPrivate(new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
				view.getPrivateServerCertImport().setEnabled(HttpMethod.POST.isAllowed(method.getResponse()));
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				view.showMessage(MessageType.ERROR, "Failed to load data");
			}
		});
		service.optionsRevocationCertByName(new MethodCallback<Void>() {
			
			@Override
			public void onSuccess(Method method, Void response) {
				view.getCertificatesSection(CertificateType.CRL).setImportEnabled(HttpMethod.POST.isAllowed(method.getResponse()));
				service.getRevocationCertNames(new MethodCallback<List<String>>() {

					@Override
					public void onSuccess(Method method, List<String> response) {
						onListCertificates(CertificateType.CRL);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						view.showMessage(MessageType.ERROR, "Failed to load data");
					}
				});
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub
				
			}
		});
		service.optionsTrustedCertificate(new MethodCallback<Void>() {
			
			@Override
			public void onSuccess(Method method, Void response) {
				view.getCertificatesSection(CertificateType.CERTS).setImportEnabled(HttpMethod.POST.isAllowed(method.getResponse()));
				service.getTrustedCertificateNames(new MethodCallback<List<String>>() {

					@Override
					public void onSuccess(Method method, List<String> response) {
						onListCertificates(CertificateType.CERTS);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						view.showMessage(MessageType.ERROR, "Failed to load data");
					}
				});
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void uploadFile(File file, String url, String fileType, final CertificateType type) {
		if (file.getName().endsWith(fileType)) {

			spinner.show();
			final XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
			xhr.open("POST", url);
			xhr.setRequestHeader("Content-Type", "application/octet-stream");
			xhr.setRequestHeader("Authorization", "Basic " + Browser.getWindow().btoa("admin:"));

			xhr.addEventListener("load", new EventListener() {

				@Override
				public void handleEvent(Event evt) {
					int status = xhr.getStatus();
					if (status == 204) {
						spinner.hide();
						if (type == CertificateType.CERTS || type == CertificateType.CRL) {
							onListCertificates(type);
						}
					}
					if (status == 500) {
						spinner.hide();
						view.showMessage(MessageType.ERROR, "Error while uploading file");
					}
				}
			}, false);

			xhr.addEventListener("error", new EventListener() {

				@Override
				public void handleEvent(Event evt) {
					spinner.hide();
					view.showMessage(MessageType.ERROR, "Error while uploading file");
				}
			}, false);

			xhr.addEventListener("abort", new EventListener() {

				@Override
				public void handleEvent(Event evt) {
					spinner.hide();
					view.showMessage(MessageType.INFO, "Aborted by user!");
				}
			}, false);

			xhr.send(file);
		} else {
			spinner.hide();
			view.showMessage(MessageType.ERROR, "File must be of type *" + fileType);
		}
	}

	@Override
	public void onInsecureChange(final boolean value) {
		service.setConnectionInsecure(value, new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				view.getInsecure().setValue(!value);
				view.showMessage(MessageType.ERROR, "Changing value failed!");
			}
		});
	}

	@Override
	public void onClientVerificationChange(final boolean value) {
		service.setConnectionClientverification(value, new MethodCallback<Void>() {

			@Override
			public void onSuccess(Method method, Void response) {
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				view.getClientVerification().setValue(!value);
				view.showMessage(MessageType.ERROR, "Changing value failed!");
			}
		});
	}

	@Override
	public void onUploadPublicServerCert(File file) {
		uploadFile(file, GWT.getHostPageBaseURL() + "rest/opc/security/own/certs", ".der", CertificateType.PUBLIC);
	}

	@Override
	public void onUploadPrivateServerCert(File file) {
		uploadFile(file, GWT.getHostPageBaseURL() + "rest/opc/security/own/private", ".pem", CertificateType.PRIVATE);
	}

	@Override
	public void onDownloadPublicServerCert() {
		Window.open(GWT.getHostPageBaseURL() + "rest/opc/security/own/certs", "_blank", "");
	}

	@Override
	public void onDownloadCertificate(String name, CertificateType type) {
		Window.open(GWT.getHostPageBaseURL() + "rest/opc/security/trusted/" + type.name().toLowerCase() + "/" + name,
				"_blank", "");

	}

	@Override
	public void onUploadCertificate(File file, String name, CertificateType type) {
		uploadFile(file, GWT.getHostPageBaseURL() + "rest/opc/security/trusted/" + type.name().toLowerCase() + "/" + name, "", type);
	}

	@Override
	public void onDeleteCertificate(final String name, final CertificateType type) {
		MethodCallback<Void> callback = new MethodCallback<Void>() {
			
			@Override
			public void onSuccess(Method method, Void response) {
				view.getCertificatesSection(type).deleteCertificate(name);
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub
				
			}
		};
		switch (type) {
		case CERTS:
			service.deleteTrustedCertificate(name, callback);
			break;
		case CRL:
			service.deleteRevocationCertByName(name, callback);
			break;
		default:
			break;
		}
	}

	@Override
	public void onListCertificates(final CertificateType type) {
		MethodCallback<List<String>> certsCallback = new MethodCallback<List<String>>() {

			@Override
			public void onSuccess(Method method, List<String> response) {
				view.getCertificatesSection(type).clearCertificates();
				for (String name : response) {
					view.getCertificatesSection(type).addCertificate(name);
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub

			}
		};
		switch (type) {
		case CERTS:
			service.getTrustedCertificateNames(certsCallback);
			break;
		case CRL:
			service.getRevocationCertNames(certsCallback);
		default:
			break;
		}
	}

}
