package org.ohmage.domain.jackson;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * <p>
 * Serializes the map of keys to lists into individual objects into just a list
 * of those objects.
 * </p>
 *
 * @author John Jenkins
 */
public class MapCollectionJsonSerializer
    extends JsonSerializer<Map<?, Collection<Object>>> {

    /*
     * (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(
        final Map<?, Collection<Object>> value,
        final JsonGenerator generator,
        final SerializerProvider provider)
        throws IOException, JsonProcessingException {

        Collection<Object> aggregateCollection = new LinkedList<Object>();
        for(Collection<Object> currValue : value.values()) {
            aggregateCollection.addAll(currValue);
        }

        provider.defaultSerializeValue(aggregateCollection, generator);
    }
}