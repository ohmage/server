package org.ohmage.domain.spring;

import java.util.Properties;

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
        cacheMappings.put("/streams", VOLATILE_CACHE_DURATION);
        cacheMappings.put("/streams/*", VOLATILE_CACHE_DURATION);
        cacheMappings.put("/streams/*/*", NON_VOLATILE_CACHE_DURATION);
        cacheMappings.put("/surveys", VOLATILE_CACHE_DURATION);
        cacheMappings.put("/surveys/*", VOLATILE_CACHE_DURATION);
        cacheMappings.put("/surveys/*/*", NON_VOLATILE_CACHE_DURATION);
        cacheMappings.put("/schemas", VOLATILE_CACHE_DURATION);
        cacheMappings.put("/schemas/*", VOLATILE_CACHE_DURATION);
        cacheMappings.put("/schemas/*/*", NON_VOLATILE_CACHE_DURATION);
        setCacheMappings(cacheMappings);
    }
}