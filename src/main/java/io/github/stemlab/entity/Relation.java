package io.github.stemlab.entity;

import java.io.Serializable;

/**
 * Created by Azamat on 8/2/2017.
 */
public class Relation implements Serializable{

    private String column;
    private String reference;

    public Relation(){}

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
