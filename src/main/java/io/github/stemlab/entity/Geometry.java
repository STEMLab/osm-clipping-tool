package io.github.stemlab.entity;

import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public class Geometry {

    private String type;

    private List coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List coordinates) {
        this.coordinates = coordinates;
    }
}
