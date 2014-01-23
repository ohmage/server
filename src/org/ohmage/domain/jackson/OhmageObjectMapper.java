package org.ohmage.domain.jackson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.ohmage.domain.ISOW3CDateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.SerializeExceptFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * <p>
 * A custom ObjectMapper for ohmage that adds functionality like filtering
 * fields on HTTP-level serialization.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmageObjectMapper extends ObjectMapper {
	/**
	 * <p>
	 * This interface signifies that its corresponding field should not be
	 * serialized to the end user.
	 * <p>
	 *
	 * @author John Jenkins
	 */
	@Documented
	@Target({ ElementType.FIELD, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface JsonFilterField {};

	/**
	 * <p>
	 * The custom filter that allows for the addition of new field names.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	private static class ExtendableSerializeExceptFilter
		extends SerializeExceptFilter {

		/**
		 * Creates a new filter with the field name.
		 *
		 * @param fieldName
		 *        The first field for this filter.
		 */
		public ExtendableSerializeExceptFilter(final String fieldName) {
			// Initialize this with the universal fields.
			super(new HashSet<String>(Arrays.asList(fieldName)));

		}

		/**
		 * Adds another field to this filter.
		 *
		 * @param fieldName
		 *        The new field.
		 */
		public void addField(final String fieldName) {
			_propertiesToExclude.add(fieldName);
		}
	}

	/**
     * A default version UID to use when serializing an instance of this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The set of private fields that should never be serialized.
     */
    private static final SimpleFilterProvider FILTER_PROVIDER =
    	new SimpleFilterProvider();

    /**
     * Creates the object mapper and initializes the filters.
     */
    public OhmageObjectMapper() {
        // Register our DateTime (de)serializer.
        SimpleModule dateTimeModule =
            new SimpleModule(
                "W3C ISO-8601 DateTime (de)serialization module",
                new Version(1, 0, 0, null, null, null));
        dateTimeModule.addSerializer(DateTime.class, new ToStringSerializer());
        dateTimeModule
            .addDeserializer(
                DateTime.class,
                new ISOW3CDateTimeFormat.Deserializer());
        registerModule(dateTimeModule);

        // Register our BigDecimal serializer.
        SimpleModule bigDecimalSerializer =
            new SimpleModule(
                "BigDecimal serialization module",
                new Version(1, 0, 0, null, null, null));
        bigDecimalSerializer
            .addSerializer(BigDecimal.class, new BigDecimalSerializer());
        registerModule(bigDecimalSerializer);

    	enable(
    	    // The clients may not send numbers for enums. They must use the
    	    // lower-case string value and if the value is unknown it will be
    	    // parsed as null.
    	    DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS,
    		DeserializationFeature.READ_ENUMS_USING_TO_STRING,
    		DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);//,
    		// Use BigInteger and BigDecimal for integer and decimal values.
//    		DeserializationFeature.USE_BIG_INTEGER_FOR_INTS,
//    		DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    	// Write the enums using their lower-case representation.
    	enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

    	// Ensure that unknown fields are ignored.
    	FILTER_PROVIDER.setFailOnUnknownId(false);
        setFilters(FILTER_PROVIDER);
    }

    /**
	 * <p>
	 * Adds all fields marked with the {@link JsonFilterField} annotation to
	 * this class' filter. This includes all fields at and above this class in
	 * its class hierarchy.
	 * </p>
	 *
	 * <p>
	 * The class must be annotated with a {@link JsonFilter} annotation, whose
	 * value must be unique to this class. It is generally recommended to use
	 * the class' package and name.
	 * </p>
	 *
	 * @param filterClass
	 *        The class to register, which must have a {@link JsonFilter}
	 *        annotation.
	 */
    public static synchronized void register(final Class<?> filterClass) {
    	// Sanitize the input.
    	if(filterClass == null) {
    		throw new IllegalArgumentException("The filter class is null.");
    	}

    	// Retrieve the JsonFilter and get its value. This will be used to
    	// construct the filter for this class.
    	JsonFilter rootFilter =
    		filterClass.getAnnotation(JsonFilter.class);
    	if(rootFilter == null) {
    		throw
    			new IllegalArgumentException(
    				"The registering class must have a JsonFilter " +
    					"annotation: " +
    					filterClass.getName());
    	}
    	String filterGroup = rootFilter.value();

    	// Create a handle to the current class in the hierarchy that we are
    	// analyzing.
    	Class<?> currClass = filterClass;

    	// Cycle through the class hierarchy.
    	do {
	    	// Cycle through each of the current class' fields.
	    	for(Field field : currClass.getDeclaredFields()) {
	    		// Determine if that field has a JsonFilterField annotation.
	    		JsonFilterField filter =
	    			field.getAnnotation(JsonFilterField.class);

	    		// If it does have the annotation.
	    		if(filter != null) {
	    			// Get the serialized field name.
	    			String fieldName;
	    			JsonProperty jsonProperty =
	    				field.getAnnotation(JsonProperty.class);
	    			if(jsonProperty == null) {
	    				fieldName = field.getName();
	    			}
	    			else {
	    				fieldName = jsonProperty.value();
	    			}

	    			// Get the existing filter, if one exists.
	    			BeanPropertyFilter propertyFilter;
	    			try {
	    				propertyFilter =
	    				    FILTER_PROVIDER.findFilter(filterGroup);
	    			}
	    			catch(IllegalArgumentException e) {
	    			    propertyFilter = null;
	    			}

					// If no such filter exists, update the filter provider
					// with a new one.
	    			if(propertyFilter == null) {
	    				FILTER_PROVIDER
	    					.addFilter(
	    						filterGroup,
	    						new ExtendableSerializeExceptFilter(
	    							fieldName));
	    			}
	    			// If a filter does exist, just add this field to it.
	    			else {
						// Safely cast the filter as we are the only
						// maintainers of it, and this is the only type of
	    				// filter we use.
	    				ExtendableSerializeExceptFilter extendableFilter =
	    					(ExtendableSerializeExceptFilter) propertyFilter;

	    				// Add the field.
	    				extendableFilter.addField(fieldName);
	    			}
	    		}
	    	}

	    	// Jump to the parent and continue.
	    	currClass = currClass.getSuperclass();
	    // Continue cycling until we have left the hierarchy.
    	} while(currClass != null);
    }
}