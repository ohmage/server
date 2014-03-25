package org.ohmage.domain;

import java.net.URI;

import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class AppInformation {
    /**
     * The JSON key for the platform.
     */
    public static final String JSON_KEY_PLATFORM = "platform";

    /**
     * The JSON key for the application's URI.
     */
    public static final String JSON_KEY_APP_URI = "app_uri";

    /**
     * The platform to which this application applies, e.g. Android, iOS,
     * Windows, etc..
     */
    @JsonProperty(JSON_KEY_PLATFORM)
    private final String platform;

    /**
     * The URI to use to direct the user to installing the application.
     */
    @JsonProperty(JSON_KEY_APP_URI)
    private final URI appUri;

    /**
     * Creates a new or reconstructs an existing AppInformation object.
     *
     * @param platform
     *        The application platform, e.g. Android, iOS, Windows, etc..
     *
     * @param appUri
     *        A URI to direct the user to install the application, e.g. the
     *        platform's app store.
     *
     * @throws InvalidArgumentException
     *         The platform or application URI are null.
     */
    @JsonCreator
    public AppInformation(
        @JsonProperty(JSON_KEY_PLATFORM) final String platform,
        @JsonProperty(JSON_KEY_APP_URI) final URI appUri)
        throws InvalidArgumentException {

        if(platform == null) {
            throw new InvalidArgumentException("The platform is missing.");
        }
        if(appUri == null) {
            throw
                new InvalidArgumentException(
                    "The application URI is missing.");
        }

        this.platform = platform;
        this.appUri = appUri;
    }
}