package havis.net.aim.rest;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import havis.net.aim.opcua.ConfigurationManager;
import havis.net.aim.opcua.ConfigurationManagerException;
import havis.net.aim.xsd.Configuration;

@Path("opc")
public class ConfigurationService {

	private ConfigurationManager configurationManager;
	private SecurityResource securityResource;
	private ServerResource serverResource;

	public ConfigurationService(ConfigurationManager configurationHelper, SecurityConfiguration security) {
		this.configurationManager = configurationHelper;
		this.securityResource = new SecurityResource(security);
		this.serverResource = new ServerResource(security);
	}

	@PermitAll
	@PUT
	@Path("configuration")
	@Consumes({ MediaType.APPLICATION_JSON })
	public void setConfiguration(Configuration configuration) throws ConfigurationManagerException {
		configurationManager.set(configuration);
	}

	@PermitAll
	@GET
	@Path("configuration")
	@Produces({ MediaType.APPLICATION_JSON })
	public Configuration getConfiguration() {
		return configurationManager.get();
	}

	@Path("security")
	public SecurityResource getSecurityService() {
		return securityResource;
	}

	@Path("server")
	public ServerResource getServerRecource() {
		return serverResource;
	}

}
