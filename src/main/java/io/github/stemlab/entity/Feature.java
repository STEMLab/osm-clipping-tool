package io.github.stemlab.entity;

import java.util.Map;

/**
 * Created by Azamat on 6/2/2017.
 */
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

    public String getType() {
        return type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
