package org.ohmage.domain;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;
import nu.xom.XPathException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaParseException;
import org.codehaus.jackson.JsonNode;
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
public class Observer {
	private static final Pattern PATTERN_ID_VALIDATOR = 
		Pattern.compile("([a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)+){3,255}");

	private final String id;
	private final long version;

	private final String name;
	private final String description;
	private final String versionString;

	/**
	 * This class represents the definition of a single stream of data. This
	 * class is immutable and, therefore, thread-safe.
	 *
	 * @author John Jenkins
	 */
	public static class Stream {
		private static final Pattern PATTERN_ID_VALIDATOR = 
			Pattern.compile("[a-zA-Z0-9_]{1,255}");
		
		private final String id;
		private final long version;

		private final String name;
		private final String description;
		
		// NULL or true means that it should be checked for, however, false
		// means that it should explicitly be ignored.
		private final boolean withId;
		private final boolean withTimestamp;
		private final boolean withLocation;

		private final Schema schema;

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
				final boolean withId,
				final boolean withTimestamp,
				final boolean withLocation,
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

			try {
				this.schema = (new Parser()).parse(schema);
			}
			catch(SchemaParseException e) {
				throw new DomainException("The schema was invalid.", e);
			}
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
					this.withId = false;
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
					withTimestamp = false;
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
					withLocation = false;
				}
			}
			else {
				withId = false;
				withTimestamp = false;
				withLocation = false;
			}
			
			try {
				schema =
					(new Parser()).parse(
						getXmlValue(
							stream, 
							"schema", 
							"stream, " +
								id +
								", schema"));
			}
			catch(SchemaParseException e) {
				throw new DomainException("The schema was invalid.", e);
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
		 * Returns whether or not records may contain an ID.
		 * 
		 * @return Whether or not records may contain an ID.
		 */
		public boolean getWithId() {
			return withId;
		}
		
		/**
		 * Returns whether or not records may contain a time stamp.
		 * 
		 * @return Whether or not records may contain a time stamp.
		 */
		public boolean getWithTimestamp() {
			return withTimestamp;
		}
		
		/**
		 * Returns whether or not records may contain a location.
		 * 
		 * @return Whether or not records may contain a location.
		 */
		public boolean getWithLocation() {
			return withLocation;
		}

		/**
		 * Returns schema.
		 * 
		 * @return The schema.
		 */
		public Schema getSchema() {
			return schema;
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
	}
	private final Map<String, Stream> streams;
	
	public static class Builder {
		private String id = null;
		private Long version = null;

		private String name = null;
		private String description = null;
		private String versionString = null;
		
		private Collection<Stream> streams = new LinkedList<Stream>();
		
		public Builder() {};
		
		public Builder setId(final String id) {
			this.id = id;
			
			return this;
		}
		
		public Builder setVersion(final long version) {
			this.version = version;
			
			return this;
		}
		
		public Builder setName(final String name) {
			this.name = name;
			
			return this;
		}
		
		public Builder setDescription(final String description) {
			this.description = description;
			
			return this;
		}
		
		public Builder setVersionString(final String versionString) {
			this.versionString = versionString;
			
			return this;
		}
		
		public Builder addStream(final Stream stream) {
			streams.add(stream);
			
			return this;
		}
		
		public Builder addStreams(final Collection<Stream> streams) {
			this.streams.addAll(streams);
			
			return this;
		}
		
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
	 * Returns streams.
	 * 
	 * @return The streams.
	 */
	public Map<String, Stream> getStreams() {
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
					"The meta-dat is not a JSON object.");
			}

			MetaData.Builder metaDataBuilder = new MetaData.Builder();
			
			if(currStream.getWithId()) {
				metaDataBuilder.setId(metaDataNode);
			}
			
			if(currStream.getWithTimestamp()) {
				metaDataBuilder.setTimestamp(metaDataNode);
			}
			
			if(currStream.getWithLocation()) {
				metaDataBuilder.setLocation(metaDataNode);
			}
			
			metaData = metaDataBuilder.build();
		}

		// Get the data which may be JSON or may be binary.
		DataStream result;
		JsonNode dataNode = data.get("data");
		if(dataNode == null) {
			throw new DomainException("The data is missing.");
		}
		
		result = 
			new DataStream(
				currStream, 
				metaData, 
				dataNode.toString());
		
		return result;
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
				"The observer is invalid. " +
					"It must consist of only alphanumeric values, " +
					"include at least one '.', " +
					"cannot end in a '.', " +
					"and cannot be longer than 255 characters: " +
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