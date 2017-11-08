package io.github.stemlab.utils;

/**
 * @brief SQL where clause operator builder
 *
 * @author Bolat Azamat
 */
public class ClauseUtil {

    /**
     * SQL "=" operator
     *
     * @param column
     * @param value
     * @return
     */
    public static String equal(String column, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append("=").append(value);
        return stringBuilder.toString();
    }

    /**
     * SQL ">" operator
     *
     * @param column
     * @param value
     * @return
     */
    public static String greaterThan(String column, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append(">").append("'").append(value).append("'");
        return stringBuilder.toString();
    }

    /**
     * SQL "<" operator
     *
     * @param column
     * @param value
     * @return
     */
    public static String lessThan(String column, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append("<").append("'").append(value).append("'");
        return stringBuilder.toString();
    }

    /**
     * SQL "<=" operator
     *
     * @param column
     * @param value
     * @return
     */
    public static String equalOrLessThan(String column, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append("<=").append("'").append(value).append("'");
        return stringBuilder.toString();
    }

    /**
     * SQL "=>" operator
     *
     * @param column
     * @param value
     * @return
     */
    public static String equalOrMoreThan(String column, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append(">=").append("'").append(value).append("'");
        return stringBuilder.toString();
    }

    /**
     * SQL "<>" operator
     *
     * @param column
     * @param value
     * @return
     */
    public static String notEqual(String column, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append("<>").append("'").append(value).append("'");
        return stringBuilder.toString();
    }

    /**
     * SQL "as" operator
     *
     * @param column
     * @return
     */
    public static String alias(String column, String alias) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column).append(" ").append("AS").append(" ").append(alias);
        return stringBuilder.toString();
    }
}
