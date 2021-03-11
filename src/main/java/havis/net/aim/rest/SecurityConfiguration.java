package havis.net.aim.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import havis.net.aim.opcua.Environment;
import havis.net.aim.rest.data.OpcState;

public class SecurityConfiguration {

	private static final Logger log = Logger.getLogger(SecurityConfiguration.class.getName());

	private static final String XPATH_INSECURE = "//SecurityPolicy[.='http://opcfoundation.org/UA/SecurityPolicy#None']";
	private static final String XPATH_CLIENT_VERIFICATION = "//AutomaticallyTrustAllClientCertificates";

	private static final String XSL_FILE_INSECURE = "xsl/%1$sInsecure.xsl";
	private static final String XSL_FILE_CLIENT_VERIFICATION = "xsl/%1$sClientVerification.xsl";

	public void upload(String path, String name, InputStream stream) throws SecurityConfigurationException {
		try {
			Files.copy(stream, Paths.get(getValidPath(path, true) + name), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new SecurityConfigurationException(e);
		}
	}

	private String getValidPath(String relativePath, boolean upload) {
		String result = Environment.SERVER_CERTIFICATES_ROOT + relativePath;
		File directory = new File(result);
		if (!directory.exists() && upload) {
			directory.mkdirs();
		}
		return result;
	}

	private void transform(String xslFile)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {
		// Load xsl-File from classpath
		InputStream xsl = getClass().getClassLoader().getResourceAsStream(xslFile);
		// Load ServerConfig.xml
		InputStream xml = Files.newInputStream(Paths.get(Environment.SERVER_CONFIG_FILE));

		// transform
		Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		// parse XML
		Document xmlDocument = factory.newDocumentBuilder().parse(xml);
		// execute XSL transformation
		StreamResult xmlResult = new StreamResult(Files.newOutputStream(Paths.get(Environment.SERVER_CONFIG_FILE)));
		transformer.transform(new DOMSource(xmlDocument), xmlResult);
	}

	private String getXslFilename(Boolean enable, String filename) {
		String pre = enable ? "enable" : "disable";
		return String.format(filename, pre);
	}

	private void read(InputStream input, StringBuilder builder) throws UnsupportedEncodingException, IOException {
		int size = input.available();
		if (size > 0) {
			char[] b = new char[size];
			try (Reader reader = new InputStreamReader(input, "UTF-8")) {
				while ((size = reader.read(b)) > -1) {
					builder.append(b);
				}
			}
		}
	}

	private String exec(String... command) throws IOException, InterruptedException, SecurityConfigurationException {
		Process process = Runtime.getRuntime().exec(command);
		int code = process.waitFor();
		final StringBuilder builder = new StringBuilder();
		read(process.getInputStream(), builder);
		if (code != 0) {
			log.log(Level.WARNING, "Execution failed with code {0}", code);
			builder.append("Execution failed\n");
			read(process.getErrorStream(), builder);
			throw new SecurityConfigurationException(builder.toString());
		}
		return builder.toString();
	}

	public void download(String path, String file, OutputStream out) throws SecurityConfigurationException {
		try {
			Files.copy(Paths.get(getValidPath(path, false) + file), out);
		} catch (IOException e) {
			throw new SecurityConfigurationException("File " + file + " not found.");
		}
	}

	public void downloadLog(OutputStream out) throws SecurityConfigurationException {
		try {
			Files.copy(Paths.get(Environment.SERVER_LOG), out);
		} catch (IOException e) {
			throw new SecurityConfigurationException(e);
		}
	}
	
	public void delete(String path, String name) throws SecurityConfigurationException {
		try {
			Files.delete(Paths.get(getValidPath(path, false) + name));
		} catch (IOException e) {
			throw new SecurityConfigurationException(e);
		}
	}

	public List<String> listFiles(String path) throws IOException {
		// TODO: Check for security!!!
		List<String> result = new ArrayList<>();
		DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(getValidPath(path, false)));
		for (Path file : ds) {
			result.add(file.getFileName().toString());
		}
		return result;
	}

	private String readXPath(String path) throws XPathExpressionException, IOException {
		InputStream xml = Files.newInputStream(Paths.get(Environment.SERVER_CONFIG_FILE));
		XPathExpression expr = XPathFactory.newInstance().newXPath().compile(path);
		return expr.evaluate(new InputSource(xml));
	}

	public Boolean getConnectionInsecure() throws SecurityConfigurationException {
		try {
			return readXPath(XPATH_INSECURE).equals("http://opcfoundation.org/UA/SecurityPolicy#None");
		} catch (IOException | XPathExpressionException e) {
			throw new SecurityConfigurationException(e);
		}
	}

	public void setConnectionInsecure(Boolean insecure) throws SecurityConfigurationException {
		if (getConnectionInsecure() != insecure) {
			try {
				transform(getXslFilename(insecure, XSL_FILE_INSECURE));
			} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
				throw new SecurityConfigurationException(e);
			}
		}
	}

	public Boolean getConnectionClientverification() throws SecurityConfigurationException {
		try {
			return Boolean.valueOf(readXPath(XPATH_CLIENT_VERIFICATION));
		} catch (IOException | XPathExpressionException e) {
			throw new SecurityConfigurationException(e);
		}
	}

	public void setConnectionClientverification(Boolean insecure) throws SecurityConfigurationException {
		if (getConnectionClientverification() != insecure) {
			try {
				transform(getXslFilename(insecure, XSL_FILE_CLIENT_VERIFICATION));
			} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
				throw new SecurityConfigurationException(e);
			}
		}
	}

	public OpcState getServerState() throws SecurityConfigurationException {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { Environment.SERVER_SCRIPT, "status"});
			if (process.waitFor() == 0) {
				return OpcState.STARTED;
			} else {
				return OpcState.STOPPED;
			}
		} catch (IOException | InterruptedException e) {
			throw new SecurityConfigurationException(e);
		}
	}

	public void setServerState(OpcState state) throws SecurityConfigurationException {
		try {
			exec(Environment.SERVER_SCRIPT, state.name().toLowerCase());
		} catch (IOException | InterruptedException e) {
			throw new SecurityConfigurationException(e);
		}
	}
}
