package havis.net.aim.rest.async;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import havis.net.aim.rest.data.OpcState;

@Path("../rest/opc/server")
public interface OpcUaServerServiceAsync extends RestService {
	@OPTIONS
	@Path("state")
	void optionsServerState(MethodCallback<Void> callback);
		
	@GET
	@Path("state")
	void getServerState(MethodCallback<OpcState> callback);
	
	@PUT
	@Path("state")
	void setServerState(OpcState state, MethodCallback<Void> callback);
	
}
