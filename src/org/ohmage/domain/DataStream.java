package org.ohmage.domain;

import java.io.IOException;

import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DecoderFactory;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.exception.DomainException;

public class DataStream {
	private static final DecoderFactory DECODER_FACTORY = new DecoderFactory();
	
	/**
	 * This class represents the meta-data for a data stream. All fields are 
	 * optional.
	 *
	 * @author John Jenkins
	 */
	public static class MetaData {
		private final DateTime timestamp;
		private final Location location;
		
		/**
		 * Creates a new MetaData object.
		 * 
		 * @param timestamp The time stamp for this meta-data.
		 * 
		 * @param location The location for this meta-data.
		 */
		public MetaData(
				final DateTime timestamp, 
				final Location location) {
			
			this.timestamp = timestamp;
			this.location = location;
		}

		/**
		 * Returns timestamp.
		 *
		 * @return The timestamp.
		 */
		public DateTime getTimestamp() {
			return timestamp;
		}

		/**
		 * Returns location.
		 *
		 * @return The location.
		 */
		public Location getLocation() {
			return location;
		}
	}
	private final MetaData metaData;
	
	private final Stream stream;
	
	private final GenericContainer container;

	public DataStream(
			final Stream stream,
			final MetaData metaData,
			final byte[] data) 
			throws DomainException {
		
		if(stream == null) {
			throw new DomainException("The stream is null.");
		}
		else if(data == null) {
			throw new DomainException("The data is null.");
		}
		else if(data.length == 0) {
			throw new DomainException("The data is empty.");
		}
		
		// Save the reference to the stream.
		this.stream = stream;
		
		// Save the meta-data.
		this.metaData = metaData;
		
		// Decode the data from the stream.
		GenericDatumReader<GenericContainer> genericReader =
			new GenericDatumReader<GenericContainer>(stream.getSchema());
		try {
			container = 
				genericReader.read(
					null, 
					DECODER_FACTORY.binaryDecoder(data, null));
		}
		catch(IOException e) {
			throw new DomainException(e);
		}
	}
	
	public DataStream(
			final Stream stream,
			final MetaData metaData,
			final JSONObject data) 
			throws DomainException {
		
		if(stream == null) {
			throw new DomainException("The stream is null.");
		}
		else if(data == null) {
			throw new DomainException("The data stream is null.");
		}
		
		// Save the reference to the stream.
		this.stream = stream;
		
		// Save the meta-data.
		this.metaData = metaData;
		
		// Decode the data from the stream.
		GenericDatumReader<GenericContainer> genericReader =
			new GenericDatumReader<GenericContainer>(stream.getSchema());
		try {
			container = 
				genericReader.read(
					null, 
					DECODER_FACTORY.jsonDecoder(
						stream.getSchema(), 
						data.toString()));
		}
		catch(IOException e) {
			throw new DomainException(e);
		}
	}
}