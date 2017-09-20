package io.github.stemlab.utils;

import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.Relation;
import io.github.stemlab.entity.enums.QueryType;
import io.github.stemlab.exception.OSMToolException;

/**
 * @brief Helper to build queries to database (builder pattern)
 * @warning very basic implementation
 *
 * @author Bolat Azamat
 */
public class QueryBuilder {

    private StringBuilder query;
    private String schema;
    private QueryType type;
    private StringBuilder insertOperator;
    private StringBuilder selectOperator;
    private StringBuilder deleteOperator;
    private StringBuilder updateOperator;
    private StringBuilder setOperator;
    private StringBuilder whereOperator;
    private StringBuilder andOperator;
    private StringBuilder insertColumns;
    private StringBuilder insertValues;
    private StringBuilder fromOperator;
    private StringBuilder caseOperator;


    private QueryBuilder() {
        this.query = new StringBuilder();
        this.schema = new String();
        this.insertOperator = new StringBuilder();
        this.selectOperator = new StringBuilder();
        this.deleteOperator = new StringBuilder();
        this.updateOperator = new StringBuilder();
        this.setOperator = new StringBuilder();
        this.whereOperator = new StringBuilder();
        this.andOperator = new StringBuilder();
        this.insertColumns = new StringBuilder();
        this.insertValues = new StringBuilder();
        this.fromOperator = new StringBuilder();
        this.caseOperator = new StringBuilder();
    }

    public static Builder newQuery() {
        return new QueryBuilder().new Builder();
    }

    public String toString() {
        return this.query.toString();
    }

    public class Builder {
        private Builder() {
        }

        public Builder schema(String schema) {
            QueryBuilder.this.schema = schema;
            return this;
        }

        /**
         * SQL update clause
         *
         * @param table
         * @return
         */
        public Builder update(String table) {
            QueryBuilder.this.updateOperator.append("UPDATE ").append(QueryBuilder.this.schema).append(".").append(table);
            return this;
        }

        /**
         * <p>SQL case operator, use @see {@link #caseWhenThen(String, String)} to express 'when'
         * and @see {@link #caseEnd()} to end or @see {@link #caseEndAs(String)} to end with alias </p>
         *
         * @param when
         * @param then
         * @return
         */
        public Builder caseWhenThen(String when, String then) {
            if (QueryBuilder.this.caseOperator.toString().equals("")) {
                QueryBuilder.this.caseOperator.append(", CASE WHEN ").append(when).append(" THEN ").append(then);
            } else {
                QueryBuilder.this.caseOperator.append(" WHEN ").append(when).append(" THEN ").append(then);
            }
            return this;
        }

        /**
         * Ends case operator without alias
         *
         * @return
         */
        public Builder caseEnd() {
            QueryBuilder.this.caseOperator.append(" END ");
            return this;
        }

        /**
         * Ends case operator with alias
         *
         * @param alias
         * @return
         */
        public Builder caseEndAs(String alias) {
            QueryBuilder.this.caseOperator.append(" END AS ").append(alias);
            return this;
        }

        /**
         * SQL delete operator
         *
         * @param table
         * @return
         */
        public Builder delete(String table) {
            QueryBuilder.this.deleteOperator.append("DELETE FROM ").append(QueryBuilder.this.schema).append(".").append(table);
            return this;
        }

        /**
         * SQL insert operator
         *
         * @param table
         * @return
         */
        public Builder insert(String table) {
            QueryBuilder.this.insertOperator.append("INSERT INTO ").append(QueryBuilder.this.schema).append(".").append(table);
            return this;
        }

        /**
         * SQL set expression
         *
         * @param column name of column
         * @param value  value of column
         * @return
         */
        public Builder set(String column, Object value) {
            if (QueryBuilder.this.setOperator.toString().equals("")) {
                QueryBuilder.this.setOperator.append(" ").append("SET ").append(column).append(" = ").append(value);
            } else {
                QueryBuilder.this.setOperator.append(", ").append(column).append(" = ").append(value);
            }
            return this;
        }


        /**
         * SQL values expression
         *
         * @param column name of column
         * @param value  value of column
         * @return
         */
        public Builder values(String column, Object value) {
            if (QueryBuilder.this.insertColumns.toString().equals("")) {
                QueryBuilder.this.insertColumns.append(" ").append(column);
            } else {
                QueryBuilder.this.insertColumns.append(", ").append(column);
            }
            if (QueryBuilder.this.insertValues.toString().equals("")) {
                QueryBuilder.this.insertValues.append(" ").append(value);
            } else {
                QueryBuilder.this.insertValues.append(", ").append(value);
            }
            return this;
        }

        /**
         * SQL values expression from relation of features
         *
         * @param relations
         * @param feature
         * @return
         * @see Relation
         * @see Feature
         */
        public Builder valuesArrayFromFeature(Relation[] relations, Feature feature) {

            for (Relation relation : relations) {
                if (QueryBuilder.this.insertColumns.toString().equals("")) {
                    QueryBuilder.this.insertColumns.append(" ").append(relation.getReference());
                } else {
                    QueryBuilder.this.insertColumns.append(", ").append(relation.getReference());
                }
                if (QueryBuilder.this.insertValues.toString().equals("")) {
                    QueryBuilder.this.insertValues.append(" ").append("'" + feature.getProperties().get(relation.getColumn()) + "'");
                } else {
                    QueryBuilder.this.insertValues.append(", ").append("'" + feature.getProperties().get(relation.getColumn()) + "'");
                }
            }

            return this;
        }


        /**
         * SQL set expression from relation of features
         *
         * @param relations
         * @param feature
         * @return
         * @see Relation
         * @see Feature
         */
        public Builder setArrayFromFeature(Relation[] relations, Feature feature) {
            for (Relation relation : relations) {
                if (QueryBuilder.this.setOperator.toString().equals("")) {
                    QueryBuilder.this.setOperator.append(" ").append("SET ").append(relation.getReference()).append(" = ").append("'" + feature.getProperties().get(relation.getColumn()) + "'");
                } else {
                    QueryBuilder.this.setOperator.append(", ").append(relation.getReference()).append(" = ").append("'" + feature.getProperties().get(relation.getColumn()) + "'");
                }
            }
            return this;
        }

        /**
         * SQL where clause
         *
         * @param clauseString
         * @return
         */
        public Builder where(String clauseString) {
            QueryBuilder.this.whereOperator.append(" ").append("WHERE ").append(clauseString);
            return this;
        }

        /**
         * SQL and expression
         *
         * @param clauseString
         * @return
         */
        public Builder and(String clauseString) {
            QueryBuilder.this.andOperator.append(" ").append("AND ").append(clauseString);
            return this;
        }

        /**
         * SQL select operator (specify set of columns), for table defining use @see {@link #from(String)}
         *
         * @param columns
         * @return
         */
        public Builder select(String... columns) {
            QueryBuilder.this.selectOperator.append("SELECT ");
            StringBuilder tables = new StringBuilder();
            for (String column : columns) {
                if (tables.length() != 0) {
                    tables.append(",");
                }
                tables.append(column);
            }
            QueryBuilder.this.selectOperator.append(tables).append(" ");
            return this;
        }

        /**
         * SQL from expression @see {@link #select(String...)}
         *
         * @param table
         * @return
         */
        public Builder from(String table) {
            QueryBuilder.this.fromOperator.append(" FROM ").append(QueryBuilder.this.schema).append(".").append(table);
            return this;
        }

        /**
         * Define type of query, See possible values in @see {@link QueryType}
         *
         * @param queryType
         * @return
         */
        public Builder queryType(QueryType queryType) {
            QueryBuilder.this.type = queryType;
            return this;
        }

        /**
         * Get assembled query
         * If query type not defined @throws {@link OSMToolException}
         *
         * @return
         */
        public QueryBuilder getQuery() {
            if (QueryBuilder.this.type.equals(QueryType.SELECT)) {
                QueryBuilder.this.query.append(QueryBuilder.this.selectOperator)
                        .append(QueryBuilder.this.caseOperator)
                        .append(QueryBuilder.this.fromOperator)
                        .append(QueryBuilder.this.whereOperator)
                        .append(QueryBuilder.this.andOperator);
                return QueryBuilder.this;
            } else if (QueryBuilder.this.type.equals(QueryType.UPDATE)) {
                QueryBuilder.this.query.append(QueryBuilder.this.updateOperator)
                        .append(QueryBuilder.this.setOperator)
                        .append(QueryBuilder.this.whereOperator)
                        .append(QueryBuilder.this.andOperator);
            } else if (QueryBuilder.this.type.equals(QueryType.INSERT)) {
                QueryBuilder.this.query.append(QueryBuilder.this.insertOperator).append(" (").append(QueryBuilder.this.insertColumns).append(") ")
                        .append(" VALUES ").append(" (").append(QueryBuilder.this.insertValues).append(") ")
                        .append(QueryBuilder.this.whereOperator)
                        .append(QueryBuilder.this.andOperator);
            } else if (QueryBuilder.this.type.equals(QueryType.DELETE)) {
                QueryBuilder.this.query.append(QueryBuilder.this.deleteOperator)
                        .append(QueryBuilder.this.whereOperator)
                        .append(QueryBuilder.this.andOperator);
            } else {
                new OSMToolException("Undefined query type");
            }

            return QueryBuilder.this;
        }
    }


}
