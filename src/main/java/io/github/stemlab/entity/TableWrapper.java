package io.github.stemlab.entity;

import java.io.Serializable;

/**
 * @brief Wrapper class for two defined tables and their relations
 *  To move data between two tables, define first one as 'source' and second tables as 'target'.
 *  So, data moves from 'source' to 'target'
 * @author Bolat Azamat
 */
public class TableWrapper implements Serializable {

    private String sourceTable; // source table name
    private String sourceKeyColumn; // source table's primary key column name
    private String sourceGeomColumn; // source table's geometry column name
    private String sourceSchema; // schema where source table placed
    private String targetTable; // target table name
    private String targetKeyColumn; // target table's primary key column name
    private String targetGeomColumn; // target table's geometry column name
    private String targetSchema; // schema where target table placed
    /**
     *  Column relation for source and target tables
     *  @see Relation
     */
    private Relation[] relations;

    public TableWrapper() {
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getSourceKeyColumn() {
        return sourceKeyColumn;
    }

    public void setSourceKeyColumn(String sourceKeyColumn) {
        this.sourceKeyColumn = sourceKeyColumn;
    }

    public String getSourceGeomColumn() {
        return sourceGeomColumn;
    }

    public void setSourceGeomColumn(String sourceGeomColumn) {
        this.sourceGeomColumn = sourceGeomColumn;
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public void setSourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetKeyColumn() {
        return targetKeyColumn;
    }

    public void setTargetKeyColumn(String targetKeyColumn) {
        this.targetKeyColumn = targetKeyColumn;
    }

    public String getTargetGeomColumn() {
        return targetGeomColumn;
    }

    public void setTargetGeomColumn(String targetGeomColumn) {
        this.targetGeomColumn = targetGeomColumn;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    public Relation[] getRelations() {
        return relations;
    }

    public void setRelations(Relation[] relations) {
        this.relations = relations;
    }
}
