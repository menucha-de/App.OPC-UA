package havis.net.aim.rest;

import havis.net.aim.opcua.Environment;
import havis.net.aim.rest.SecurityConfiguration;
import havis.net.aim.rest.SecurityConfigurationException;
import havis.net.aim.rest.data.OpcState;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import mockit.Capturing;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SecurityConfigutationTest {
	
	final static private String filePath = "home/user/test/";
	final static private String fileName = "file1";
	
	static private SecurityConfiguration configuration;
	
	@Before
	public void beforeEachTest() {
		configuration = new SecurityConfiguration();
	}
	
	@Test
	public void upload(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked InputStream stream,
			final @Mocked Environment environment,
			final @Mocked File directory) throws Exception {
		
		new NonStrictExpectations() {
			{				
				new File("/opt/havis.opc-ua/pkiserver/" + filePath);
				result = directory;
				
				directory.exists();
				result = false;
			}
		};
		
		configuration.upload(filePath, fileName, stream);

		new Verifications() {
			{
				directory.mkdirs();
				times = 1;
				
				Path resultPath;
				String resultPathString;
				resultPath = Paths.get(resultPathString = withCapture());
				times = 1;
				Assert.assertEquals("/opt/havis.opc-ua/pkiserver/" + filePath + fileName, resultPathString);
				
				CopyOption[] options;
				Files.copy(withEqual(stream), withEqual(resultPath), options = withCapture());
				times = 1;
				Assert.assertEquals(StandardCopyOption.REPLACE_EXISTING, options[0]);
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void uploadFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked InputStream stream,
			final @Mocked Environment environment,
			final @Mocked File directory) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.copy(stream, (Path)any, (CopyOption[]) any);
				result = new IOException();
			}
		};
		
		configuration.upload(filePath, fileName, stream);
	}
	
	@Test
	public void download(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked OutputStream stream,
			final @Mocked Environment environment,
			final @Mocked File directory) throws Exception {
		
		new NonStrictExpectations() {
			{				
				new File("/opt/havis.opc-ua/pkiserver/" + filePath);
				result = directory;
				
				directory.exists();
				result = true;
			}
		};
		
		configuration.download(filePath, fileName, stream);

		new Verifications() {
			{				
				Path resultPath;
				String resultPathString;
				resultPath = Paths.get(resultPathString = withCapture());
				times = 1;
				Assert.assertEquals("/opt/havis.opc-ua/pkiserver/" + filePath + fileName, resultPathString);
				
				Files.copy(withEqual(resultPath), withEqual(stream));
				times = 1;
			}
		};
	}
	
	@Test
	public void downloadFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked OutputStream stream,
			final @Mocked Environment environment,
			final @Mocked File directory) throws Exception{
		
		new NonStrictExpectations() {
			{
				Files.copy((Path) any, stream);
				result = new IOException();
			}
		};

		try {
			configuration.download(filePath, fileName, stream);
		} catch (Exception e) {
			Assert.assertEquals("File " + fileName + " not found.", e.getMessage());
		}
	}
	
	@Test
	public void downloadLog(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked OutputStream stream,
			final @Mocked Environment environment) throws Exception {
		
		configuration.downloadLog(stream);
		
		new Verifications() {
			{				
				Path resultPath;
				String resultPathString;
				resultPath = Paths.get(resultPathString = withCapture());
				times = 1;
				Assert.assertEquals("/var/log/opc-ua/current", resultPathString);
				
				Files.copy(withEqual(resultPath), withEqual(stream));
				times = 1;
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void downloadLogFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked OutputStream stream,
			final @Mocked Environment environment) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.copy((Path) any, stream);
				result = new IOException();
			}
		};
		
		configuration.downloadLog(stream);
	}
	
	@Test
	public void delete(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked File directory) throws Exception {
		
		new NonStrictExpectations() {
			{				
				new File("/opt/havis.opc-ua/pkiserver/" + filePath);
				result = directory;
				
				directory.exists();
				result = true;
			}
		};
		
		configuration.delete(filePath, fileName);
		
		new Verifications() {
			{
				directory.mkdirs();
				times = 0;
				
				Path resultPath;
				String resultPathString;
				resultPath = Paths.get(resultPathString = withCapture());
				times = 1;
				Assert.assertEquals("/opt/havis.opc-ua/pkiserver/" + filePath + fileName, resultPathString);
				
				Files.delete(withEqual(resultPath));
				times = 1;
			}
		};		
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void deleteFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked File directory) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.delete((Path)any);
				result = new IOException();
			}
		};
		
		configuration.delete(filePath, fileName);
	}

	@Test
	public void listFiles(final @Mocked Paths paths,
			final @Mocked Path path,
			final @Mocked Files files,
			final @Capturing DirectoryStream<Path> stream,
			final @Capturing Iterator<?> iterator) throws Exception {
		
		new NonStrictExpectations() {
			{
				Paths.get(anyString);
				result = path;
				
				Files.newDirectoryStream(path);
				result = stream;

				stream.iterator();
				result = iterator;
				
				iterator.hasNext();
				times = 1;
				result = true;

				iterator.hasNext();
				returns(true, false);
				
				iterator.next();
				result = path;
				
				path.getFileName();
				result = path;
				
				path.toString();
				result = fileName;
			}
		};
		
		List<String> fileList = configuration.listFiles(filePath);
		Assert.assertTrue(!fileList.isEmpty());
		Assert.assertEquals(fileName, fileList.get(0));
		
		new Verifications() {
			{
				String uri;
				Path path;
				
				path = Paths.get(uri = withCapture());
				times = 1;
				Assert.assertEquals("/opt/havis.opc-ua/pkiserver/" + filePath, uri);
				
				Files.newDirectoryStream(withEqual(path));
				times = 1;
			}
		};
	}
	
	@Test
	public void getConnectionInsecure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "http://opcfoundation.org/UA/SecurityPolicy#None";
			}
		};
		
		Assert.assertTrue(configuration.getConnectionInsecure());
		
		new Verifications() {
			{
				String uri;
				Paths.get(uri = withCapture());
				times = 1;
				Assert.assertEquals("/opt/havis.opc-ua/conf/ServerConfig.xml", uri);
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void getConnectionInsecureFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment) throws Exception {
		
		new NonStrictExpectations() {
			{
				Paths.get(anyString);
				result = new IOException();
			}
		};
		
		configuration.getConnectionInsecure();
	}
	
	@Test
	public void setConnectionInsecure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Mocked InputStream stream,
			final @Mocked Transformer transformer,
			final @Mocked TransformerFactory transformerFactory,
			final @Mocked StreamSource streamSource,
			final @Mocked DocumentBuilderFactory documentBuilderFactory,
			final @Mocked DocumentBuilder documentBuilder,
			final @Mocked DOMSource domSource,
			final @Capturing Document document,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "http://opcfoundation.org/UA/SecurityPolicy#None";
				
				transformerFactory.newTransformer(streamSource);
				result = transformer;
			}
		};
		
		configuration.setConnectionInsecure(false);
		
		new Verifications() {
			{
				Path path;
				Document doc;
				
				String uri;
				path = Paths.get(uri = withCapture());
				times = 3;
				Assert.assertEquals("/opt/havis.opc-ua/conf/ServerConfig.xml", uri);				
				
				Files.newInputStream(withEqual(path), (OpenOption[])null);
				times = 2;
				
				documentBuilderFactory.setNamespaceAware(true);
				times = 1;
				
				documentBuilderFactory.newDocumentBuilder();
				times = 1;
				
				doc = documentBuilder.parse(withInstanceOf(InputStream.class));
				times = 1;
				
				Files.newOutputStream(withEqual(path), (OpenOption[])null);
				times = 1;
				
				new DOMSource(withEqual(doc));
				times = 1;
				
				transformer.transform(withInstanceOf(DOMSource.class), (Result) withNotNull());
				times = 1;
			}
		};
	}
	
	@Test
	public void setConnectionInsecureSameBool(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "http://opcfoundation.org/UA/SecurityPolicy#None";				
			}
		};
		
		configuration.setConnectionInsecure(true);		
		
		new Verifications() {
			{
				// invoked one times in readXPath < getConnectionInsecure
				// but not in transform < setConnectionInsecure
				Paths.get(Environment.SERVER_CONFIG_FILE);
				times = 1;
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void setConnectionInsecureFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Mocked TransformerFactory transformerFactory,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "http://opcfoundation.org/UA/SecurityPolicy#None";
				
				TransformerFactory.newInstance();
				result = new TransformerException("");
			}
		};
		
		configuration.setConnectionInsecure(false);
	}
	
	@Test
	public void getConnectionClientverification(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "TRUE";
			}
		};
		
		Assert.assertTrue(configuration.getConnectionClientverification());
		
		new Verifications() {
			{
				String uri;
				Paths.get(uri = withCapture());
				times = 1;
				Assert.assertEquals("/opt/havis.opc-ua/conf/ServerConfig.xml", uri);
			
				xPath.compile(withEqual("//AutomaticallyTrustAllClientCertificates"));
				times = 1;
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void getConnectionClientverificationFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment) throws Exception {
		
		new NonStrictExpectations() {
			{
				Paths.get(anyString);
				result = new IOException();
			}
		};
		
		configuration.getConnectionClientverification();
	}
	
	@Test
	public void setConnectionClientverification(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Mocked InputStream stream,
			final @Mocked Transformer transformer,
			final @Mocked TransformerFactory transformerFactory,
			final @Mocked StreamSource streamSource,
			final @Mocked DocumentBuilderFactory documentBuilderFactory,
			final @Mocked DocumentBuilder documentBuilder,
			final @Mocked DOMSource domSource,
			final @Capturing Document document,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "FALSE";
			}
		};
		
		configuration.setConnectionClientverification(true);
		
		new Verifications() {
			{
				Path path;
				Document doc;
				
				String uri;
				path = Paths.get(uri = withCapture());
				times = 3;
				Assert.assertEquals("/opt/havis.opc-ua/conf/ServerConfig.xml", uri);				
				
				Files.newInputStream(withEqual(path), (OpenOption[])null);
				times = 2;
				
				documentBuilderFactory.setNamespaceAware(true);
				times = 1;
				
				documentBuilderFactory.newDocumentBuilder();
				times = 1;
				
				doc = documentBuilder.parse(withInstanceOf(InputStream.class));
				times = 1;
				
				Files.newOutputStream(withEqual(path), (OpenOption[])null);
				times = 1;
				
				new DOMSource(withEqual(doc));
				times = 1;
				
				transformer.transform(withInstanceOf(DOMSource.class), (Result) withNotNull());
				times = 1;
			}
		};		
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void setConnectionClientverificationFailure(final @Mocked Files files,
			final @Mocked Paths paths,
			final @Mocked Environment environment,
			final @Mocked InputStream xml,
			final @Mocked XPathFactory factory,
			final @Mocked InputSource is,
			final @Mocked TransformerFactory transformerFactory,
			final @Capturing XPath xPath,
			final @Capturing XPathExpression exp) throws Exception {
		
		new NonStrictExpectations() {
			{
				Files.newInputStream((Path) any, (OpenOption[])null);
				result = xml;
				
				factory.newXPath();
				result = xPath;
				
				xPath.compile(anyString);
				result = exp;
				
				new InputSource(xml);
				result = is;
				
				exp.evaluate(is);
				result = "TRUE";
				
				TransformerFactory.newInstance();
				result = new TransformerException("");
			}
		};
		
		configuration.setConnectionClientverification(false);
	}
	
	@Test
	public void getServerStateStarted(final @Mocked Runtime runtime,
			final @Mocked Process process) throws Exception {
		new NonStrictExpectations() {
			{
				Runtime.getRuntime();
				result = runtime;
				
				runtime.exec((String) withNotNull());
				result = process;
				
				process.waitFor();
				result = 0;
			}
		};
		
		Assert.assertEquals(OpcState.STARTED, configuration.getServerState());
		
		new Verifications() {
			{
				String[] cmds;
				runtime.exec(cmds = withCapture());
				times = 1;
				Assert.assertTrue(cmds.length == 2);
				Assert.assertEquals("/etc/init.d/opc-ua.sh", cmds[0]);
				Assert.assertEquals("status", cmds[1]);
			}
		};
	}
	
	@Test
	public void getServerStateStopped(final @Mocked Runtime runtime,
			final @Mocked Process process) throws Exception {
		new NonStrictExpectations() {
			{
				Runtime.getRuntime();
				result = runtime;
				
				runtime.exec((String) withNotNull());
				result = process;
				
				process.waitFor();
				result = 1;
			}
		};
		
		Assert.assertEquals(OpcState.STOPPED, configuration.getServerState());
		
		new Verifications() {
			{
				String[] cmds;
				runtime.exec(cmds = withCapture());
				times = 1;
				Assert.assertTrue(cmds.length == 2);
				Assert.assertEquals("/etc/init.d/opc-ua.sh", cmds[0]);
				Assert.assertEquals("status", cmds[1]);
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void getServerStateFailure(final @Mocked Runtime runtime,
			final @Mocked Process process) throws Exception {
		
		new NonStrictExpectations() {
			{
				Runtime.getRuntime();
				result = runtime;
				
				runtime.exec((String) withNotNull());
				result = process;
				
				process.waitFor();
				result = new InterruptedException();
			}
		};
		
		configuration.getServerState();
	}
	
	//TODO: read Methode überprüfen
	@Test
	public void setServerStateSuccessfull(final @Mocked Runtime runtime,
			final @Mocked Process process,
			final @Mocked InputStreamReader reader,
			final @Mocked InputStream stream,
			final @Mocked StringBuilder builder) throws Exception {
		
		new NonStrictExpectations() {
			{
				Runtime.getRuntime();
				result = runtime;
				
				runtime.exec(anyString);
				result = process;
				
				process.waitFor();
				result = 0;
				
				process.getInputStream();
				result = stream;
				
				stream.available();
				result = 5;
				
				new InputStreamReader(stream, "UTF-8");
				result = reader;
				
				reader.read((char[])any);
				returns(3, 2, 1, 0, -1);
			}
		};
		
		configuration.setServerState(OpcState.START);
		
		new Verifications() {
			{
				String[] commands;
				runtime.exec(commands = withCapture());
				times = 1;
				Assert.assertTrue(commands.length == 2);
				Assert.assertEquals("/etc/init.d/opc-ua.sh", commands[0]);
				Assert.assertEquals("start", commands[1]);
				
				process.waitFor();
				times = 1;
				
				stream.available();
				times = 1;
				
				reader.read((char[]) any);
				times = 5;
			}
		};
	}
	
	@Test (expected = SecurityConfigurationException.class)
	public void setServerStateFailure(final @Mocked Runtime runtime,
			final @Mocked Process process) throws Exception {
		
		new NonStrictExpectations() {
			{
				Runtime.getRuntime();
				result = runtime;
				
				runtime.exec(anyString);
				result = process;
				
				process.waitFor();
				result = new InterruptedException();
			}
		};
		
		configuration.setServerState(OpcState.STOP);
	}
	
	@Test
	public void setServerStateFailureErrorCode(final @Mocked Runtime runtime,
			final @Mocked Process process,
			final @Mocked InputStream stream,
			final @Mocked Logger log) throws Exception {
		
		new NonStrictExpectations() {
			{
				Runtime.getRuntime();
				result = runtime;
				
				runtime.exec(anyString);
				result = process;
				
				process.waitFor();
				result = 1;
				
				process.getInputStream();
				result = stream;
				
				stream.available();
				result = 0;
				
				process.getErrorStream();
				result = stream;
			}
		};
		
		try {
			configuration.setServerState(OpcState.START);
		} catch (SecurityConfigurationException e) {
			Assert.assertEquals("Execution failed\n", e.getMessage());
		}
		
		new Verifications() { 
			{
				String[] commands;
				runtime.exec(commands = withCapture());
				times = 1;
				Assert.assertTrue(commands.length == 2);
				Assert.assertEquals("/etc/init.d/opc-ua.sh", commands[0]);
				Assert.assertEquals("start", commands[1]);
				
				process.waitFor();
				times = 1;
				
				stream.available();
				times = 2;
				
				Level loglevel;
				String msg;
				int code;
				log.log(loglevel = withCapture(), msg = withCapture(), code = withCapture());
				times = 1;
				Assert.assertEquals(Level.WARNING, loglevel);
				Assert.assertEquals("Execution failed with code {0}", msg);
				Assert.assertEquals(1, code);
			}
		};
	}
}
