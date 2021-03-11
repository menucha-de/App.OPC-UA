package havis.net.aim.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import havis.net.aim.opcua.ConfigurationManager;
import havis.net.aim.rest.provider.SecurityConfigurationExceptionMapper;

public class RESTApplication extends Application {
	private final static String PROVIDERS = "javax.ws.rs.ext.Providers";

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();
	private Map<String, Object> properties = new HashMap<>();

	public RESTApplication(ConfigurationManager configurationHelper, SecurityConfiguration security) {
		singletons.add(new ConfigurationService(configurationHelper, security));
		properties.put(PROVIDERS, new Class<?>[] { SecurityConfigurationExceptionMapper.class });
	}

	@Override
	public Set<Class<?>> getClasses() {
		return empty;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
}