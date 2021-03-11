package havis.net.aim.rest.provider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import havis.net.aim.rest.SecurityConfigurationException;

@Provider
public class SecurityConfigurationExceptionMapper implements ExceptionMapper<SecurityConfigurationException> {

	@Override
	public Response toResponse(SecurityConfigurationException e) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
				.build();
	}

}
