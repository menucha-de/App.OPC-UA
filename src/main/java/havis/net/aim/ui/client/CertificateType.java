package havis.net.aim.ui.client;

enum CertificateType {
	PUBLIC(".der"),
	PRIVATE(".pem"),
	CERTS(".der"),
	CRL(".crl");
	
	private String extension;

	private CertificateType(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}
}