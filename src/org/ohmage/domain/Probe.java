package org.ohmage.domain;

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.ohmage.exception.DomainException;

public class Probe {
	private static final Pattern PATTERN_ID_VALIDATOR = 
		Pattern.compile("[a-zA-Z0-9\\.]");
	
	private final String id;
	private final long version;
	
	private final String name;
	private final String description;
	private final String versionString;
	
	public static class Observer{
		private final String id;
		private final long version;
		
		private final String name;
		private final String description;
		
		// This may not be a string if Avro has it's own representation.
		private final String definition; 
		
		public Observer(
				final String id,
				final long version,
				final String name,
				final String description,
				final String definition)
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
			if(definition == null) {
				throw new DomainException("The definition of this observer.");
			}
			
			this.id = id;
			this.version = version;
			
			this.name = name;
			this.description = description;
			
			// There will probably need to be an Avro component here that 
			// validates this string.
			Parser parser = new Parser();
			Schema schema = parser.parse(definition);
			this.definition = definition;
		}
	}
	private final Collection<Observer> observers;
	
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
		
		this.observers = observers;
	}
	
	public void validateValue(final String value) {
		DeocderFactory.get();
	}
	
	private static final String sanitizeId(
			final String id) 
			throws DomainException {
		
		if(id == null) {
			throw new DomainException("The ID is null,");
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
}
