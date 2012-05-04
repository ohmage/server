package org.ohmage.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaParseException;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.ohmage.exception.DomainException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Probe {
	private static final Pattern PATTERN_ID_VALIDATOR = 
		Pattern.compile("[a-zA-Z0-9\\.]");
	
	private final String id;
	private final long version;
	
	private final String name;
	private final String description;
	private final String versionString;
	
	public static class Observer {
		private final String id;
		private final long version;
		
		private final String name;
		private final String description;
		
		// This may not be a string if Avro has it's own representation.
		private final Schema schema; 
		
		public Observer(
				final String id,
				final long version,
				final String name,
				final String description,
				final String schema)
				throws DomainException {
			
			if(id == null) {
				throw new DomainException("The ID is null.");
			}
			if(name == null) {
				throw new DomainException("The name is null.");
			}
			if(description == null) {
				throw new DomainException("The description is null.");
			}
			if(schema == null) {
				throw new DomainException(
					"The schema of this observer is null.");
			}
			
			this.id = id;
			this.version = version;
			
			this.name = name;
			this.description = description;
			
			try {
				this.schema = (new Parser()).parse(schema);
			}
			catch(SchemaParseException e) {
				throw new DomainException("The schema was invalid.", e);
			}
		}
		
		public GenericContainer parseDatum(
				final byte[] binary) 
				throws DomainException {
			
			if(binary == null) {
				throw new DomainException("The binary data is null.");
			}
			
			Decoder decoder = 
				(new DecoderFactory()).binaryDecoder(binary, null);
			GenericDatumReader<GenericContainer> genericReader =
				new GenericDatumReader<GenericContainer>(schema);
			
			try {
				return genericReader.read(null, decoder);
			}
			catch(IOException e) {
				throw new DomainException("The data could not be read.", e);
			}
		}
	}
	private final Map<String, Observer> observers;
	
	public Probe(
			final String id,
			final long version,
			final String name,
			final String description,
			final String versionString,
			final Collection<Observer> observers)
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		if(name == null) {
			throw new DomainException("The name is null.");
		}
		if(description == null) {
			throw new DomainException("The description is null.");
		}
		if(versionString == null) {
			throw new DomainException("The version string is null.");
		}
		if(observers == null) {
			throw new DomainException("The observers collection is null.");
		}
		if(observers.size() == 0) {
			throw new DomainException("The observers collection is empty.");
		}
		
		this.id = sanitizeId(id);
		this.version = version;
		
		this.name = name.trim();
		this.description = description.trim();
		this.versionString = versionString.trim();
		
		this.observers = new HashMap<String, Observer>(observers.size());
		for(Observer observer : observers) {
			if(observer == null) {
				continue;
			}
			
			this.observers.put(observer.id, observer);
		}
	}
	
	public Probe(final String xml) throws DomainException {
		Element root;
		try {
			root =
				DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(
						new ByteArrayInputStream(
							xml.getBytes()))
					.getDocumentElement();
		}
		catch(SAXException e) {
			throw new DomainException(
				"The XML is invalid.",
				e);
		}
		catch(IOException e) {
			// The stream failed!?
			throw new DomainException(e);
		}
		catch(FactoryConfigurationError e) {
			// The document builder factory failed to be built.
			throw new DomainException(e);
		}
		catch(ParserConfigurationException e) {
			// A document builder could not be created.
			throw new DomainException(e);
		}
		catch(IllegalArgumentException e) {
			// The InputStream was null.
			throw new DomainException(e);
		}
		
		id = sanitizeId(getXmlValue(root, "id", "probe ID"));
		
		try {
			version = 
				Long.decode(getXmlValue(root, "version", "probe version"));
		}
		catch(NumberFormatException e) {
			throw new DomainException(
				"The probe version was not a number.",
				e);
		}

		name = getXmlValue(root, "name", "probe name");
		description = getXmlValue(root, "description", "probe description");
		versionString = getXmlValue(root, "name", "probe version string");
		
		observers = new HashMap<String, Observer>();
		NodeList observersList = root.getElementsByTagName("observer");
		int observersListLength = observersList.getLength();
		for(int i = 0; i < observersListLength; i++) {
			Element observerNode = (Element) observersList.item(i);
			
			String id = 
				sanitizeId(getXmlValue(observerNode, "id", "observer ID"));
			
			Long version;
			try {
				version = 
					Long.decode(
						getXmlValue(
							observerNode, 
							"version", 
							"observer, " + id + ", version"));
			}
			catch(NumberFormatException e) {
				throw new DomainException(
					"The observer's, " + id + ", version was not a number.",
					e);
			}

			String name = 
				getXmlValue(
					observerNode, 
					"name", 
					"observer, " + id + ", name");
			
			String description = 
				getXmlValue(
					observerNode, 
					"description", 
					"observer, " + id + ", description");

			String schema = 
				getXmlValue(
					observerNode,
					"schema",
					"observer, " + id + ", schema");
			
			observers.put(
				id, 
				new Observer(id, version, name, description, schema));
		}
	}
	
	private static final String sanitizeId(
			final String id) 
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The ID is null.");
		}
		
		String trimmedId = id.trim();
		if(trimmedId.length() == 0) {
			throw new DomainException("The ID is all whitespace.");
		}
		
		String[] idTokens = trimmedId.split(".");
		for(String idToken : idTokens) {
			if(idToken == null) {
				throw new DomainException(
					"Multiple field separators are in sequence: " +
						trimmedId);
			}
			
			String trimmedIdToken = idToken.trim();
			if(trimmedIdToken.length() == 0) {
				throw new DomainException(
					"Multiple field separators are in sequence: " +
						trimmedId);
			}
			if(trimmedIdToken.length() != idToken.length()) {
				throw new DomainException(
					"A portion of the ID contains whitespace characters: " +
						trimmedIdToken);
			}
			if(! PATTERN_ID_VALIDATOR.matcher(trimmedIdToken).matches()) {
				throw new DomainException(
					"A portion of the ID contains illegal characters: " +
						trimmedIdToken);
			}
		}
		
		return trimmedId;
	}
	
	private static final String getXmlValue(
			final Element element,
			final String tagName,
			final String errorName) 
			throws DomainException {
		
		String result = null;
		NodeList results = element.getElementsByTagName(tagName);
		if(results.getLength() > 1) {
			throw new DomainException(
				"Multiple " + errorName + "s were defined in the XML.");
		}
		else if(results.getLength() == 1) {
			try {
				result = results.item(0).getTextContent();
			}
			catch(DOMException e) {
				throw new DomainException(
					"The " + errorName + " was not a value.",
					e);
			}
		}
		if(result == null || result.trim().length() == 0) {
			throw new DomainException(
				"The " + errorName + " is missing.");
		}
		else {
			return result.trim();
		}
	}
}
