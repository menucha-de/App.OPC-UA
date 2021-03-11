package havis.net.aim.opcua;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import havis.net.aim.xsd.Configuration;
import havis.net.aim.xsd.TagSet;

public class ConfigurationManager {

	private Configuration config;
	private ObjectMapper mapper;

	public ConfigurationManager() {
		super();

		this.mapper = new ObjectMapper();
		this.mapper.enableDefaultTyping();
		this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

		this.config = deserialize();

	}

	public TagSet getTagSet() {
		return config.getTagSet();
	}

	public synchronized void setTagSet(TagSet tagSet) throws ConfigurationManagerException {
		TagSet oldTagSet = getTagSet();

		try {
			this.config.setTagSet(tagSet);
			serialize();
		} catch (IOException ex) {
			this.config.setTagSet(oldTagSet);
			throw new ConfigurationManagerException("Failed to persist configuration.", ex);
		}
	}

	public synchronized void reset() {
		new File(Environment.CONFIG_FILE).delete();
	}

	private Configuration deserialize() {

		File configFile = new File(Environment.CONFIG_FILE);

		if (configFile.exists()) {
			try (InputStream cfgStream = new FileInputStream(configFile)) {
				return mapper.readValue(cfgStream, Configuration.class);
			} catch (IOException ex) {
				reset();
			}
		}
		Configuration cfg = new Configuration();
		cfg.setTagSet(TagSet.CURRENT);
		return cfg;
	}

	private void serialize() throws IOException {
		File configFile = new File(Environment.CONFIG_FILE);
		if (!configFile.exists())
			Files.createDirectories(configFile.toPath().getParent(), new FileAttribute<?>[] {});
		mapper.writeValue(configFile, this.config);
	}

	public synchronized void set(Configuration cfg) throws ConfigurationManagerException {

		if (config.getTagSet() != cfg.getTagSet())
			setTagSet(cfg.getTagSet());
	}

	public synchronized Configuration get() {
		return config.clone();
	}
}
