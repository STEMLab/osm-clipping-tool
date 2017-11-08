package io.github.stemlab.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.entity.Feature;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Azamat on 7/13/2017.
 */
public class FeatureDeserializer extends StdDeserializer<Feature> {

    public FeatureDeserializer() {
        this(null);
    }

    public FeatureDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Feature deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        Long id = Long.parseLong(node.get("id").asText());
        String type = node.get("type").asText();
        JsonNode properties = node.get("properties");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> p = mapper.convertValue(properties, HashMap.class);

        GeoJSONReader reader = new GeoJSONReader();
        Geometry geometry = reader.read(node.get("geometry").toString());
        Feature feature = new Feature();
        feature.setId(id);
        feature.setGeometry(geometry);
        feature.setProperties(p);
        feature.setType(type);
        return feature;
    }
}
