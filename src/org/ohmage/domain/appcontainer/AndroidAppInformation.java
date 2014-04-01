package org.ohmage.domain.appcontainer;

import java.net.URI;

import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * Application information for an Android application.
 * </p>
 *
 * @author John Jenkins
 */
public class AndroidAppInformation extends AppInformation {
    /**
     * The JSON key for the application's package.
     */
    public static final String JSON_KEY_PACKAGE = "package";

    /**
     * The JSON key for the application's version.
     */
    public static final String JSON_KEY_VERSION = "version";

    /**
     * The package name in the application's manifest.
     */
    @JsonProperty(JSON_KEY_PACKAGE)
    private final String packageName;

    /**
     * The application's version in its manifest.
     */
    @JsonProperty(JSON_KEY_VERSION)
    private final long version;

    /**
     * Creates a new Android app information object.
     *
     * @param appUri
     *        A URI to direct the user to install the application, e.g. the
     *        Android Market.
     *
     * @param authorizationUri
     *        A URI to direct the user to grant ohmage permission to read the
     *        user's data.
     *
     * @param packageName
     *        The application's name from its manifest.
     *
     * @param version
     *        The application's version from its manifest.
     *
     * @throws InvalidArgumentException
     *         The package name and/or version are null.
     */
    @JsonCreator
    public AndroidAppInformation(
        @JsonProperty(JSON_KEY_APP_URI) final URI appUri,
        @JsonProperty(JSON_KEY_AUTHORIZATION_URI) final URI authorizationUri,
        @JsonProperty(JSON_KEY_PACKAGE) final String packageName,
        @JsonProperty(JSON_KEY_VERSION) final Long version)
        throws InvalidArgumentException {

        super(appUri, authorizationUri);

        if(packageName == null) {
            throw new InvalidArgumentException("The package name is missing.");
        }
        if(version == null) {
            throw new InvalidArgumentException("The version is missing.");
        }

        this.packageName = packageName;
        this.version = version;
    }
}