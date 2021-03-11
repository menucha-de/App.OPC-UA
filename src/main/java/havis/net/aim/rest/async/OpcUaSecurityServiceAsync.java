package havis.net.aim.rest.async;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../rest/opc/security")
public interface OpcUaSecurityServiceAsync extends RestService {

	@OPTIONS
	@Path("connection/insecure")
	void optionsConnectionInsecure(MethodCallback<Void> callback);

	@GET
	@Path("connection/insecure")
	void getConnectionInsecure(MethodCallback<Boolean> callback);

	@PUT
	@Path("connection/insecure")
	void setConnectionInsecure(Boolean insecure, MethodCallback<Void> callback);

	@OPTIONS
	@Path("connection/clientverification")
	void optionsConnectionClientverification(MethodCallback<Void> callback);
	
	@GET
	@Path("connection/clientverification")
	void getConnectionClientverification(MethodCallback<Boolean> callback);

	@PUT
	@Path("connection/clientverification")
	void setConnectionClientverification(Boolean insecure, MethodCallback<Void> callback);

	@OPTIONS
	@Path("trusted/crl")
	void optionsRevocationCertNames(MethodCallback<Void> callback);

	@GET
	@Path("trusted/crl")
	void getRevocationCertNames(MethodCallback<List<String>> callback);

	@OPTIONS
	@Path("trusted/crl/{name}")
	void optionsRevocationCertByName(MethodCallback<Void> callback);

	@DELETE
	@Path("trusted/crl/{name}")
	void deleteRevocationCertByName(@PathParam("name") String name, MethodCallback<Void> callback);

	@OPTIONS
	@Path("trusted/certs")
	void optionsTrustedCertificateNames(MethodCallback<Void> callback);

	@GET
	@Path("trusted/certs")
	void getTrustedCertificateNames(MethodCallback<List<String>> callback);

	@OPTIONS
	@Path("trusted/certs/{name}")
	void optionsTrustedCertificate(MethodCallback<Void> callback);

	@DELETE
	@Path("trusted/certs/{name}")
	void deleteTrustedCertificate(@PathParam("name") String name, MethodCallback<Void> callback);
	
	@OPTIONS
	@Path("own/certs")
	void optionsServerCertificate(MethodCallback<Void> callback);
	
	@OPTIONS
	@Path("own/private")
	void optionsServerPrivate(MethodCallback<Void> callback);

}
