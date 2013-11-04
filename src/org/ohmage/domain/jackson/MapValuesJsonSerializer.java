package org.ohmage.domain.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * <p>
 * Serializes the values of some map into a list.
 * <p>
 *
 * @author John Jenkins
 */
public class MapValuesJsonSerializer extends JsonSerializer<Map<?, ?>> {
	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(
		final Map<?, ?> value,
		final JsonGenerator generator,
		final SerializerProvider provider)
		throws IOException, JsonProcessingException {
		
		provider.defaultSerializeValue(value.values(), generator);
	}
}