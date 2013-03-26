package org.ohmage.domain;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;
import nu.xom.XPathException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream.MetaData;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;
import org.w3c.dom.DOMException;

/**
 * This class represents an observer which contains the overall information
 * including its set of streams.
 *
 * @author John Jenkins
 */
@XmlRootElement
public class Observer {
	/**
	 * This is the contents of the JavaSciprt file that evaluates schemas and
	 * data against those schemas. This should be used in conjunction with
	 * something like Rhino, in order to create a JavaScript interpreter to
	 * evaluate the JavaScript.
	 */
	private static final String JS_SCHEMA;
	static {
		FileReader reader;
		try {
			reader =
				new FileReader(
					System.getProperty("webapp.root") + "Concordia.js");
		}
		catch(FileNotFoundException e) {
			throw new IllegalStateException(
				"The JSON Schema could not be found.",
				e);
		}
		
		try {
			int amountRead;
			char[] buffer = new char[4096];
			StringBuilder builder = new StringBuilder();
			while((amountRead = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, amountRead);
			}
		
			JS_SCHEMA = builder.toString();
		}
		catch(IOException e) {
			throw new IllegalStateException(
				"There was a problem reading the JSON Schema's JavaScript file.",
				e);
		}
		finally {
			try {
				reader.close();
			}
			catch(IOException e) {
				throw new IllegalStateException(
					"Could not close the file reader.",
					e);
			}
		}
	}
	
	/**
	 * The JSON factory for creating parsers and generators.
	 */
	private static final JsonFactory JSON_FACTORY = new MappingJsonFactory();
	
	private static final String KEY_JSON_ID = "id";
	private static final String KEY_JSON_VERSION = "version";
	private static final String KEY_JSON_NAME = "name";
	private static final String KEY_JSON_DESCRIPTION = "description";
	private static final String KEY_JSON_VERSION_STRING = "versionString";
	private static final String KEY_JSON_STREAMS = "streams";
	private static final String KEY_XML_STREAM = "stream";
	
	private static final Pattern PATTERN_ID_VALIDATOR = 
		Pattern.compile("([a-zA-Z]{1}[\\w]*(\\.[a-zA-Z]{1}[\\w]*)+)?");
	private static final long MAX_OBSERVER_ID_LENGTH = 255;

	@XmlElement(name=KEY_JSON_ID)
	private final String id;
	@XmlElement(name=KEY_JSON_VERSION)
	private final long version;

	@XmlElement(name=KEY_JSON_NAME)
	private final String name;
	@XmlElement(name=KEY_JSON_DESCRIPTION)
	private final String description;
	@XmlElement(name=KEY_JSON_VERSION_STRING)
	private final String versionString;

	/**
	 * This class represents the definition of a single stream of data. This
	 * class is immutable and, therefore, thread-safe.
	 *
	 * @author John Jenkins
	 */
	public static class Stream {
		private static final String KEY_JSON_ID = "id";
		private static final String KEY_JSON_VERSION = "version";
		private static final String KEY_JSON_NAME = "name";
		private static final String KEY_JSON_DESCRIPTION = "description";
		private static final String KEY_JSON_WITH_ID = "with_id";
		private static final String KEY_JSON_WITH_TIMESTAMP = "with_timestamp";
		private static final String KEY_JSON_WITH_LOCATION = "with_location";
		private static final String KEY_JSON_SCHEMA = "schema";

		private static final Pattern PATTERN_ID_VALIDATOR = 
			Pattern.compile("[a-zA-Z]{1}[\\w_]{0,254}");
		
		@XmlElement(name=KEY_JSON_ID)
		private final String id;
		@XmlElement(name=KEY_JSON_VERSION)
		private final long version;

		@XmlElement(name=KEY_JSON_NAME)
		private final String name;
		@XmlElement(name=KEY_JSON_DESCRIPTION)
		private final String description;
		
		// 'true' means that a field must be present, 'false' means that a
		// field cannot be present, and 'null' means that the it may or may
		// not be there.
		@XmlElement(name=KEY_JSON_WITH_ID)
		private final Boolean withId;
		@XmlElement(name=KEY_JSON_WITH_TIMESTAMP)
		private final Boolean withTimestamp;
		@XmlElement(name=KEY_JSON_WITH_LOCATION)
		private final Boolean withLocation;

		@XmlElement(name=KEY_JSON_SCHEMA)
		private final String schemaString;
		private final JsonParser schema;
		
		/**
		 * Private, default constructor. This should never be used and would
		 * result in a very broken object, but it is required by JAXB. :(
		 */
		@SuppressWarnings("unused")
		private Stream() {
			id = null;
			version = -1;
			name = null;
			description = null;
			withId = null;
			withTimestamp = null;
			withLocation = null;
			schemaString = null;
			schema = null;
		}

		/**
		 * Creates a new stream definition.
		 * 
		 * @param id The unique identifier.
		 * 
		 * @param version The version for internal use.
		 * 
		 * @param name The display name for this stream.
		 * 
		 * @param description The description to be displayed to users.
		 * 
		 * @param withTimestamp Whether or not the data streams for this stream
		 * 						should contain a timestamp. False means to 
		 * 						explicitly ignore timestamps whereas true means
		 * 						to consume them if they exist.
		 * 
		 * @param withLocation Whether or not the data streams for this stream
		 * 					   should contain a locatoin. False means to
		 * 					   explicitly ignore location data whereas true 
		 * 					   means to consume them if they exist.
		 * 
		 * @param schema A string representing a valid Avro JSON schema.
		 * 
		 * @throws DomainException Required information was missing or invalid.
		 */
		public Stream(
				final String id,
				final long version,
				final String name,
				final String description,
				final Boolean withId,
				final Boolean withTimestamp,
				final Boolean withLocation,
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

			this.id = sanitizeId(id);
			this.version = version;

			this.name = name;
			this.description = description;
			
			this.withId = withId;
			this.withTimestamp = withTimestamp;
			this.withLocation = withLocation;

			this.schema = validateSchema(schema);
			this.schemaString = schema;
		}
		
		/**
		 * Creates a Stream from the XML node.
		 * 
		 * @param stream The XML node that defines a Stream.
		 * 
		 * @throws DomainException Required fields were missing or invalid from
		 * 						   the XML.
		 */
		private Stream(final Node stream) throws DomainException {
			id = sanitizeId(getXmlValue(stream, "id", "stream ID"));

			try {
				version =
					Long.decode(getXmlValue(
						stream,
						"version",
						"stream, '" + id + "', version"));
			}
			catch(NumberFormatException e) {
				throw new DomainException(
					"The stream's, '" +
						id +
						"', version was not a number.", e);
			}

			name = getXmlValue(stream, "name", "stream, '" + id + "', name");

			description =
				getXmlValue(
					stream, 
					"description", 
					"stream, '" +
						id +
						"', description");
			
			Nodes metadatas;
			try {
				metadatas = stream.query("metadata");
			}
			catch(XPathException e) {
				// The XPath is invalid.
				throw new DomainException(e);
			}
			
			if(metadatas.size() > 1) {
				throw new DomainException(
					"Multiple metadata tags were given for '" + id + "'.");
			}
			else if(metadatas.size() == 1) {
				Node metadata = metadatas.get(0);
				
				Nodes withIds = metadata.query("id");
				if(withIds.size() > 1) {
					throw new DomainException(
						"Multiple ID flags were given for the stream '" +
							id +
							"'.");
				}
				else if(withIds.size() == 1) {
					Node withId = withIds.get(0);
					String timestampString = withId.getValue().trim();
					if(timestampString.length() == 0) {
						this.withId = true;
					}
					else {
						Boolean tWithId =
							StringUtils.decodeBoolean(timestampString);
						
						if(tWithId == null) {
							throw new DomainException(
								"The timestamp flag in observer '" +
									id +
									"' is not a valid boolean: " + 
									timestampString);
						}
						else {
							this.withId = tWithId;
						}
					}
				}
				else {
					this.withId = null;
				}
				
				Nodes timestamps = metadata.query("timestamp");
				if(timestamps.size() > 1) {
					throw new DomainException(
						"Multiple timestamp flags were given for the stream '" +
							id +
							"'.");
				}
				else if(timestamps.size() == 1) {
					Node timestamp = timestamps.get(0);
					String timestampString = timestamp.getValue().trim();
					if(timestampString.length() == 0) {
						withTimestamp = true;
					}
					else {
						Boolean tWithTimestamp =
							StringUtils.decodeBoolean(timestampString);
						
						if(tWithTimestamp == null) {
							throw new DomainException(
								"The timestamp flag in observer '" +
									id +
									"' is not a valid boolean: " + 
									timestampString);
						}
						else {
							withTimestamp = tWithTimestamp;
						}
					}
				}
				else {
					withTimestamp = null;
				}
				
				Nodes locations = metadata.query("location");
				if(locations.size() > 1) {
					throw new DomainException(
						"Multiple location flags were given for the stream '" +
							id +
							"'.");
				}
				else if(locations.size() == 1) {
					Node location = locations.get(0);
					String locationString = location.getValue().trim();
					if(locationString.length() == 0) {
						withLocation = true;
					}
					else {
						Boolean tWithLocation =
							StringUtils.decodeBoolean(locationString);
						
						if(tWithLocation == null) {
							throw new DomainException(
								"The location flag in observer '" +
									id +
									"' is not a valid boolean: " + 
									locationString);
						}
						else {
							withLocation = tWithLocation;
						}
					}
				}
				else {
					withLocation = null;
				}
			}
			else {
				withId = null;
				withTimestamp = null;
				withLocation = null;
			}
			
			schemaString = 
				getXmlValue(stream, "schema", "stream, " + id + ", schema");
			schema = validateSchema(schemaString);
			
		}

		/**
		 * Returns id.
		 * 
		 * @return The id.
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns version.
		 * 
		 * @return The version.
		 */
		public long getVersion() {
			return version;
		}

		/**
		 * Returns name.
		 * 
		 * @return The name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns description.
		 * 
		 * @return The description.
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * Returns whether or not records may contain an ID.
		 * 
		 * @return Whether or not records may contain an ID.
		 */
		public Boolean getWithId() {
			return withId;
		}
		
		/**
		 * Returns whether or not records may contain a time stamp.
		 * 
		 * @return Whether or not records may contain a time stamp.
		 */
		public Boolean getWithTimestamp() {
			return withTimestamp;
		}
		
		/**
		 * Returns whether or not records may contain a location.
		 * 
		 * @return Whether or not records may contain a location.
		 */
		public Boolean getWithLocation() {
			return withLocation;
		}

		/**
		 * Returns schema.
		 * 
		 * @return The schema.
		 */
		public JsonParser getSchema() {
			return schema;
		}
		
		/**
		 * Writes this stream to the JSON generator.
		 * 
		 * @param generator The JSON generator to write the output to. This 
		 * 					should already be setup and ready to go.
		 * 
		 * @throws JsonProcessingException There was a problem with the 
		 * 								   generator that prevented data from 
		 * 								   being written.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		public void toJson(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Write the start of this observer's object.
			generator.writeStartObject();
			
			try {
				// Add the ID.
				generator.writeStringField(KEY_JSON_ID, id);
				
				// Add the version.
				generator.writeNumberField(KEY_JSON_VERSION, version);
				
				// Add the name.
				generator.writeStringField(KEY_JSON_NAME, name);
				
				// Add the description.
				generator.writeStringField(KEY_JSON_DESCRIPTION, description);

				// Add the "with ID" boolean if it was given..
				if(withId != null) {
					generator.writeBooleanField(KEY_JSON_WITH_ID, withId);
				}
				
				// Add the "with timestamp" boolean.
				if(withTimestamp != null) {
					generator.writeBooleanField(
						KEY_JSON_WITH_TIMESTAMP, 
						withTimestamp);
				}
				
				// Add the "with location" boolean.
				if(withLocation != null) {
					generator.writeBooleanField(
						KEY_JSON_WITH_LOCATION, 
						withLocation);
				}
				
				// Add the schema.
				generator.writeObjectField(
					KEY_JSON_SCHEMA, 
					schema.readValueAsTree());
			}
			finally {
				// Close this observer's object.
				generator.writeEndObject();
			}
		}
		
		/**
		 * Validates that some data conforms to the schema used when creating
		 * this stream.
		 * 
		 * @param data The data to validate.
		 * 
		 * @return The JsonNode as passed into this function.
		 * 
		 * @throws DomainException The data does not conform to the schema.
		 */
		public JsonNode validateData(JsonNode data) throws DomainException {
			Context context = Context.enter();
			try {
				Scriptable scope = context.initStandardObjects();
				Function concordiaConstructor =
					context.compileFunction(
						scope, 
						JS_SCHEMA, 
						"Concordia.js", 
						1, 
						null);
				
				Scriptable concordia =
					concordiaConstructor.construct(
						context, 
						scope, 
						new Object[] { schemaString }
					);
				
				Object validateData = 
					concordia.get("validateData", concordia);
				if(validateData instanceof Function) {
					Function validateDataFunction = (Function) validateData;
					
					validateDataFunction.call(
						context, 
						scope, 
						validateDataFunction, 
						new Object[] { data.toString() }
					);
				}
				else {
					throw new DomainException(
						"The 'validateData' function is missing.");
				}
			}
			catch(JavaScriptException e) {
				throw new DomainException(
					ErrorCode.OBSERVER_INVALID_STREAM_DATA,
					"The data does not conform to the schema: " + 
						e.getMessage(),
					e);
			}
			finally {
				Context.exit();
			}
			
			return data;
		}
		
		/**
		 * Sanitizes the stream ID and returns it.
		 * 
		 * @param id The ID to be sanitized.
		 * 
		 * @return The sanitized ID.
		 * 
		 * @throws DomainException The ID was not valid.
		 */
		public static String sanitizeId(
				final String id) 
				throws DomainException {
			
			if(id == null) {
				throw new DomainException("The ID is null.");
			}
			
			String trimmedId = id.trim();
			if(! PATTERN_ID_VALIDATOR.matcher(trimmedId).matches()) {
				throw new DomainException(
					ErrorCode.OBSERVER_INVALID_STREAM_ID,
					"The stream ID is invalid. " +
						"It must contain only alphanumeric characters " +
						"and underscores, " +
						"and it cannot be more than 255 characters.");
			}
			
			return trimmedId;
		}
		
		/**
		 * Validates that a schema used to defined a stream is valid.
		 * 
		 * @param schema The stream's schema.
		 * 
		 * @return The schema if it was valid.
		 * 
		 * @throws DomainException The schema was not valid.
		 */
		public static JsonParser validateSchema(
				final String schema)
				throws DomainException {
			
			Context context = Context.enter();
			try {
				Scriptable scope = context.initStandardObjects();
				Function concordiaConstructor =
					context.compileFunction(
						scope, 
						JS_SCHEMA, 
						"Concordia.js", 
						1, 
						null);
				
				concordiaConstructor.construct(
					context, 
					scope, 
					new Object[] { schema });
			}
			catch(JavaScriptException e) {
				throw new DomainException(
					ErrorCode.OBSERVER_INVALID_STREAM_DEFINITION,
					"The schema is invalid: " + e.getMessage(),
					e);
			}
			catch(RhinoException e) {
				throw new DomainException(
					ErrorCode.OBSERVER_INVALID_STREAM_DEFINITION, 
					"A stream definition is not valid JSON.");
			}
			finally {
				Context.exit();
			}
			
			try {
				return JSON_FACTORY.createJsonParser(schema);
			}
			catch(JsonParseException e) {
				throw new DomainException(
					"Validation succeeded, but the schema could not be parsed as JSON.",
					e);
			}
			catch(IOException e) {
				throw new DomainException(
					"Could not read the string value.",
					e);
			}
		}
	}
	private final Map<String, Stream> streams;
	
	/**
	 * Builder for creating new observers.
	 *
	 * @author John Jenkins
	 */
	public static class Builder {
		private String id = null;
		private Long version = null;

		private String name = null;
		private String description = null;
		private String versionString = null;
		
		private Collection<Stream> streams = new LinkedList<Stream>();
		
		/**
		 * Default constructor. Creates an empty observer.
		 */
		public Builder() {};
		
		/**
		 * Sets the ID.
		 * 
		 * @param id The ID.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder setId(final String id) {
			this.id = id;
			
			return this;
		}
		
		/**
		 * Sets the version.
		 * 
		 * @param version The version.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder setVersion(final long version) {
			this.version = version;
			
			return this;
		}
		
		/**
		 * Sets the name.
		 * 
		 * @param name The name.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder setName(final String name) {
			this.name = name;
			
			return this;
		}
		
		/**
		 * Sets the description.
		 * 
		 * @param description The description.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder setDescription(final String description) {
			this.description = description;
			
			return this;
		}
		
		/**
		 * Sets the version string.
		 * 
		 * @param versionString The version string.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder setVersionString(final String versionString) {
			this.versionString = versionString;
			
			return this;
		}
		
		/**
		 * Adds a single stream to the collection of streams. Stream order is
		 * not important or preserved.
		 * 
		 * @param stream The stream to associate with this observer.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder addStream(final Stream stream) {
			streams.add(stream);
			
			return this;
		}
		
		/**
		 * Adds all of the streams to the collection of streams. Stream order 
		 * is not important or preserved.
		 * 
		 * @param streams The collection of streams to associate with this
		 * 				  observer.
		 * 
		 * @return This builder, to allow for chaining.
		 */
		public Builder addStreams(final Collection<Stream> streams) {
			this.streams.addAll(streams);
			
			return this;
		}
		
		/**
		 * Attempts to create the observer.
		 * 
		 * @return The observer based on the values set.
		 * 
		 * @throws DomainException Either, not all of the required values were
		 * 						   given, or one or more of the values were
		 * 						   invalid.
		 */
		public Observer build() throws DomainException {
			return new Observer(
				id, 
				version, 
				name, 
				description, 
				versionString,
				streams);
		}
	}
	
	/**
	 * Private, default constructor. This is entirely broken and should never
	 * be used, but it is required by JAXB. :(
	 */
	@SuppressWarnings("unused")
	private Observer() {
		this.id = null;
		this.version = -1L;
		this.name = null;
		this.description = null;
		this.versionString = null;
		this.streams = null;
	}
	
	/**
	 * Creates a new observer.
	 * 
	 * @param id The universally unique identifier for this observer. This 
	 * 			 should conform to Java's name-spacing. 
	 *  
	 * @param version The version of this observer for internal use.
	 * 
	 * @param name The display name of this observer.
	 * 
	 * @param description A description of what this observer does.
	 * 
	 * @param versionString A string representing the version of this observer.
	 * 
	 * @param streams A collection of Streams for this observer.
	 * 
	 * @throws DomainException Some of the parameters were missing or invalid.
	 */
	public Observer(
			final String id,
			final long version,
			final String name,
			final String description,
			final String versionString,
			final Collection<Stream> streams) 
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
		if(streams == null) {
			throw new DomainException("The streams collection is null.");
		}
		if(streams.size() == 0) {
			throw new DomainException("The streams collection is empty.");
		}

		this.id = sanitizeId(id);
		this.version = version;

		this.name = name.trim();
		this.description = description.trim();
		this.versionString = versionString.trim();

		this.streams = new HashMap<String, Stream>(streams.size());
		for(Stream observer : streams) {
			if(observer == null) {
				continue;
			}

			this.streams.put(observer.id, observer);
		}
	}

	/**
	 * Creates a new Observer based on an XML string.
	 * 
	 * @param xml The XML string.
	 * 
	 * @throws DomainException The XML was not valid XML or some of the  
	 * 						   required elements were missing or invalid.
	 */
	public Observer(final String xml) throws DomainException {
		Node root;
		try {
			Document document = 
				(new nu.xom.Builder()).build(new StringReader(xml));
			
			Nodes roots = document.query("/observer");
			if(roots.size() > 1) {
				throw new DomainException(
					"Multiple observers were defined in the XML.");
			}
			else if(roots.size() == 0) {
				throw new DomainException(
					"No observers were defined in the XML.");
			}
			
			root = roots.get(0);
		}
		catch(XMLException e) {
			// No XML parser is installed in the local class path.
			throw new DomainException(e);
		}
		catch(ValidityException e) {
			// The XML is invalid.
			throw new DomainException("The XML is not valid XML.", e);
		}
		catch(ParsingException e) {
			// The XML is not well formed.
			throw new DomainException("The XML is not valid XML.", e);
		}
		catch(IOException e) {
			// The stream failed!?
			throw new DomainException(e);
		}
		catch(XPathException e) {
			// The XPath for the root node is not valid XPath.
			throw new DomainException(e);
		}

		id = sanitizeId(getXmlValue(root, "id", "observer ID"));

		try {
			version =
				Long.decode(getXmlValue(root, "version", "observer version"));
		}
		catch(NumberFormatException e) {
			throw new DomainException(
				"The observer version was not a number.", 
				e);
		}

		name = getXmlValue(root, "name", "observer name");
		description = getXmlValue(root, "description", "observer description");
		versionString = 
			getXmlValue(root, "versionString", "observer version string");

		Nodes streamList;
		try {
			streamList = root.query("stream");
		}
		catch(XPathException e) {
			// The XPath is invalid.
			throw new DomainException(e);
		}
		
		streams = new HashMap<String, Stream>();
		int streamListLength = streamList.size();
		for(int i = 0; i < streamListLength; i++) {
			Stream stream = new Stream(streamList.get(i));
			streams.put(stream.id, stream);
		}
		if(streams.size() == 0) {
			throw new DomainException("No streams were given.");
		}
	}

	/**
	 * Returns id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns version.
	 * 
	 * @return The version.
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Returns name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns description.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns versionString.
	 * 
	 * @return The versionString.
	 */
	public String getVersionString() {
		return versionString;
	}
	
	/**
	 * Returns the {@link Stream} objects.
	 * 
	 * @return The {@link Stream} objects.
	 */
	@XmlElement(name=KEY_XML_STREAM)
	public Collection<Stream> getStreams() {
		return Collections.unmodifiableCollection(streams.values());
	}

	/**
	 * Returns the map of stream IDs to their concrete {@link Stream} object.
	 * 
	 * @return The map of stream IDs to their concrete {@link Stream} object.
	 */
	public Map<String, Stream> getStreamsMap() {
		return Collections.unmodifiableMap(streams);
	}
	
	/**
	 * Takes a JSON object that represents a single data record, validates it,
	 * and returns it as a DataStream object.
	 * 
	 * @param data A single data record as a JSON object.
	 * 
	 * @return The record as a DataStream object.
	 * 
	 * @throws DomainException The data record was invalid.
	 */
	public DataStream getDataStream(
			final JsonNode data) 
			throws DomainException {
		
		// Get the stream's ID.
		JsonNode idNode = data.get("stream_id");
		if(idNode == null) {
			throw new DomainException("The stream ID is missing.");
		}
		else if(! idNode.isTextual()) {
			throw new DomainException("The stream ID is not a string");
		}
		String streamId = idNode.getTextValue();
		
		// Get the stream's version.
		JsonNode versionNode = data.get("stream_version");
		if(versionNode == null) {
			throw new DomainException("The stream version is missing.");
		}
		else if(! versionNode.isNumber()) {
			throw new DomainException("The version is not a number.");
		}
		long streamVersion = versionNode.getNumberValue().longValue();
		
		// Get the Stream and validate the version.
		Stream currStream = streams.get(streamId);
		if(currStream == null) {
			throw new DomainException(
				"There is no such stream with the given ID: " + streamId);
		}
		if(currStream.getVersion() != streamVersion) {
			throw new DomainException(
				"The stream's version, " + 
					currStream.getVersion() + 
					", does not match the given version: " + 
					streamVersion);
		}
		
		// Get the meta-data, which is optional.
		MetaData metaData = null;
		JsonNode metaDataNode = data.get("metadata");
		if(metaDataNode != null) {
			if(! metaDataNode.isObject()) {
				throw new DomainException(
					"The meta-data is not a JSON object.");
			}

			MetaData.Builder metaDataBuilder = new MetaData.Builder();
			
			Boolean withId = currStream.getWithId();
			metaDataBuilder.setId(metaDataNode); 
			if((withId != null) && 
				(withId == true) && (! metaDataBuilder.hasId())) {
				
				throw
					new DomainException(
						"An ID must be supplied with each point per the definition.");
			}
			else if((withId != null) && 
				(withId == false) && metaDataBuilder.hasId()) {
				
				throw
					new DomainException(
						"An ID cannot be supplied for any point per the definition.");
			}
			
			Boolean withTimestamp = currStream.getWithTimestamp();
			metaDataBuilder.setTimestamp(metaDataNode);
			if((withTimestamp != null) && 
				(withTimestamp == true) && 
				(! metaDataBuilder.hasTimestamp())) {
				
				throw
					new DomainException(
						"A timestamp must be supplied with each point per the definition.");
			}
			else if((withTimestamp != null) && 
				(withTimestamp == false) && metaDataBuilder.hasTimestamp()) {
				
				throw
					new DomainException(
						"A timestamp cannot be supplied for any point per the definition.");
			}
			
			Boolean withLocation = currStream.getWithLocation();
			metaDataBuilder.setLocation(metaDataNode);
			if((withLocation != null) && 
				(withLocation == true) && (! metaDataBuilder.hasLocation())) {
				
				throw
					new DomainException(
						"A location must be supplied with each point per the definition.");
			}
			else if((withLocation != null) && 
				(withLocation == false) && metaDataBuilder.hasLocation()) {
				
				throw
					new DomainException(
						"A location cannot be supplied for any point per the definition.");
			}
			
			metaData = metaDataBuilder.build();
		}

		// Get the data which may be JSON or may be binary.
		DataStream result;
		JsonNode dataNode = data.get("data");
		if(dataNode == null) {
			throw new DomainException("The data is missing.");
		}
		dataNode = currStream.validateData(dataNode);
		
		result = 
			new DataStream(
				currStream, 
				metaData, 
				dataNode);
		
		return result;
	}
	
	/**
	 * Writes this observer to the JSON generator.
	 * 
	 * @param generator The JSON generator to write the output to. This should
	 * 					already be setup and ready to go.
	 * 
	 * @throws JsonProcessingException There was a problem with the generator
	 * 								   that prevented data from being written.
	 * 
	 * @throws IOException There was a problem writing to the generator.
	 */
	public void toJson(
			final JsonGenerator generator)
			throws JsonProcessingException, IOException {
		
		// Write the start of this observer's object.
		generator.writeStartObject();
		
		try {
			// Add the ID.
			generator.writeStringField(KEY_JSON_ID, id);
			
			// Add the version.
			generator.writeNumberField(KEY_JSON_VERSION, version);
			
			// Add the name.
			generator.writeStringField(KEY_JSON_NAME, name);
			
			// Add the description.
			generator.writeStringField(KEY_JSON_DESCRIPTION, description);
			
			// Add the version string.
			generator.writeStringField(KEY_JSON_VERSION_STRING, versionString);
			
			// Add the observer's streams.
			generator.writeArrayFieldStart(KEY_JSON_STREAMS);
			try {
				for(Stream stream : streams.values()) {
					stream.toJson(generator);
				}
			}
			finally {
				generator.writeEndArray();
			}
		}
		finally {
			// Close this observer's object.
			generator.writeEndObject();
		}
	}

	/**
	 * Sanitizes the observer's ID to ensure that it conforms, at least to some
	 * degree, to Java's name-spacing and doesn't contain any illegal 
	 * characters.
	 * 
	 * @param id The ID to sanitize.
	 * 
	 * @return The sanitized ID.
	 * 
	 * @throws DomainException The ID could not be sanitized because it was 
	 * 						   invalid.
	 */
	public static String sanitizeId(
			final String id)
			throws DomainException {

		if(id == null) {
			throw new DomainException("The ID is null.");
		}

		String trimmedId = id.trim();
		if(! PATTERN_ID_VALIDATOR.matcher(trimmedId).matches()) {
			throw new DomainException(
				ErrorCode.OBSERVER_INVALID_ID,
				"The observer ID is invalid. " +
					"It must consist of only alphanumeric values, " +
					"begin with a letter, " +
					"include at least one '.', " +
					"and cannot end in a '.': " +
					trimmedId);
		}
		else if(trimmedId.length() > MAX_OBSERVER_ID_LENGTH) {
			throw new DomainException(
				ErrorCode.OBSERVER_INVALID_ID,
				"The observer ID cannot be more than " +
					MAX_OBSERVER_ID_LENGTH +
					" long: " +
					trimmedId);
		}

		return trimmedId;
	}

	/**
	 * Parses the current node of an XML document and returns the requested
	 * tag's value.
	 * 
	 * @param node The node in which to perform the parsing.
	 * 
	 * @param tagName The tag name to search for within this node.
	 * 
	 * @param errorName A string describing the tag being parsed which will be
	 * 					used to construct the error message if it is invalid.
	 * 
	 * @return The tag's value.
	 * 
	 * @throws DomainException Multiple or no tags with that name exist or the
	 * 						   tag isn't a string tag and, therefore, doesn't
	 * 						   have a value.
	 */
	private static String getXmlValue(
			final Node node,
			final String tagName,
			final String errorName)
			throws DomainException {

		String result = null;
		Nodes results;
		try {
			results = node.query(tagName);
		}
		catch(XPathException e) {
			// The XPath expression is invalid.
			throw new DomainException(e);
		}
		
		if(results.size() > 1) {
			throw new DomainException(
				"Multiple " +
					errorName +
					"s were defined in the XML.");
		}
		else if(results.size() == 1) {
			try {
				result = results.get(0).getValue();
			}
			catch(DOMException e) {
				throw new DomainException(
					"The " +
						errorName +
						" was not a value.", 
					e);
			}
		}
		if(result == null || result.trim().length() == 0) {
			throw new DomainException("The " + errorName + " is missing.");
		}
		else {
			return result.trim();
		}
	}
}