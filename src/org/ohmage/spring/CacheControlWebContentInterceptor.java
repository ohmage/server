package org.ohmage.spring;

import java.util.Properties;

import org.ohmage.controller.OhmletController;
import org.ohmage.controller.SchemaController;
import org.ohmage.controller.StreamController;
import org.ohmage.controller.SurveyController;
import org.ohmage.javax.servlet.listener.ConfigurationFileImport;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

/**
 * <p>
 * An intercepter designed to set caching headers for web requests.
 * </p>
 *
 * @author John Jenkins
 */
public class CacheControlWebContentInterceptor extends WebContentInterceptor {

    /**
     * The default duration to cache non-static, semi-volatile content in
     * seconds. This is set to 5 minutes.
     */
    public static final String VOLATILE_CACHE_DURATION =
        Integer.toString(60 * 5);
    /**
     * The default duration to cache non-static, non-volatile content in
     * seconds. This is set to one day.
     */
    public static final String NON_VOLATILE_CACHE_DURATION =
        Integer.toString(60 * 60 * 24);

    public static final String DISABLE_CACHING_KEY = "ohmage.disable_caching";

    /**
     * Configuration option that disables caching for development purposes.
     */
    private static final boolean DISABLE_CACHING =
        Boolean.parseBoolean(ConfigurationFileImport
            .getCustomProperties()
            .getProperty(DISABLE_CACHING_KEY));

    /**
     * Creates and initializes the cache-control interceptor for web requests.
     */
    public CacheControlWebContentInterceptor() {
        if (!DISABLE_CACHING) {
            setCacheSeconds(0);
            setAlwaysUseFullPath(true);

            Properties cacheMappings = new Properties();

            // Stream caching.
            cacheMappings
                .put("/" + StreamController.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + StreamController.ROOT_MAPPING + "/*",
                    VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + StreamController.ROOT_MAPPING + "/*/*",
                    NON_VOLATILE_CACHE_DURATION);

            // Survey caching.
            cacheMappings
                .put("/" + SurveyController.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + SurveyController.ROOT_MAPPING + "/*",
                    VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + SurveyController.ROOT_MAPPING + "/*/*",
                    NON_VOLATILE_CACHE_DURATION);

            // Schema caching.
            cacheMappings
                .put("/" + SchemaController.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + SchemaController.ROOT_MAPPING + "/*",
                    VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + SchemaController.ROOT_MAPPING + "/*/*",
                    NON_VOLATILE_CACHE_DURATION);

            // Ohmlet caching.
            cacheMappings
                .put("/" + OhmletController.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
            cacheMappings
                .put(
                    "/" + OhmletController.ROOT_MAPPING + "/*",
                    VOLATILE_CACHE_DURATION);

            setCacheMappings(cacheMappings);
        }
    }
}
