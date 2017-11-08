package io.github.stemlab.entity;

import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public class FeatureCollection {

    private static String type = "FeatureCollection";

    private List<Feature> features;

    public FeatureCollection(List<Feature> features) {
        this.features = features;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

}
