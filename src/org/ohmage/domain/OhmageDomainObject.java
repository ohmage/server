package org.ohmage.domain;

import java.util.UUID;

import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The base class for all ohmage domain objects.
 * </p>
 *
 * @author John Jenkins
 */
@JsonAutoDetect(
	fieldVisibility = Visibility.DEFAULT,
	getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE,
	setterVisibility = Visibility.NONE,
	creatorVisibility = Visibility.DEFAULT)
@JsonFilter(OhmageDomainObject.JSON_FILTER_VALUE)
public abstract class OhmageDomainObject {
	/**
	 * <p>
	 * The root builder class for all ohmage domain objects.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class Builder<T extends OhmageDomainObject> {
		/**
		 * The internal read version of this object.
		 */
		protected long internalReadVersion =
			OhmageDomainObject.DEFAULT_INTERNAL_VERSION;
		/**
		 * The internal write version of this object.
		 */
		protected long internalWriteVersion =
			OhmageDomainObject.DEFAULT_INTERNAL_VERSION;

		/**
		 * Creates a new builder based on an existing object and increments its
		 * internal write version.
		 * 
		 * @param user
		 *        The existing object on which this Builder should be based.
		 */
		public Builder(final T object) {
			if(object == null) {
				return;
			}
			
			this.internalReadVersion = object.getInternalReadVersion();
			this.internalWriteVersion = object.getInternalWriteVersion() + 1;
		}
	}
	
	/**
	 * The JSON key for the internal version of the entity in the data-store.
	 */
	public static final String JSON_KEY_INTERNAL_VERSION = "version";
	
	/**
	 * The default internal version.
	 */
	private static final long DEFAULT_INTERNAL_VERSION = 0;
	
	/**
	 * The JSON filter value.
	 */
	protected static final String JSON_FILTER_VALUE =
		"org.ohmage.domain.OhmageDomainObject";
	// Register this class.
	static {
		OhmageObjectMapper.register(OhmageDomainObject.class);
	}
	
	/**
	 * The internal version of the user when it was read from the data-store.
	 */
	@JsonIgnore
	private final long internalReadVersion;
	/**
	 * The new version to use if the user were written back to the data-store.
	 */
	@JsonProperty(JSON_KEY_INTERNAL_VERSION)
	@JsonFilterField
	private final long internalWriteVersion;
	
	/**
	 * Creates a new domain object.
	 * 
	 * @param internalReadVersion
	 *        The current internal read version of this object.
	 * 
	 * @param internalWriteVersion
	 *        The current internal write version of this object.
	 */
	public OhmageDomainObject(
		final Long internalReadVersion,
		final Long internalWriteVersion) {
		
		this.internalReadVersion =
			(internalReadVersion == null) ?
				DEFAULT_INTERNAL_VERSION :
				internalReadVersion;
		
		this.internalWriteVersion =
			(internalWriteVersion == null) ?
				this.internalReadVersion :
				internalReadVersion;
	}
	
	/**
	 * Returns the internal read version of this object.
	 * 
	 * @return The internal read version of this object.
	 */
	public long getInternalReadVersion() {
		return internalReadVersion;
	}
	
	/**
	 * Returns the internal read version of this object.
	 * 
	 * @return The internal read version of this object.
	 */
	public long getInternalWriteVersion() {
		return internalWriteVersion;
	}
	
	/**
	 * Returns a randomly generated ID.
	 * 
	 * @return A randomly generated ID.
	 */
	protected static String getRandomId() {
		return UUID.randomUUID().toString();
	}
}