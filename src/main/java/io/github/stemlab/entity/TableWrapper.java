package io.github.stemlab.entity;

import java.io.Serializable;

/**
 * Created by Azamat on 8/2/2017.
 */
public class TableWrapper implements Serializable {

    private String origin;
    private String originKey;
    private String originGeom;
    private String originSchema;
    private String osm;
    private String osmKey;
    private String osmGeom;
    private String osmSchema;
    private Relation[] relations;

    public TableWrapper() {
    }

    public String getOriginSchema() {
        return originSchema;
    }

    public void setOriginSchema(String originSchema) {
        this.originSchema = originSchema;
    }

    public String getOsmSchema() {
        return osmSchema;
    }

    public void setOsmSchema(String osmSchema) {
        this.osmSchema = osmSchema;
    }

    public String getOriginKey() {
        return originKey;
    }

    public void setOriginKey(String originKey) {
        this.originKey = originKey;
    }

    public String getOriginGeom() {
        return originGeom;
    }

    public void setOriginGeom(String originGeom) {
        this.originGeom = originGeom;
    }

    public String getOsmKey() {
        return osmKey;
    }

    public void setOsmKey(String osmKey) {
        this.osmKey = osmKey;
    }

    public String getOsmGeom() {
        return osmGeom;
    }

    public void setOsmGeom(String osmGeom) {
        this.osmGeom = osmGeom;
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
