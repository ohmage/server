package org.ohmage.domain.jackson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.SerializeExceptFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

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
	@Target({ ElementType.FIELD })
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
    	// Ensure that unknown fields are ignored.
    	FILTER_PROVIDER.setFailOnUnknownId(false);
    	
    	// Save the FilterProvider in this ObjectMapper.
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
	    			BeanPropertyFilter propertyFilter =
	    				FILTER_PROVIDER.findFilter(filterGroup);
	    			
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