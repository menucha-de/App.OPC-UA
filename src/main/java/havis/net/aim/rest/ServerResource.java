package havis.net.aim.rest;

import java.io.ByteArrayOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import havis.net.aim.rest.data.OpcState;
import havis.net.rest.shared.Resource;

public class ServerResource extends Resource {

	private SecurityConfiguration security;

	public ServerResource(SecurityConfiguration security) {
		this.security = security;
	}

	@GET
	@RolesAllowed("admin")
	@Path("state")
	@Produces(MediaType.APPLICATION_JSON)
	public OpcState getServerState() throws SecurityConfigurationException {
		return security.getServerState();
	}

	@PUT
	@RolesAllowed("admin")
	@Path("state")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setServerState(OpcState state) throws SecurityConfigurationException {
		security.setServerState(state);
	}

	@GET
	@RolesAllowed("admin")
	@Path("log")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getServerLog() throws SecurityConfigurationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		security.downloadLog(out);
		return Response.ok(out.toByteArray()).header("Content-Disposition", "attachment; filename=\"opc-ua_server_log\"")
				.header("Content-Type", "application/octet-stream").build();
	}
}
