package org.ohmage.servlet.spring;

import java.util.Properties;

import org.ohmage.servlet.OhmletServlet;
import org.ohmage.servlet.SchemaServlet;
import org.ohmage.servlet.StreamServlet;
import org.ohmage.servlet.SurveyServlet;
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

    /**
     * Creates and initializes the cache-control interceptor for web requests.
     */
    public CacheControlWebContentInterceptor() {
        setCacheSeconds(0);
        setAlwaysUseFullPath(true);

        Properties cacheMappings = new Properties();

        // Stream caching.
        cacheMappings
            .put("/" + StreamServlet.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + StreamServlet.ROOT_MAPPING + "/*",
                VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + StreamServlet.ROOT_MAPPING + "/*/*",
                NON_VOLATILE_CACHE_DURATION);

        // Survey caching.
        cacheMappings
            .put("/" + SurveyServlet.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + SurveyServlet.ROOT_MAPPING + "/*",
                VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + SurveyServlet.ROOT_MAPPING + "/*/*",
                NON_VOLATILE_CACHE_DURATION);

        // Schema caching.
        cacheMappings
            .put("/" + SchemaServlet.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + SchemaServlet.ROOT_MAPPING + "/*",
                VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + SchemaServlet.ROOT_MAPPING + "/*/*",
                NON_VOLATILE_CACHE_DURATION);

        // Ohmlet caching.
        cacheMappings
            .put("/" + OhmletServlet.ROOT_MAPPING, VOLATILE_CACHE_DURATION);
        cacheMappings
            .put(
                "/" + OhmletServlet.ROOT_MAPPING + "/*",
                VOLATILE_CACHE_DURATION);

        setCacheMappings(cacheMappings);
    }
}