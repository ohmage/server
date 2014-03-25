package org.ohmage.domain.stream;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;

import org.ohmage.domain.AppInformation;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>
 * A stream of passively collected data.
 * </p>
 *
 * @author John Jenkins
 */
public class Stream extends Schema {
	/**
	 * <p>
	 * A builder for a {@link Stream}.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class Builder extends Schema.Builder {
        /**
         * The definition of this schema.
         */
        protected Concordia definition;

        /**
         * The list of information about the apps that correspond to this
         * stream.
         */
        protected List<AppInformationWithAuthorization> apps;

        /**
         * Creates a new Stream builder object.
         *
         * @param version
         *        The version of this schema.
         *
         * @param name
         *        The name of this schema.
         *
         * @param description
         *        The description of this schema.
         *
         * @param iconId
         *        The media ID for the icon image.
         *
         * @param omhVisible
         *        Whether or not this schema is visible to the Open mHealth
         *        APIs.
         *
         * @param definition
         *        The definition of this schema.
         *
         * @param apps
         *        The list of information about applications that correspond to
         *        this data stream.
         */
		@JsonCreator
		public Builder(
			@JsonProperty(JSON_KEY_VERSION) final long version,
			@JsonProperty(JSON_KEY_NAME) final String name,
			@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
            @JsonProperty(JSON_KEY_ICON_ID) final String iconId,
            @JsonProperty(JSON_KEY_OMH_VISIBLE) final Boolean omhVisible,
			@JsonProperty(JSON_KEY_DEFINITION) final Concordia definition,
	        @JsonProperty(JSON_KEY_APPS) final List<AppInformationWithAuthorization> apps) {

			super(version, name, description, iconId, omhVisible);

			this.definition = definition;
			this.apps = apps;
		}

		/**
		 * Creates a new builder based on an existing Stream object.
		 *
		 * @param stream
		 *        The existing Stream object on which this Builder should be
		 *        based.
		 */
		public Builder(final Stream stream) {
			super(stream);

			definition = stream.definition;
			apps = stream.apps;
		}

		/**
		 * Creates a new Stream object from the state of this builder.
		 *
		 * @throws InvalidArgumentException
		 *         The state of this builder is insufficient to build a new
		 *         {@link Stream} object.
		 */
		@Override
		public Stream build() throws InvalidArgumentException {
			return
				new Stream(
				    (schemaId == null) ? getRandomId() : schemaId,
					version,
					name,
					description,
					owner,
					iconId,
					omhVisible,
					definition,
					apps,
					internalReadVersion,
					internalWriteVersion);
		}
	}

    /**
     * <p>
     * Information about an application that corresponds to a particular
     * stream. This should be used to indicate to users of a particular
     * platform, e.g. Android, iOS, Windows, etc., how to install an
     * application and how to begin the flow of authorizing ohmage to install
     * the data.
     * </p>
     *
     * @author John Jenkins
     */
	public static class AppInformationWithAuthorization
	    extends AppInformation {

	    /**
	     * The JSON key for the authorization URI.
	     */
	    public static final String JSON_KEY_AUTHORIZATION_URI =
	        "authorization_uri";

	    /**
	     * The URI to use to direct the user to authorize the application.
	     */
	    @JsonProperty(JSON_KEY_AUTHORIZATION_URI)
	    private final URI authorizationUri;

        /**
         * Creates a new or reconstructs an existing
         * AppInformationWithAuthorization object.
         *
         * @param platform
         *        The application platform, e.g. Android, iOS, Windows, etc..
         *
         * @param appUri
         *        A URI to direct the user to install the application, e.g. the
         *        platform's app store.
         *
         * @param authorizationUri
         *        A URI to direct the user to grant ohmage permission to read
         *        the user's data.
         *
         * @throws InvalidArgumentException
         *         The platform or application URI are null.
         */
	    @JsonCreator
	    public AppInformationWithAuthorization(
	        @JsonProperty(JSON_KEY_PLATFORM) final String platform,
	        @JsonProperty(JSON_KEY_APP_URI) final URI appUri,
	        @JsonProperty(JSON_KEY_AUTHORIZATION_URI)
	            final URI authorizationUri)
	        throws InvalidArgumentException {

	        super(platform, appUri);

	        this.authorizationUri = authorizationUri;
	    }
	}

	public static final String JSON_KEY_APPS = "apps";

    /**
     * The definition of this schema.
     */
    @JsonProperty(JSON_KEY_DEFINITION)
    private final Concordia definition;

    /**
     * The list of applications that supply data for this stream.
     */
    @JsonProperty(JSON_KEY_APPS)
    private final List<AppInformationWithAuthorization> apps;

    /**
     * Creates a new Stream object.
     *
     * @param version
     *        The version of this stream.
     *
     * @param name
     *        The name of this stream.
     *
     * @param description
     *        The description of this stream.
     *
     * @param owner
     *        The owner of this stream.
     *
     * @param iconId
     *        The media ID for the icon image.
     *
     * @param omhVisible
     *        Whether or not this schema is visible to the Open mHealth APIs.
     *
     * @param definition
     *        The definition of this stream.
     *
     * @param apps
     *        The list of information about applications that correspond to
     *        this data stream.
     *
     * @throws InvalidArgumentException
     *         A parameter is invalid.
     */
	public Stream(
		final long version,
		final String name,
		final String description,
		final String owner,
		final String iconId,
        final boolean omhVisible,
		final Concordia definition,
		final List<AppInformationWithAuthorization> apps)
		throws InvalidArgumentException {

		this(
		    null,
		    version,
		    name,
		    description,
		    owner,
		    iconId,
		    omhVisible,
		    definition,
		    apps,
		    null);
	}

	/**
	 * Rebuild an existing Schema object.
	 *
	 * @param id
	 *        The unique identifier for this object. If null, a default value
	 *        is given.
	 *
	 * @param version
	 *        The version of this schema.
	 *
	 * @param name
	 *        The name of this schema.
	 *
	 * @param description
	 *        The description of this schema.
	 *
	 * @param owner
	 *        The owner of this schema.
     *
     * @param iconId
     *        The media ID for the icon image.
     *
     * @param omhVisible
     *        Whether or not this schema is visible to the Open mHealth APIs.
	 *
	 * @param definition
	 *        The definition of this schema.
     *
     * @param apps
     *        The list of information about applications that correspond to
     *        this data stream.
	 *
	 * @param internalVersion
	 *        The internal version of this schema.
	 *
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 *
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	@JsonCreator
	protected Stream(
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_VERSION) final long version,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
        @JsonProperty(JSON_KEY_OWNER) final String owner,
        @JsonProperty(JSON_KEY_ICON_ID) final String iconId,
        @JsonProperty(JSON_KEY_OMH_VISIBLE) final Boolean omhVisible,
		@JsonProperty(JSON_KEY_DEFINITION) final Concordia definition,
		@JsonProperty(JSON_KEY_APPS) final List<AppInformationWithAuthorization> apps,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException, InvalidArgumentException {

		this(
			id,
			version,
			name,
			description,
			owner,
			iconId,
			omhVisible,
			definition,
			apps,
			internalVersion,
			internalVersion);
	}

	/**
	 * Builds the Schema object.
	 *
	 * @param id
	 *        The unique identifier for this object.
	 *
	 * @param version
	 *        The version of this schema.
	 *
	 * @param name
	 *        The name of this schema.
	 *
	 * @param description
	 *        The description of this schema.
	 *
	 * @param owner
	 *        The owner of this schema.
     *
     * @param iconId
     *        The media ID for the icon image.
     *
     * @param omhVisible
     *        Whether or not this schema is visible to the Open mHealth APIs.
	 *
	 * @param definition
	 *        The definition of this schema.
     *
     * @param apps
     *        The list of information about applications that correspond to
     *        this data stream.
	 *
	 * @param internalReadVersion
	 *        The internal version of this schema when it was read from the
	 *        database.
	 *
	 * @param internalWriteVersion
	 *        The new internal version of this schema when it will be written
	 *        back to the database.
	 *
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 *
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	private Stream(
		final String id,
		final long version,
		final String name,
		final String description,
		final String owner,
		final String iconId,
        final Boolean omhVisible,
		final Concordia definition,
		final List<AppInformationWithAuthorization> apps,
		final Long internalReadVersion,
		final Long internalWriteVersion)
		throws IllegalArgumentException, InvalidArgumentException {

		super(
			id,
			version,
			name,
			description,
			owner,
			iconId,
			omhVisible,
			internalReadVersion,
			internalWriteVersion);

        if(definition == null) {
            throw new InvalidArgumentException("The definition is null.");
        }

        this.definition = definition;
        this.apps =
            (apps == null) ?
                Collections.<AppInformationWithAuthorization>emptyList() :
                new ArrayList<AppInformationWithAuthorization>(apps);
	}

	/**
	 * Validates that some meta-data and data conform to this stream's
	 * definition.
	 *
	 * @param metaData
	 *        The {@link MetaData} to validate.
	 *
	 * @param data
	 *        The data to validate.
	 *
	 * @throws InvalidArgumentException
	 *         The meta-data or data were invalid.
	 */
	public void validate(
		final MetaData metaData,
		final JsonNode data)
		throws InvalidArgumentException {

	    if(data == null) {
	        throw new InvalidArgumentException("The stream data is null.");
	    }

		try {
			getDefinition().validateData(data);
		}
		catch(ConcordiaException e) {
			throw
				new InvalidArgumentException(
					"The data does not conform to the schema.",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.Schema#getDefinition()
	 */
	@Override
    public Concordia getDefinition() {
	    return definition;
	}
}