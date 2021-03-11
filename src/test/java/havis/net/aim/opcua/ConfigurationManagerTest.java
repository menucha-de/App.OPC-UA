package havis.net.aim.opcua;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.junit.After;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import havis.net.aim.xsd.Configuration;
import havis.net.aim.xsd.TagSet;

public class ConfigurationManagerTest {

	
	private void init(TagSet initialTagSet) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		File configFile = new File(Environment.CONFIG_FILE);
		if (!configFile.exists())
			Files.createDirectories(configFile.toPath().getParent(), new FileAttribute<?>[] {});
		
		Configuration config = new Configuration();
		config.setTagSet(initialTagSet);
		
		mapper.writeValue(configFile, config);
	}
	
	@After
	public void cleanUp() throws IOException {
		File configFile = new File(Environment.CONFIG_FILE);
		Path path = configFile.toPath();
		while (path != null) {
			if (!"target".equals(path.toString()))
				Files.deleteIfExists(path);
			path = path.getParent();
		}
	}
	
	@Test
	public void testConfigurationManager() throws IOException {
		ConfigurationManager cMgr = new ConfigurationManager();
		Configuration cfg = getField(cMgr, "config");
		assertEquals(TagSet.CURRENT, cfg.getTagSet());
		
		init(TagSet.ADDITIONS);
		cMgr = new ConfigurationManager();
		cfg = getField(cMgr, "config");
		assertEquals(TagSet.ADDITIONS, cfg.getTagSet());
	}

	@Test
	public void testGetTagSet() {
		ConfigurationManager cMgr = new ConfigurationManager();
		assertEquals(((Configuration) getField(cMgr, "config")).getTagSet(), cMgr.getTagSet()); 
	}

	@Test
	public void testSetTagSet() throws IOException, ConfigurationManagerException {
		init(TagSet.CURRENT);
		ConfigurationManager cMgr = new ConfigurationManager();
		assertEquals(TagSet.CURRENT, ((Configuration) getField(cMgr, "config")).getTagSet());		
		
		cMgr.setTagSet(TagSet.ADDITIONS);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		File configFile = new File(Environment.CONFIG_FILE);
		
		Configuration newConfig = mapper.readValue(configFile, Configuration.class);
		assertEquals(TagSet.ADDITIONS, newConfig.getTagSet());
	}

	@Test
	public void testReset() throws IOException {		
		init(TagSet.CURRENT);
		assertTrue(new File(Environment.CONFIG_FILE).exists());
		new ConfigurationManager().reset();
		assertFalse(new File(Environment.CONFIG_FILE).exists());
	}

	@Test
	public void testSet() throws ConfigurationManagerException, JsonParseException, JsonMappingException, IOException {
		ConfigurationManager cMgr = new ConfigurationManager();
		assertEquals(TagSet.CURRENT, cMgr.getTagSet());
		Configuration newCfg = new Configuration();
		newCfg.setTagSet(TagSet.DELETIONS);
		cMgr.set(newCfg);
		
		File configFile = new File(Environment.CONFIG_FILE);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		newCfg = mapper.readValue(configFile, Configuration.class);
		assertEquals(TagSet.DELETIONS, newCfg.getTagSet());				
	}

	@Test
	public void testGet() {
		ConfigurationManager cMgr = new ConfigurationManager();
		Configuration cfg = new Configuration();
		cfg.setTagSet(TagSet.ADDITIONS);
		setField(cMgr, "config", cfg);
		
		Configuration oldCfg = cMgr.get();
		assertEquals(cfg.getTagSet(), oldCfg.getTagSet());
	}
}
