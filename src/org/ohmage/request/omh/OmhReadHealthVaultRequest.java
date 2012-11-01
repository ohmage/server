package org.ohmage.request.omh;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.service.OmhServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.OmhValidators;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.hsg.Connection;
import com.microsoft.hsg.ConnectionFactory;
import com.microsoft.hsg.HVAccessor;
import com.microsoft.hsg.HVException;
import com.microsoft.hsg.Request;
import com.microsoft.hsg.Response;
import com.microsoft.hsg.thing.oxm.jaxb.base.Person;
import com.microsoft.hsg.thing.oxm.jaxb.condition.Condition;
import com.microsoft.hsg.thing.oxm.jaxb.medication.Medication;

public class OmhReadHealthVaultRequest
		extends UserRequest
		implements OmhReadResponder {
	
	private static final Logger LOGGER = 
		Logger.getLogger(OmhReadHealthVaultRequest.class);
	
	private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER =
		ISODateTimeFormat.dateTime();
	
	/**
	 * The super class for all HealthVault Things that can be read.
	 *
	 * @author John Jenkins
	 */
	public abstract static class HealthVaultThing implements OmhReadResponder {
		// The 'codable-value' fields.
		private static final String JSON_KEY_CODABLE_VALUE_TEXT = "text";
		private static final String JSON_KEY_CODABLE_VALUE_CODE = "code";
		private static final String JSON_KEY_CODABLE_VALUE_CODE_VALUE = 
			"value";
		private static final String JSON_KEY_CODABLE_VALUE_CODE_FAMILY = 
			"family";
		private static final String JSON_KEY_CODABLE_VALUE_CODE_TYPE = 
			"type";
		private static final String JSON_KEY_CODABLE_VALUE_CODE_VERSION = 
			"version";
		
		// The 'general-measurement' fields.
		private static final String JSON_KEY_GENERAL_MEASUREMENT_DISPLAY = 
			"display";
		private static final String JSON_KEY_GENERAL_MEASUREMENT_STRUCTURED = 
			"structured";
		
		// The 'structured-measurement' fields.
		private static final String JSON_KEY_STRUCTURED_MEASUREMENT_VALUE = 
			"value";
		private static final String JSON_KEY_STRUCTURED_MEASUREMENT_UNITS = 
			"units";
		
		// The 'approx-date-time' fields.
		private static final String JSON_KEY_APPROX_DATE_TIME_DESCRIPTIVE =
			"descriptive";
		private static final String JSON_KEY_APPROX_DATE_TIME_STRUCTURED =
			"structured";
		private static final String JSON_KEY_APPROX_DATE_TIME_STRUCTURED_DATE =
			"date";
		private static final String JSON_KEY_APPROX_DATE_TIME_STRUCTURED_TIME =
			"time";
		private static final String JSON_KEY_APPROX_DATE_TIME_STRUCTURED_TZ =
			"tz";
		
		// The 'date' fields.
		private static final String JSON_KEY_DATE_YEAR = "y";
		private static final String JSON_KEY_DATE_MONTH = "m";
		private static final String JSON_KEY_DATE_DAY = "d";
		private static final String JSON_KEY_DATE_HOUR = "h";
		private static final String JSON_KEY_DATE_MINUTE = "m";
		private static final String JSON_KEY_DATE_SECOND = "s";
		private static final String JSON_KEY_DATE_MILLISECOND = "f";
		
		// The 'person' fields.
		private static final String JSON_KEY_PERSON_NAME = "name";
		private static final String JSON_KEY_PERSON_ORGANIZATION = 
			"organization";
		private static final String JSON_KEY_PERSON_PROFESSIONAL_TRAINING = 
			"professional-training";
		private static final String JSON_KEY_PERSON_ID = "id";
		private static final String JSON_KEY_PERSON_CONTACT = "contact";
		private static final String JSON_KEY_PERSON_TYPE = "type";
		
		// The 'name' fields.
		private static final String JSON_KEY_NAME_FULL = "full";
		private static final String JSON_KEY_NAME_TITLE = "title";
		private static final String JSON_KEY_NAME_FIRST = "first";
		private static final String JSON_KEY_NAME_MIDDLE = "middle";
		private static final String JSON_KEY_NAME_LAST = "last";
		private static final String JSON_KEY_NAME_SUFFIX = "suffix";
		
		// The 'contact' fields.
		private static final String JSON_KEY_CONTACT_ADDRESS = "address";
		private static final String JSON_KEY_CONTACT_PHONE = "phone";
		private static final String JSON_KEY_CONTACT_EMAIL = "email";
		
		// The 'address' fields.
		private static final String JSON_KEY_ADDRESS_DESCRIPTION = 
			"description";
		private static final String JSON_KEY_ADDRESS_IS_PRIMARY = "is-primary";
		private static final String JSON_KEY_ADDRESS_STREET = "street";
		private static final String JSON_KEY_ADDRESS_CITY = "city";
		private static final String JSON_KEY_ADDRESS_STATE = "state";
		private static final String JSON_KEY_ADDRESS_POSTCODE = "postcode";
		private static final String JSON_KEY_ADDRESS_COUNTRY = "country";
		private static final String JSON_KEY_ADDRESS_COUNTY = "county";
		
		// The 'phone' fields.
		private static final String JSON_KEY_PHONE_DESCRIPTION = "description";
		private static final String JSON_KEY_PHONE_IS_PRIMARY = "is-primary";
		private static final String JSON_KEY_PHONE_NUMBER = "number";
		
		// The 'email' fields.
		private static final String JSON_KEY_EMAIL_DESCRIPTION = "description";
		private static final String JSON_KEY_EMAIL_IS_PRIMARY = "is-primary";
		private static final String JSON_KEY_EMAIL_ADDRESS = "address";
		
		/**
		 * The name of the Thing.
		 */
		private final String name;
		
		/**
		 * A flag to indicate if this request has been made yet or not.
		 */
		private boolean madeRequest = false;
		
		/**
		 * Defines a HealthVault Thing.
		 * 
		 * @param name The name of the thing to retrieve.
		 * 
		 * @throws IllegalArgumentException The path is null or only 
		 * 									whitespace.
		 */
		protected HealthVaultThing(final String name) {
			if(name == null) {
				throw new IllegalArgumentException("The name is null.");
			}
			else if(name.trim().length() == 0) {
				throw new IllegalArgumentException(
					"The name is all whitespace.");
			}
			
			this.name = name;
		}
		
		/**
		 * Returns this Thing's name.
		 * 
		 * @return This Thing's name.
		 */
		public final String getName() {
			return name;
		}
		
		/**
		 * Returns whether or not the records at this API will have an ID 
		 * associated with each one.
		 * 
		 * @return Whether or not these records have IDs.
		 */
		public abstract boolean hasId();
		
		/**
		 * Returns whether or not the records at this API will have a timestamp
		 * associated with each one.
		 * 
		 * @return Whether or not these records have timestamps.
		 */
		public abstract boolean hasTimestamp();
		
		/**
		 * Returns whether or not the records at this API will have a location
		 * associated with each one.
		 * 
		 * @return Whether or not these records have locations.
		 */
		public abstract boolean hasLocation();
		
		/**
		 * Creates the registry entry for this RunKeeper API.
		 * 
		 * @param generator The generator to use to write the definition.
		 * 
		 * @throws JsonGenerationException There was an error creating the 
		 * 								   JSON.
		 * 
		 * @throws IOException There was an error writing to the generator.
		 */
		public void writeRegistryEntry(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// RunKeeper definition
			generator.writeStartObject();
			
			// Output the chunk size which will be the same for all 
			// observers.
			generator.writeNumberField(
				"chunk_size", 
				StreamReadRequest.MAX_NUMBER_TO_RETURN);
			
			// There are no external IDs yet. This may change to
			// link to observer/read, but there are some
			// discrepancies in the parameters.
			
			// Set the local timezone as authoritative.
			generator.writeBooleanField(
				"local_tz_authoritative",
				true);
			
			// Set the summarizable as false for the time being.
			generator.writeBooleanField("summarizable", false);
			
			// Set the payload ID.
			StringBuilder payloadIdBuilder = 
				new StringBuilder("urn:health_vault:");
			payloadIdBuilder.append(name);
			generator.writeStringField(
				"payload_id", 
				payloadIdBuilder.toString());
			
			// Set the payload version. For now, all surveys have 
			// the same version, 1.
			generator.writeStringField(
				"payload_version", 
				"1");
			
			// Set the payload definition.
			generator.writeFieldName("payload_definition"); 
			toConcordia(generator);

			// End the RunKeeper definition.
			generator.writeEndObject();
		}
		
		/**
		 * Generates the Concordia schema for this path.
		 * 
		 * @param generator The generator to use to write the definition.
		 * 
		 * @return The 'generator' that was passed in to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		public abstract JsonGenerator toConcordia(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException;
		
		/**
		 * Makes the request to the API and stores the received data.
		 * 
		 * @param recordId The HealthVault ID for offline access for this user.
		 * 
		 * @param personId The HealthVault ID for the user. 
		 * 
		 * @param startDate Limits the data to only those points on or after
		 * 					this date and time.
		 * 
		 * @param endDate Limits the data to only those points on or before 
		 * 				  this date and time.
		 * 
		 * @param numToSkip This represents the number of records that will be
		 * 					skipped. Records are in reverse-chronological
		 * 					order.
		 * 
		 * @param numToReturn This represents the number of records that will 
		 * 					  be returned. This is processed after records have
		 * 					  been skipped.
		 * 
		 * @throws DomainException There was an error making the call.
		 */
		public final void service(
				final String recordId,
				final String personId,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			if(madeRequest) {
				return;
			}
			
			makeRequest(
				recordId, 
				personId, 
				startDate, 
				endDate, 
				numToSkip, 
				numToReturn);
			
			madeRequest = true;
		}
		
		/**
		 * Makes the request and returns the list of Medications that matched 
		 * the given parameters.
		 * 
		 * @param recordId The HealthVault ID for offline access for this user.
		 * 
		 * @param personId The HealthVault ID for the user.
		 * 
		 * @param startDate Limits the data to only those points on or after
		 * 					this date and time.
		 * 
		 * @param endDate Limits the data to only those points on or before 
		 * 				  this date and time.
		 * 
		 * @param numToSkip This represents the number of records that will be
		 * 					skipped. Records are in reverse-chronological
		 * 					order.
		 * 
		 * @param numToReturn This represents the number of records that will 
		 * 					  be returned. This is processed after records have
		 * 					  been skipped.
		 * 
		 * @throws DomainException There was a problem reading or decoding the 
		 * 						   data.
		 */
		protected abstract void makeRequest(
				final String recordId, 
				final String personId,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException;

		/**
		 * Makes the request and returns the list of Medications that matched 
		 * the given parameters.
		 * 
		 * @param recordId The HealthVault ID for offline access for this user.
		 * 
		 * @param personId The HealthVault ID for the user.
		 * 
		 * @param startDate Limits the data to only those points on or after
		 * 					this date and time.
		 * 
		 * @param endDate Limits the data to only those points on or before 
		 * 				  this date and time.
		 * 
		 * @param numToSkip The maximum number of results to return.
		 * 
		 * @return The list of medications that match the criteria.
		 * 
		 * @throws DomainException There was a problem reading or decoding the 
		 * 						   data.
		 */
		protected NodeList makeRequest(
				final String recordId, 
				final String personId,
				final DateTime startDate,
				final DateTime endDate,
				final long maxToReturn)
				throws DomainException {
			
			// Reads the configuration information from the HealthVault 
			// configuration and keystore files in the classpath.
			Connection connection = ConnectionFactory.getConnection();
			HVAccessor accessor = new HVAccessor();
			
			Request request = new Request();
			request.setMethodName("GetThings");
			request.setMethodVersion("3");
			request.setRecordId(recordId);
			request.setOfflineUserId(personId);
			request
				.setInfo(
					"<info>" +
						// Limit the number of results to return.
						"<group max=\"" + maxToReturn + "\">" +
							// Filter based on the type's ID and the start and
							// end dates, if given.
							"<filter>" +
								// Specify the Medication ID.
								"<type-id>" +
									getTypeId() +
								"</type-id>" +
								// Start date - ISO 8601
								((startDate == null) ?
									"" :
									"<eff-date-min>" +
										ISO_DATE_TIME_FORMATTER
											.print(startDate) +
									"</eff-date-min>") +
								// End date - ISO 8601
								((endDate == null) ?
									"" :
									"<eff-date-max>" +
										ISO_DATE_TIME_FORMATTER
											.print(endDate) +
									"</eff-date-max>") +
							"</filter>" +
							// Specifies how to format the response.
							"<format>" +
								// Return all of the data as XML, untouched.
								"<xml></xml>" +
							"</format>" +
						"</group>" +
					"</info>");
			
			// Make the request
			try {
				accessor.send(request, connection);
			}
			catch(HVException e) {
				throw
					new DomainException(
						"There was a problem with the request.",
						e);
			}
			
			// Create a default document builder for parsing the XML.
			DocumentBuilder documentBuilder;
			try {
				// Create the document builder.
				documentBuilder = 
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
			}
			catch(FactoryConfigurationError e) {
				throw
					new DomainException(
						"Couldn't create the document builder factory.",
						e);
			}
			catch(ParserConfigurationException e) {
				throw
					new DomainException(
						"Couldn't create the document builder.",
						e);
			}
			
			// Create the document that represents the root of the XML.
			Document document;
			Response response = accessor.getResponse();
			try {
				document = documentBuilder.parse(response.getInputStream());
			}
			catch(SAXException e) {
				throw
					new DomainException(
						"The response was not valid XML.",
						e);
			}
			catch(IOException e) {
				throw
					new DomainException(
						"There was an error reading the response from the server.",
						e);
			}
			
			// Create the XPath object to use to query the document.
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			// Get the appropriate tags from the XML using the XPath and return
			// them.
			try {
				return
					(NodeList) xPath
						.evaluate(
							getResponseXpath(),
							document, 
							XPathConstants.NODESET);
			}
			catch(XPathExpressionException e) {
				throw
					new DomainException("The XPath expression is invalid.", e);
			}
		}
		
		/**
		 * Returns the type UUID for this type.
		 *  
		 * @return The type's UUID.
		 */
		protected abstract String getTypeId();
		
		/**
		 * Returns the XPath String that is used to query the HealthVault XML
		 * and retrieve the data Nodes.
		 * 
		 * @return The XPath String for retrieving data from the returned
		 * 		   HealthVault XML.
		 */
		protected abstract String getResponseXpath();
		
		/**
		 * Writes the Concordia schema for a HealthVault "date".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeDateConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "year" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_YEAR);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Add the "month" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_MONTH);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Add the "day" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_DAY);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "approx-date".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeApproxDateConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "year" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_YEAR);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Add the "month" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_MONTH);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "day" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_DAY);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "approx-date".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeTimeConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "hour" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_HOUR);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Add the "minute" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_MINUTE);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Add the "second" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_SECOND);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "millisecond" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_DATE_MILLISECOND);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "approx-date-time".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeApproxDateTimeConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Both the "display" and "structured" fields will exist, and 
			// exactly one will be populated and the other null.
			
			// Add the "display" field.
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				JSON_KEY_APPROX_DATE_TIME_DESCRIPTIVE);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "structured" field.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_APPROX_DATE_TIME_STRUCTURED);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", true);
			generator.writeArrayFieldStart("schema");
			
			// Add the approximate date.
			writeApproxDateConcordia(
				generator,
				JSON_KEY_APPROX_DATE_TIME_STRUCTURED_DATE,
				false);
			
			// Add the optional time.
			writeTimeConcordia(
				generator,
				JSON_KEY_APPROX_DATE_TIME_STRUCTURED_TIME,
				true);
			
			// Add the "tz" field.
			writeCodableValueConcordia(
				generator, 
				JSON_KEY_APPROX_DATE_TIME_STRUCTURED_TZ,
				true);
			
			// End the "structured" schema.
			generator.writeEndArray();
			
			// End the "structured" field.
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "codable-value".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeCodableValueConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "text" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_CODABLE_VALUE_TEXT);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "code" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_CODABLE_VALUE_CODE);
			generator.writeStringField("type", "array");
			generator.writeBooleanField("optional", true);
			generator.writeObjectFieldStart("schema");
			
			// Each index in this array is an object.
			generator.writeStringField("type", "object");
			generator.writeArrayFieldStart("schema");
			
			// Add the "code"'s value field.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_CODABLE_VALUE_CODE_VALUE);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "code"'s family field.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_CODABLE_VALUE_CODE_FAMILY);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "code"'s type field.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_CODABLE_VALUE_CODE_TYPE);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "code"'s version field.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_CODABLE_VALUE_CODE_VERSION);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// End the "code"'s array index schema.
			generator.writeEndArray();
			
			// End the "code"'s schema.
			generator.writeEndObject();
			
			// End the "code" field.
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "general-measurement".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeGeneralMeasurementConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "display" field.
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				JSON_KEY_GENERAL_MEASUREMENT_DISPLAY);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Write the "structured" definition.
			writeStructuredMeasurementConcordia(
				generator, 
				JSON_KEY_GENERAL_MEASUREMENT_STRUCTURED, 
				true);
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault
		 * "structured-measurement".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeStructuredMeasurementConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "array");
			generator.writeBooleanField("optional", optional);
			generator.writeObjectFieldStart("schema");
			
			// Each element in the array is an object.
			generator.writeStringField("type", "object");
			generator.writeArrayFieldStart("schema");
			
			// Add the "value" field.
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				JSON_KEY_STRUCTURED_MEASUREMENT_VALUE);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Write the "units" definition.
			writeCodableValueConcordia(
				generator, 
				JSON_KEY_STRUCTURED_MEASUREMENT_UNITS,
				false);
			
			// End the array indices definition.
			generator.writeEndArray();
			
			// End the root schema.
			generator.writeEndObject();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "name".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeNameConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "full" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_NAME_FULL);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "title" field.
			writeCodableValueConcordia(generator, JSON_KEY_NAME_TITLE, true);
			
			// Add the "first" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_NAME_FIRST);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "middle" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_NAME_MIDDLE);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "last" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_NAME_LAST);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "suffix" field.
			writeCodableValueConcordia(generator, JSON_KEY_NAME_SUFFIX, true);
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "contact".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeContactConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "address" field.
			writeAddressConcordia(
				generator, 
				JSON_KEY_CONTACT_ADDRESS, 
				true);
			
			// Add the "phone" field.
			writePhoneConcordia(
				generator, 
				JSON_KEY_CONTACT_PHONE, 
				true);
			
			// Add the "email" field.
			writeEmailConcordia(
				generator, 
				JSON_KEY_CONTACT_EMAIL, 
				true);
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "address".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeAddressConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "array");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "description" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_DESCRIPTION);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "is-primary" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_IS_PRIMARY);
			generator.writeStringField("type", "boolean");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "street" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_STREET);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "city" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_CITY);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "state" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_STATE);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "postcode" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_POSTCODE);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "country" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_COUNTRY);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the "county" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_ADDRESS_COUNTY);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "phone".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writePhoneConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "array");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "description" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_PHONE_DESCRIPTION);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "is-primary" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_PHONE_IS_PRIMARY);
			generator.writeStringField("type", "boolean");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "street" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_PHONE_NUMBER);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "email".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writeEmailConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "array");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "description" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_EMAIL_DESCRIPTION);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "is-primary" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_EMAIL_IS_PRIMARY);
			generator.writeStringField("type", "boolean");
			generator.writeBooleanField("optional", optional);
			generator.writeEndObject();
			
			// Add the "street" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_EMAIL_ADDRESS);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "person".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writePersonConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Add the "person" definition.
			writeNameConcordia(generator, JSON_KEY_PERSON_NAME, true);
			
			// Add the "organization" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_PERSON_ORGANIZATION);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "professional-training" field.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_PERSON_PROFESSIONAL_TRAINING);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "id" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_PERSON_ID);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the "contact" definition.
			writeContactConcordia(generator, JSON_KEY_PERSON_CONTACT, true);
			
			// Add the "type" definition.
			writeCodableValueConcordia(generator, JSON_KEY_PERSON_TYPE, true);
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
	}
	
	/**
	 * The HealthVault Thing for Medications.
	 *
	 * @author John Jenkins
	 */
	public static class MedicationThing extends HealthVaultThing {
		/**
		 * The name of this thing.
		 */
		private static final String NAME = "medication";
		/**
		 * The type ID for this type as defined by HealthVault.
		 */
		private static final String TYPE_ID = 
			"30cafccc-047d-4288-94ef-643571f7919d";
		/**
		 * The XPath to use to get the medications.
		 */
		private static final String XPATH = "//medication";
		
		// The root fields.
		private static final String JSON_KEY_NAME = "name";
		private static final String JSON_KEY_GENERIC_NAME = "generic-name";
		private static final String JSON_KEY_DOSE = "dose";
		private static final String JSON_KEY_STRENGTH = "strength";
		private static final String JSON_KEY_FREQUENCY = "frequency";
		private static final String JSON_KEY_ROUTE = "route";
		private static final String JSON_KEY_INDICATION = "indication";
		private static final String JSON_KEY_DATE_STARTED = "date-started";
		private static final String JSON_KEY_DATE_DISCONTINUED = 
			"date-discontinued";
		private static final String JSON_KEY_PRESCRIBED = "prescribed";
		private static final String JSON_KEY_PRESCRIPTION = "prescription";
		
		// The Prescription fields.
		private static final String JSON_KEY_PRESCRIPTION_PRESCRIBED_BY = 
			"prescribedBy";
		private static final String JSON_KEY_PRESCRIPTION_DATE_PRESCRIBED = 
			"date-prescribed";
		private static final String JSON_KEY_PRESCRIPTION_AMOUNT_PRESCRIBED = 
			"amount-prescribed";
		private static final String JSON_KEY_PRESCRIPTION_SUBSTITUTION = 
			"substitution";
		private static final String JSON_KEY_PRESCRIPTION_REFILLS = "refills";
		private static final String JSON_KEY_PRESCRIPTION_DAYS_SUPPLY = 
			"days-supply";
		private static final String JSON_KEY_PRESCRIPTION_PRESCRIPTION_EXPIRATION = 
			"prescription-expiration";
		private static final String JSON_KEY_PRESCRIPTION_INSTRUCTIONS = 
			"instructions";
		
		/**
		 * The medications retrieved from HealthVault. This will always be 
		 * empty until
		 * {@link #service(String, String, DateTime, DateTime, long, long)} has
		 * been called.
		 */
		private final List<Medication> medications =
			new LinkedList<Medication>();

		/**
		 * Creates a {@link HealthVaultThing} for medications.
		 */
		public MedicationThing() {
			super(NAME);
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasId()
		 */
		@Override
		public boolean hasId() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasTimestamp()
		 */
		@Override
		public boolean hasTimestamp() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasLocation()
		 */
		@Override
		public boolean hasLocation() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#toConcordia(org.codehaus.jackson.JsonGenerator)
		 */
		@Override
		public JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Begin the definition.
			generator.writeStartObject();
			
			// The type of the data is a JSON object.
			generator.writeStringField("type", "object");
			
			// Define that object.
			generator.writeArrayFieldStart("schema");
			
			// Write the "name" field.
			writeCodableValueConcordia(generator, JSON_KEY_NAME, false);
			
			// Write the "generic-name" field.
			writeCodableValueConcordia(generator, JSON_KEY_GENERIC_NAME, true);
			
			// Write the "dose" field.
			writeGeneralMeasurementConcordia(generator, JSON_KEY_DOSE, true);
			
			// Write the "strength" field.
			writeGeneralMeasurementConcordia(
				generator, 
				JSON_KEY_STRENGTH, 
				true);
			
			// Write the "frequency" field.
			writeGeneralMeasurementConcordia(
				generator, 
				JSON_KEY_FREQUENCY, 
				true);

			// Write the "route" field.
			writeCodableValueConcordia(generator, JSON_KEY_ROUTE, true);
			
			// Write the "indication" field.
			writeCodableValueConcordia(generator, JSON_KEY_INDICATION, true);
			
			// Write the "date-started" field.
			writeApproxDateTimeConcordia(
				generator,
				JSON_KEY_DATE_STARTED,
				true);

			// Write the "date-discontinued" field.
			writeApproxDateTimeConcordia(
				generator,
				JSON_KEY_DATE_DISCONTINUED,
				true);

			// Write the "prescribed" field.
			writeCodableValueConcordia(generator, JSON_KEY_PRESCRIBED, true);

			// Write the "prescription" field.
			writePrescriptionConcordia(generator, JSON_KEY_PRESCRIPTION, true);

			// Finish defining the overall object.
			generator.writeEndArray();
			
			// Close the definition.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#getTypeId()
		 */
		@Override
		protected String getTypeId() {
			return TYPE_ID;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#getResponseXpath()
		 */
		@Override
		protected String getResponseXpath() {
			return XPATH;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#makeRequest(java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
		 */
		@Override
		protected void makeRequest(
				final String recordId,
				final String personId,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			// Create the unmarshaller, which will take the XML and convert it
			// into Medication objects.
			Unmarshaller unmarshaller;
			try {
				unmarshaller = 
					JAXBContext
						.newInstance(Medication.class).createUnmarshaller();
			}
			catch(JAXBException e) {
				throw 
					new DomainException(
						"Could not create the unmarshaller for the medications.",
						e);
			}
			
			// Make the request to HealthVault.
			NodeList medicationNodes =
				makeRequest(
					recordId, 
					personId, 
					startDate, 
					endDate, 
					numToSkip + numToReturn);
			
			// For each of the medication nodes, convert it into a Medication
			// object.
			int numMedications = medicationNodes.getLength();
			try {
				for(int i = 0; i < numMedications; i++) {
					medications
						.add(
							(Medication) unmarshaller
								.unmarshal(medicationNodes.item(i)));
				}
			}
			catch(JAXBException e) {
				throw
					new DomainException(
						"There was an error unmarshalling a medication.",
						e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
		 */
		@Override
		public long getNumDataPoints() {
			return medications.size();
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator, org.ohmage.request.observer.StreamReadRequest.ColumnNode)
		 */
		@Override
		public void respond(
				final JsonGenerator generator,
				final ColumnNode<String> columns)
				throws JsonGenerationException, IOException, DomainException {
			
			// For each object,
			for(Medication medication : medications) {
				// Start the overall object.
				generator.writeStartObject();
				
				// Write the data.
				generator.writeObjectField("data", medication);
				
				// End the overall object.
				generator.writeEndObject();
			}
		}
		
		/**
		 * Writes the Concordia schema for a HealthVault "Prescription".
		 * 
		 * @param generator The generator to use to write the schema.
		 * 
		 * @param name The name of this field.
		 * 
		 * @param optional Whether or not this field is optional.
		 * 
		 * @return The generator to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		protected static final JsonGenerator writePrescriptionConcordia(
				final JsonGenerator generator,
				final String name,
				final boolean optional)
				throws JsonGenerationException, IOException {
			
			// Add the root field.
			generator.writeStartObject();
			generator.writeStringField("name", name);
			generator.writeStringField("type", "object");
			generator.writeBooleanField("optional", optional);
			generator.writeArrayFieldStart("schema");
			
			// Write the "prescribed-by" definition.
			writePersonConcordia(
				generator, 
				JSON_KEY_PRESCRIPTION_PRESCRIBED_BY,
				false);
			
			// Write the "date-prescribed" definition.
			writeApproxDateTimeConcordia(
				generator, 
				JSON_KEY_PRESCRIPTION_DATE_PRESCRIBED,
				true);
			
			// Write the "amount_prescribed" definition.
			writeGeneralMeasurementConcordia(
				generator, 
				JSON_KEY_PRESCRIPTION_AMOUNT_PRESCRIBED,
				true);
			
			// Write the "substitution" definition.
			writeCodableValueConcordia(
				generator, 
				JSON_KEY_PRESCRIPTION_SUBSTITUTION,
				true);
			
			// Write the "refills" definition.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_PRESCRIPTION_REFILLS);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Write the "days-supply" definition.
			generator.writeStartObject();
			generator.writeStringField(
				"name",
				JSON_KEY_PRESCRIPTION_DAYS_SUPPLY);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Write the "prescription-expiration" definition.
			writeDateConcordia(
				generator, 
				JSON_KEY_PRESCRIPTION_PRESCRIPTION_EXPIRATION,
				true);
			
			// Write the "instructions" definition.
			writeCodableValueConcordia(
				generator, 
				JSON_KEY_PRESCRIPTION_INSTRUCTIONS,
				true);
			
			// End the root schema.
			generator.writeEndArray();
			
			// End the root field.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}
	}
	
	/**
	 * The HealthVault Thing for Conditions.
	 *
	 * @author John Jenkins
	 */
	public static class ConditionThing extends HealthVaultThing {
		/**
		 * The name of this thing.
		 */
		private static final String NAME = "condition";
		/**
		 * The type ID for this type as defined by HealthVault.
		 */
		private static final String TYPE_ID = 
			"7ea7a1f9-880b-4bd4-b593-f5660f20eda8";
		/**
		 * The XPath to use to get the conditions.
		 */
		private static final String XPATH = "//condition";
		
		// The root fields.
		private static final String JSON_KEY_NAME = "name";
		private static final String JSON_KEY_ONSET_DATE = "onset-date";
		private static final String JSON_KEY_STATUS = "status";
		private static final String JSON_KEY_STOP_DATE = "stop-date";
		private static final String JSON_KEY_STOP_REASON = "stop-reason";
		
		/**
		 * The conditions retrieved from HealthVault. This will always be 
		 * empty until
		 * {@link #service(String, String, DateTime, DateTime, long, long)} has
		 * been called.
		 */
		private final List<Condition> conditions =
			new LinkedList<Condition>();

		/**
		 * Creates a {@link HealthVaultThing} for conditions.
		 */
		public ConditionThing() {
			super(NAME);
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasId()
		 */
		@Override
		public boolean hasId() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasTimestamp()
		 */
		@Override
		public boolean hasTimestamp() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasLocation()
		 */
		@Override
		public boolean hasLocation() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#toConcordia(org.codehaus.jackson.JsonGenerator)
		 */
		@Override
		public JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Begin the definition.
			generator.writeStartObject();
			
			// The type of the data is a JSON object.
			generator.writeStringField("type", "object");
			
			// Define that object.
			generator.writeArrayFieldStart("schema");
			
			// Add the "name" field.
			writeCodableValueConcordia(generator, JSON_KEY_NAME, true);
			
			// Add the "onset-date" field.
			writeApproxDateTimeConcordia(generator, JSON_KEY_ONSET_DATE, true);
			
			// Add the "status" field.
			writeCodableValueConcordia(generator, JSON_KEY_STATUS, true);
			
			// Add the "stop-date" field.
			writeApproxDateTimeConcordia(generator, JSON_KEY_STOP_DATE, true);
			
			// Add the "stop-reason" field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_STOP_REASON);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();

			// Finish defining the overall object.
			generator.writeEndArray();
			
			// Close the definition.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#getTypeId()
		 */
		@Override
		protected String getTypeId() {
			return TYPE_ID;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#getResponseXpath()
		 */
		@Override
		protected String getResponseXpath() {
			return XPATH;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#makeRequest(java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
		 */
		@Override
		protected void makeRequest(
				final String recordId,
				final String personId,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			// Create the unmarshaller, which will take the XML and convert it 
			// into Condition objects.
			Unmarshaller unmarshaller;
			try {
				unmarshaller = 
					JAXBContext
						.newInstance(Condition.class).createUnmarshaller();
			}
			catch(JAXBException e) {
				throw 
					new DomainException(
						"Could not create the unmarshaller for the conditions.",
						e);
			}
			
			// Make the request to HealthVault.
			NodeList conditionNodes =
				makeRequest(
					recordId, 
					personId, 
					startDate, 
					endDate, 
					numToSkip + numToReturn);
			
			// For each of the condition nodes, convert it into a Condition
			// object.
			int numConditions = conditionNodes.getLength();
			try {
				for(int i = 0; i < numConditions; i++) {
					conditions
						.add(
							(Condition) unmarshaller
								.unmarshal(conditionNodes.item(i)));
				}
			}
			catch(JAXBException e) {
				throw
					new DomainException(
						"There was an error unmarshalling a condition.",
						e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
		 */
		@Override
		public long getNumDataPoints() {
			return conditions.size();
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator, org.ohmage.request.observer.StreamReadRequest.ColumnNode)
		 */
		@Override
		public void respond(
				final JsonGenerator generator, 
				final ColumnNode<String> columns)
				throws JsonGenerationException, IOException, DomainException {
			
			// For each object,
			for(Condition condition : conditions) {
				// Start the overall object.
				generator.writeStartObject();
				
				// Write the data.
				generator.writeObjectField("data", condition);
				
				// End the overall object.
				generator.writeEndObject();
			}
		}
	}
	
	/**
	 * The HealthVault Thing for Conditions.
	 *
	 * @author John Jenkins
	 */
	public static class EmergencyOrProviderContactThing
			extends HealthVaultThing {
		
		/**
		 * The name of this thing.
		 */
		private static final String NAME = "emergency_or_provider_contact";
		/**
		 * The type ID for this type as defined by HealthVault.
		 */
		private static final String TYPE_ID = 
			"25c94a9f-9d3d-4576-96dc-6791178a8143";
		/**
		 * The XPath to use to get the conditions.
		 */
		private static final String XPATH = "//person";
		
		/**
		 * The conditions retrieved from HealthVault. This will always be 
		 * empty until
		 * {@link #service(String, String, DateTime, DateTime, long, long)} has
		 * been called.
		 */
		private final List<Person> contacts = new LinkedList<Person>();

		/**
		 * Creates a {@link HealthVaultThing} for emergency or provider
		 * contacts
		 */
		public EmergencyOrProviderContactThing() {
			super(NAME);
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasId()
		 */
		@Override
		public boolean hasId() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasTimestamp()
		 */
		@Override
		public boolean hasTimestamp() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#hasLocation()
		 */
		@Override
		public boolean hasLocation() {
			return false;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#toConcordia(org.codehaus.jackson.JsonGenerator)
		 */
		@Override
		public JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Begin the definition.
			generator.writeStartObject();
			
			// The type of the data is a JSON object.
			generator.writeStringField("type", "object");
			
			// Define that object.
			generator.writeFieldName("schema");
			
			// Add the person object.
			writePersonConcordia(generator, "person", true);
			
			// Close the definition.
			generator.writeEndObject();
			
			// Return the generator to facilitate chaining.
			return generator;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#getTypeId()
		 */
		@Override
		protected String getTypeId() {
			return TYPE_ID;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#getResponseXpath()
		 */
		@Override
		protected String getResponseXpath() {
			return XPATH;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadHealthVaultRequest.HealthVaultThing#makeRequest(java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
		 */
		@Override
		protected void makeRequest(
				final String recordId,
				final String personId,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			// Create the unmarshaller, which will take the XML and convert it
			// into Medication objects.
			Unmarshaller unmarshaller;
			try {
				unmarshaller = 
					JAXBContext
						.newInstance(Person.class).createUnmarshaller();
			}
			catch(JAXBException e) {
				throw 
					new DomainException(
						"Could not create the unmarshaller for the contacts.",
						e);
			}
			
			// Make the request to HealthVault.
			NodeList contactNodes =
				makeRequest(
					recordId, 
					personId, 
					startDate, 
					endDate, 
					numToSkip + numToReturn);
			
			// For each of the medication nodes, convert it into a Medication
			// object.
			int numContacts = contactNodes.getLength();
			try {
				for(int i = 0; i < numContacts; i++) {
					contacts
						.add(
							(Person) unmarshaller
								.unmarshal(contactNodes.item(i)));
				}
			}
			catch(JAXBException e) {
				throw
					new DomainException(
						"There was an error unmarshalling a contact.",
						e);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
		 */
		@Override
		public long getNumDataPoints() {
			return contacts.size();
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator, org.ohmage.request.observer.StreamReadRequest.ColumnNode)
		 */
		@Override
		public void respond(
				final JsonGenerator generator,
				final ColumnNode<String> columns)
				throws JsonGenerationException, IOException, DomainException {
			
			// For each object,
			for(Person contact : contacts) {
				// Start the overall object.
				generator.writeStartObject();
				
				// Write the data.
				generator.writeObjectField("data", contact);
				
				// End the overall object.
				generator.writeEndObject();
			}
		}
	}
	
	/**
	 * A factory class for generated {@link HealthVaultThing} objects.
	 *
	 * @author John Jenkins
	 */
	public static enum HealthVaultThingFactory {
		MEDICATION (MedicationThing.NAME, MedicationThing.class),
		CONDITION (ConditionThing.NAME, ConditionThing.class),
		EMERGENCY_OR_PROVIDER_CONTACT (
			EmergencyOrProviderContactThing.NAME,
			EmergencyOrProviderContactThing.class);
		
		private final String thingName;
		private final Class<? extends HealthVaultThing> thingClass;
		
		/**
		 * The mapping of method strings to their corresponding objects.
		 */
		private static final Map<String, HealthVaultThingFactory> FACTORY = 
			new HashMap<String, HealthVaultThingFactory>();
		static {
			// Populate the 'FACTORY' object.
			for(HealthVaultThingFactory factory : values()) {
				FACTORY.put(factory.thingName, factory);
			}
		}
		
		/**
		 * Default constructor made private to prevent instantiation.
		 * 
		 * @param thingName The thing's name.
		 * 
		 * @param thingClass The class the implements this thing.
		 */
		private HealthVaultThingFactory(
				final String methodString, 
				final Class<? extends HealthVaultThing> methodClass) {
			
			if(methodString == null) {
				throw
					new IllegalArgumentException("The thing name is null.");
			}
			if(methodString.trim().length() == 0) {
				throw new IllegalArgumentException(
					"The thing name is all whitespace.");
			}
			if(methodClass == null) {
				throw new IllegalArgumentException("The thing class is null.");
			}
			
			this.thingName = methodString;
			this.thingClass = methodClass;
		}
		
		/**
		 * Returns the thing's name.
		 * 
		 * @return The thing's name.
		 */
		public final String getThingName() {
			return thingName;
		}
		
		/**
		 * Returns a new instance of the HealthVaultThing object specified by 
		 * the 'name' parameter.
		 * 
		 * @param name The string to use to lookup a HealthVaultThing object.
		 * 
		 * @return The HealthVaultThing object that corresponds to the 'name'
		 * 		   parameter.
		 * 
		 * @throws DomainException The thing's name was unknown or there was an
		 * 						   error generating an instance of it.
		 */
		public static final HealthVaultThing getThing(
				final String name)
				throws DomainException {
			
			if(FACTORY.containsKey(name)) {
				try {
					return FACTORY.get(name).thingClass.newInstance();
				}
				catch(InstantiationException e) {
					throw new DomainException(
						"The Class for the method cannot be instantiated: " +
							name,
						e);
				}
				catch(IllegalAccessException e) {
					throw new DomainException(
						"The Class for the method does not contain a no-argument constructor: " +
							name,
						e);
				}
				catch(SecurityException e) {
					throw new DomainException(
						"The security manager prevented instantiation for the method: " +
							name,
						e);
				}
				catch(ExceptionInInitializerError e) {
					throw new DomainException(
						"The constructor for the method threw an exception: " +
							name,
						e);
				}
			}
			
			throw new DomainException("The thing is unknown: " + name);
		}
	}

	private final String owner;
	private final DateTime startDate;
	private final DateTime endDate;
	private final long numToSkip;
	private final long numToReturn;
	private final HealthVaultThing thing;
	
	/**
	 * Creates a request to read a HealthVault API.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters already decoded from the HTTP request.
	 * 
	 * @param hashPassword Whether or not to hash the user's password on
	 * 					   authentication. If null, username and password are
	 * 					   not allowed for this API.
	 * 
	 * @param tokenLocation Where to search for the user's token. If null, a
	 * 						token is not allowed for this API.
	 * 
	 * @param callClientRequester Refers to the "client" parameter as the
	 * 							  "requester".
	 * 
	 * @param owner The user whose data is being requested. If null, the 
	 * 				requester is requesting data about themselves.
	 * 
	 * @param startDate Limits the results to only those on or after this date.
	 * 
	 * @param endDate Limits the results to only those on or before this date.
	 * 
	 * @param columns Limits the data output based on the given columns.
	 * 
	 * @param numToSkip The number of responses to skip. Responses are in 
	 * 					reverse-chronological order.
	 * 
	 * @param numToReturn The number of responses to return after the required
	 * 					  responses have been skipped.
	 * 
	 * @param thingName The name of the HealthVault Thing to retrieve.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhReadHealthVaultRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final String owner,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn,
			final String thingName)
			throws IOException, InvalidRequestException {
		
		super(
			httpRequest, 
			hashPassword, 
			tokenLocation, 
			parameters, 
			callClientRequester);
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH read request for HealthVault.");
		}
		
		this.owner = owner;
		this.startDate = startDate;
		this.endDate = endDate;
		this.numToSkip = numToSkip;
		this.numToReturn = numToReturn;
		
		HealthVaultThing tThing = null;
		try {
			tThing = OmhValidators.validateHealthVaultThing(thingName);
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		this.thing = tThing;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read request for HealthVault.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// Verify that the user is allowed to query this data through 
			// ohmage.
			if((owner != null) && (! owner.equals(getUser().getUsername()))) {
				try {
					LOGGER.info("Checking if the user is an admin.");
					UserServices.instance().verifyUserIsAdmin(
						getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					LOGGER.info("The user is not an admin.");

					LOGGER.info(
						"Checking if reading data about another user is even allowed.");
					boolean isPlausible;
					try {
						isPlausible = 
							StringUtils.decodeBoolean(
								PreferenceCache.instance().lookup(
									PreferenceCache.KEY_PRIVILEGED_USER_IN_CLASS_CAN_VIEW_MOBILITY_FOR_EVERYONE_IN_CLASS));
					}
					catch(CacheMissException e) {
						throw new ServiceException(e);
					}
					
					if(isPlausible) {
						LOGGER.info(
							"Checking if the requester is allowed to read data about this user.");
						UserClassServices
							.instance()
							.userIsPrivilegedInAnotherUserClass(
								getUser().getUsername(), 
								owner);
					}
					else {
						throw new ServiceException(
							ErrorCode.OMH_INSUFFICIENT_PERMISSIONS,
							"This user is not allowed to query data about the requested user.");
					}
				}
			}
			
			// Get the authentication information from the database.
			LOGGER
				.info(
					"Getting the authentication credentials for HealthVault.");
			Map<String, String> healthVaultCredentials =
				OmhServices.instance().getCredentials("health_vault");
			
			// Switch on either the requester or the given username.
			String requestee = 
				((owner == null) ? getUser().getUsername() : owner);
			
			// Retrieve the user's record ID.
			String recordId = 
				healthVaultCredentials
					.get(requestee + "_record_id");
			if(recordId == null) {
				// If the user is not linked, we treat it as if they have no
				// data.
				LOGGER
					.info(
						"The user's account is not linked, so we are returning no data.");
				return;
			}
			
			// Retrieve the user's person ID.
			String personId = 
				healthVaultCredentials
					.get(requestee + "_person_id");
			if(personId == null) {
				// If the user is not linked, we treat it as if they have no
				// data.
				LOGGER
					.info(
						"The user's account is not linked, so we are returning no data.");
				return;
			}
			
			// Get the data and massage it into a form we like.
			try {
				LOGGER.info("Calling the HealthVault API.");
				thing
					.service(
						recordId, 
						personId, 
						startDate, 
						endDate, 
						numToSkip, 
						numToReturn);
			}
			catch(DomainException e) {
				throw new ServiceException("Could not retrieve the data.", e);
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
	 */
	@Override
	public long getNumDataPoints() {
		return thing.getNumDataPoints();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator, org.ohmage.request.observer.StreamReadRequest.ColumnNode)
	 */
	@Override
	public void respond(
			final JsonGenerator generator,
			final ColumnNode<String> columns)
			throws JsonGenerationException, IOException, DomainException {
		
		LOGGER.info("Responding to an OMH read request for HealthVault data.");
		
		// We call through to the API to respond.
		thing.respond(generator, columns);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {

		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
		else {
			throw new UnsupportedOperationException(
				"HTTP requests are invalid for this request.");
		}
	}
}