package io.github.stemlab.entity;

import java.io.Serializable;

/**
 * @brief Define relation between source and target tables columns
 * @author Bolat Azamat
 */
public class Relation implements Serializable{

    private String sourceColumn;
    private String targetColumn;

    public Relation(){}

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }
}
