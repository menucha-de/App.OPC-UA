package havis.net.aim.rest.async;

import havis.net.aim.xsd.Configuration;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../rest/opc")
public interface OpcUaConfigurationServiceAsync extends RestService {
	
	@GET
	@Path("configuration")
	void getConfiguration(MethodCallback<Configuration> callback);
	
	@PUT
	@Path("configuration")
	void setConfiguration(Configuration config, MethodCallback<Void> callback);
	
	@OPTIONS
	@Path("configuration")
	void optionsConfiguration(MethodCallback<Void> callback);
}
