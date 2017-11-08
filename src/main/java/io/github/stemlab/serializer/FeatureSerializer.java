package io.github.stemlab.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.stemlab.entity.Feature;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;

/**
 * Created by Azamat on 7/12/2017.
 */
public class FeatureSerializer extends StdSerializer<Feature> {

    public FeatureSerializer() {
        this(null);
    }

    public FeatureSerializer(Class<Feature> t) {
        super(t);
    }

    @Override
    public void serialize(Feature feature, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", feature.getId());
        jsonGenerator.writeObjectField("properties", feature.getProperties());
        jsonGenerator.writeStringField("type", feature.getType());

        GeoJSONWriter writer = new GeoJSONWriter();
        if(feature.getGeometry()!=null){
            GeoJSON json = writer.write(feature.getGeometry());
            jsonGenerator.writeObjectField("geometry", json);
        }
        jsonGenerator.writeEndObject();
    }
}
