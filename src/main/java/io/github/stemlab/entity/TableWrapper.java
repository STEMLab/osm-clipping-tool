package io.github.stemlab.entity;

import java.io.Serializable;

/**
 * Created by Azamat on 8/2/2017.
 */
public class TableWrapper implements Serializable{
    private String origin;
    private String osm;
    private Relation[] relations;

    public TableWrapper() {
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOsm() {
        return osm;
    }

    public void setOsm(String osm) {
        this.osm = osm;
    }

    public Relation[] getRelations() {
        return relations;
    }

    public void setRelations(Relation[] relations) {
        this.relations = relations;
    }
}
