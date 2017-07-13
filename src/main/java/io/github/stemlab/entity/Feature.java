package io.github.stemlab.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.serializer.FeatureDeserializer;
import io.github.stemlab.serializer.FeatureSerializer;

import java.util.Map;

/**
 * Created by Azamat on 6/2/2017.
 */
@JsonSerialize(using = FeatureSerializer.class)
@JsonDeserialize(using = FeatureDeserializer.class)
public class Feature {

    private static String type = "Feature";
    public Map<String, String> properties;
    private Long id;
    private Geometry geometry;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getType() {
        return type;
    }

    public static void setType(String type) {
        Feature.type = type;
    }

    public com.vividsolutions.jts.geom.Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(com.vividsolutions.jts.geom.Geometry geometry) {
        this.geometry = geometry;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
