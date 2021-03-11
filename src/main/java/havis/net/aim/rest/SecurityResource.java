package havis.net.aim.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import havis.net.rest.shared.Resource;

public class SecurityResource extends Resource {

	private SecurityConfiguration security;

	public SecurityResource(SecurityConfiguration security) {
		this.security = security;
	}

	@GET
	@RolesAllowed("admin")
	@Path("connection/insecure")
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean getConnectionInsecure() throws SecurityConfigurationException {
		return security.getConnectionInsecure();
	}

	@PUT
	@RolesAllowed("admin")
	@Path("connection/insecure")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConnectionInsecure(Boolean insecure) throws SecurityConfigurationException {
		security.setConnectionInsecure(insecure);
	}

	@GET
	@RolesAllowed("admin")
	@Path("connection/clientverification")
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean getConnectionClientverification() throws SecurityConfigurationException {
		return security.getConnectionClientverification();
	}

	@PUT
	@RolesAllowed("admin")
	@Path("connection/clientverification")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConnectionClientverification(Boolean insecure) throws SecurityConfigurationException {
		security.setConnectionClientverification(insecure);
	}

	@GET
	@RolesAllowed("admin")
	@Path("trusted/crl")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getRevocationCertNames() throws IOException {
		return security.listFiles("trusted/crl/");
	}

	@GET
	@RolesAllowed("admin")
	@Path("trusted/crl/{name}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getRevocationCertByName(@PathParam("name") String name) throws SecurityConfigurationException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		security.download("trusted/crl/", name, stream);
		return Response.ok(stream.toByteArray()).build();
	}

	@POST
	@RolesAllowed("admin")
	@Path("trusted/crl/{name}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void setRevocationCertByName(@PathParam("name") String name, InputStream stream)
			throws SecurityConfigurationException {
		security.upload("trusted/crl/", name, stream);
	}

	@DELETE
	@RolesAllowed("admin")
	@Path("trusted/crl/{name}")
	public void deleteRevocationCertByName(@PathParam("name") String name) throws SecurityConfigurationException {
		security.delete("trusted/crl/", name);
	}

	@GET
	@RolesAllowed("admin")
	@Path("trusted/certs")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTrustedCertificateNames() throws IOException {
		return security.listFiles("trusted/certs/");
	}

	@GET
	@RolesAllowed("admin")
	@Path("trusted/certs/{name}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getTrustedCertificateByName(@PathParam("name") String name) throws SecurityConfigurationException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		security.download("trusted/certs/", name, stream);
		return Response.ok(stream.toByteArray()).build();
	}

	@POST
	@RolesAllowed("admin")
	@Path("trusted/certs/{name}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void setTrustedCertificateByName(@PathParam("name") String name, InputStream stream)
			throws SecurityConfigurationException {
		security.upload("trusted/certs/", name, stream);
	}

	@DELETE
	@RolesAllowed("admin")
	@Path("trusted/certs/{name}")
	public void deleteTrustedCertificate(@PathParam("name") String name) throws SecurityConfigurationException {
		security.delete("trusted/certs/", name);
	}

	@GET
	@RolesAllowed("admin")
	@Path("own/certs")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getServerCertificate() throws SecurityConfigurationException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		security.download("own/certs/", "uaservercpp.der", stream);
		return Response.ok(stream.toByteArray()).header("Content-Disposition", "attachment; filename=\"uaservercpp.der\"")
				.header("Content-Type", "application/octet-stream").build();
	}

	@POST
	@RolesAllowed("admin")
	@Path("own/certs")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void setServerCertificate(InputStream stream) throws SecurityConfigurationException {
		security.upload("own/certs/", "uaservercpp.der", stream);
	}

	@POST
	@RolesAllowed("admin")
	@Path("own/private")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void setServerPrivate(InputStream stream) throws SecurityConfigurationException {
		security.upload("own/private/", "uaservercpp.pem", stream);
	}
}
