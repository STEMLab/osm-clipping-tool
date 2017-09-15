package io.github.stemlab.dao.impl;

import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.entity.enums.QueryType;
import io.github.stemlab.exception.DatabaseException;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import io.github.stemlab.session.SessionStore;
import io.github.stemlab.utils.ClauseUtil;
import io.github.stemlab.utils.QueryBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Repository
public class SpatialDaoImpl implements SpatialDao {


    private static final Logger logger = Logger.getLogger(SpatialDaoImpl.class);
    private static final String TOPOLOGY_TYPE = "topology_type";
    private static final String TABLENAME = "tablename";
    private static final String SRID = "3857";

    @Autowired
    SessionStore sessionStore;

    @Autowired
    SpatialService spatialService;

    @Autowired
    Database database;

    private Connection getDBConnection() throws DatabaseException {

        Connection dbConnection = null;

        try {
            Class.forName(database.getDriver());
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found", e);
            throw new DatabaseException("PostgreSQL driver not found");
        }

        try {
            dbConnection = DriverManager.getConnection(database.getConnection(), database.getUser(),
                    database.getPassword());
            return dbConnection;
        } catch (SQLException e) {
            logger.error("Fail on connection", e);
            throw new DatabaseException("Connection can't be established");
        }
    }

    public List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        int srid = getTableSRID(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm(), database.getTableWrapper().getOsmGeom());
        List<Column> columns = spatialService.getOSMColumnsWithoutMainAttributes();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getOsmSchema()).select("*", database.getTableWrapper().getOsmKey(), ClauseUtil.alias("'" + database.getTableWrapper().getOsm() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getOsmGeom() + ", " + SRID + "))", "geojson")
        ).caseWhenThen("(ST_Within(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'within'")
                .caseWhenThen("(ST_Crosses(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'crosses'")
                .caseWhenThen("(ST_Overlaps(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'overlaps'")
                .caseEndAs("topology_type")
                .from(database.getTableWrapper().getOsm()).where("ST_Intersects(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))").getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            for (int i = 1; i <= 16; i = i + 4) {
                preparedStatement.setDouble(i, envelope.getxMin());
                preparedStatement.setDouble((i) + 1, envelope.getyMin());
                preparedStatement.setDouble((i) + 2, envelope.getxMax());
                preparedStatement.setDouble((i) + 3, envelope.getyMax());
            }

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {

                Feature feature = new Feature();
                feature.setId(rs.getLong(database.getTableWrapper().getOsmKey()));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TOPOLOGY_TYPE, rs.getString(TOPOLOGY_TYPE));
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("source", "osm");

                for (Column column : columns) {
                    properties.put(column.getName(), String.valueOf(rs.getObject(column.getName())));
                }

                feature.setProperties(properties);

                GeoJSONReader reader = new GeoJSONReader();
                Geometry geometry = (rs.getString("geojson") != null) ? reader.read(rs.getString("geojson")) : null;
                feature.setGeometry(geometry);

                if (feature.getGeometry().getGeometryType().equals("LineString") || feature.getGeometry().getGeometryType().equals("MultiLineString")) {
                    sessionStore.putToLineTree(feature);
                } else if (feature.getGeometry().getGeometryType().equals("Polygon") || feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                    sessionStore.putToSurfaceTree(feature);
                }

                features.add(feature);
            }


        } catch (SQLException e) {
            logger.error("Exception on getOSMIntersectsWithTopologyType() ", e);
            throw new DatabaseException("Exception on getting intersected objects");
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return features;

    }

    public List<Feature> getKRIntersectsWithTopologyType(Envelope envelope) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        int srid = getTableSRID(database.getTableWrapper().getOriginSchema(), database.getTableWrapper().getOrigin(), database.getTableWrapper().getOriginGeom());
        List<Column> columns = spatialService.getOriginColumnsWithoutMainAttributes();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getOriginSchema()).select("*", database.getTableWrapper().getOriginKey(), ClauseUtil.alias("'" + database.getTableWrapper().getOrigin() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getOriginGeom() + ", " + SRID + "))", "geojson")
        ).caseWhenThen("(ST_Within(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'within'")
                .caseWhenThen("(ST_Crosses(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'crosses'")
                .caseWhenThen("(ST_Overlaps(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'overlaps'")
                .caseEndAs("topology_type")
                .from(database.getTableWrapper().getOrigin()).where("ST_Intersects(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))").getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            for (int i = 1; i <= 16; i = i + 4) {
                preparedStatement.setDouble(i, envelope.getxMin());
                preparedStatement.setDouble((i) + 1, envelope.getyMin());
                preparedStatement.setDouble((i) + 2, envelope.getxMax());
                preparedStatement.setDouble((i) + 3, envelope.getyMax());
            }
            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {

                Feature feature = new Feature();
                feature.setId(rs.getLong(database.getTableWrapper().getOriginKey()));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TOPOLOGY_TYPE, rs.getString(TOPOLOGY_TYPE));
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("source", "un");

                for (Column column : columns) {
                    properties.put(column.getName(), String.valueOf(rs.getObject(column.getName())));
                }

                feature.setProperties(properties);

                GeoJSONReader reader = new GeoJSONReader();
                Geometry geometry = (rs.getString("geojson") != null) ? reader.read(rs.getString("geojson")) : null;
                feature.setGeometry(geometry);

                if (feature.getGeometry().getGeometryType().equals("LineString") || feature.getGeometry().getGeometryType().equals("MultiLineString")) {
                    sessionStore.putToLineTree(feature);
                } else if (feature.getGeometry().getGeometryType().equals("Polygon") || feature.getGeometry().getGeometryType().equals("MultiPolygon")) {
                    sessionStore.putToSurfaceTree(feature);
                }

                features.add(feature);
            }


        } catch (SQLException e) {

            logger.error("Exception on getKRIntersectsWithTopologyType() ", e);
            throw new DatabaseException("Exception on getting intersected objects");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return features;

    }

    @Override
    public List<Feature> getUNFeatures() throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getOriginSchema()).select("*", database.getTableWrapper().getOriginKey(), ClauseUtil.alias("'" + database.getTableWrapper().getOrigin() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getOriginGeom() + ", " + SRID + "))", "geojson")
        ).from(database.getTableWrapper().getOrigin()).getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();
            List<Column> columns = spatialService.getOriginColumnsWithoutMainAttributes();

            while (rs.next()) {

                Feature feature = new Feature();
                feature.setId(rs.getLong(database.getTableWrapper().getOriginKey()));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("source", "un");

                for (Column column : columns) {
                    properties.put(column.getName(), String.valueOf(rs.getObject(column.getName())));
                }

                feature.setProperties(properties);

                GeoJSONReader reader = new GeoJSONReader();
                Geometry geometry = (rs.getString("geojson") != null) ? reader.read(rs.getString("geojson")) : null;
                feature.setGeometry(geometry);

                features.add(feature);
            }


        } catch (SQLException e) {

            logger.error("Exception on getUNFeatures() ", e);
            throw new DatabaseException("Exception on getting objects from DB");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return features;
    }

    @Override
    public List<Feature> getOSMFeatures() throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getOsmSchema()).select("*", database.getTableWrapper().getOsmKey(), ClauseUtil.alias("'" + database.getTableWrapper().getOsm() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getOsmGeom() + ", " + SRID + "))", "geojson")
        ).from(database.getTableWrapper().getOsm()).getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();
            List<Column> columns = spatialService.getOSMColumnsWithoutMainAttributes();

            while (rs.next()) {

                Feature feature = new Feature();
                feature.setId(rs.getLong(database.getTableWrapper().getOsmKey()));
                HashMap<String, String> properties = new HashMap<String, String>();


                for (Column column : columns) {
                    properties.put(column.getName(), String.valueOf(rs.getObject(column.getName())));
                }

                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("source", "osm");
                feature.setProperties(properties);

                GeoJSONReader reader = new GeoJSONReader();
                Geometry geometry = (rs.getString("geojson") != null) ? reader.read(rs.getString("geojson")) : null;
                feature.setGeometry(geometry);
                features.add(feature);
            }


        } catch (SQLException e) {

            logger.error("Exception on getOSMFeatures() ", e);
            throw new DatabaseException("Exception on getting objects from DB");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return features;
    }

    public void addToOSM(Feature feature) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        GeoJSONWriter writer = new GeoJSONWriter();
        int osmSRID = getTableSRID(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm(), database.getTableWrapper().getOsmGeom());
        Long key = generateKey();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.INSERT).schema(database.getTableWrapper().getOsmSchema()).insert(database.getTableWrapper().getOsm())
                .values(database.getTableWrapper().getOsmKey(), key)
                .values(database.getTableWrapper().getOsmGeom(), "(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(feature.getGeometry()) + "')," + SRID + ")," + osmSRID + "))")
                .valuesArrayFromFeature(database.getTableWrapper().getRelations(), feature)
                .getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), key, Action.ADD);

        } catch (SQLException e) {

            logger.error("Exception on addToOSM() ", e);
            throw new DatabaseException("Exception on adding object into DB");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public void replaceObjects(Feature from, Feature to) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        GeoJSONWriter writer = new GeoJSONWriter();
        int osmSRID = getTableSRID(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm(), database.getTableWrapper().getOsmGeom());

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.UPDATE).schema(database.getTableWrapper().getOsmSchema()).update(database.getTableWrapper().getOsm())
                .set(database.getTableWrapper().getOsmGeom(), "(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(from.getGeometry()) + "')," + SRID + ")," + osmSRID + "))")
                .setArrayFromFeature(database.getTableWrapper().getRelations(), from)
                .where(ClauseUtil.equal(database.getTableWrapper().getOsmKey(), "?")).getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            preparedStatement.setLong(1, to.getId());

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), to.getId(), Action.REPLACE);

        } catch (SQLException e) {

            logger.error("Exception on replaceObjects() ", e);
            throw new DatabaseException("Exception on replacing object in DB");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public void deleteObjects(Feature feature) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.DELETE).schema(database.getTableWrapper().getOsmSchema()).delete(database.getTableWrapper().getOsm()).where(ClauseUtil.equal(database.getTableWrapper().getOsmKey(), "?")).getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            preparedStatement.setLong(1, feature.getId());

            // execute delete SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), feature.getId(), Action.DELETE);

        } catch (SQLException e) {

            logger.error("Exception on deleteObjects() ", e);
            throw new DatabaseException("Exception on deleting object in DB");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    private int getTableSRID(String schema, String table, String column) throws SQLException {

        final String query = "SELECT Find_SRID(?, ?, ?) as srid;";

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Integer srid = null;


        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);


            preparedStatement.setString((1), schema);
            preparedStatement.setString((2), table);
            preparedStatement.setString((3), column);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                srid = rs.getInt("srid");
            }


        } catch (SQLException e) {

            logger.error("Exception on getTableSRID() ", e);
            throw new DatabaseException("Exception on getting object SRID");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        if (srid == 0) {
            throw new DatabaseException(schema + "." + table + " SRID not deinfed");
        }

        return srid;
    }

    private Long generateKey() throws SQLException {

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getOsmSchema()).select(ClauseUtil.alias("max(" + database.getTableWrapper().getOsmKey() + ")", "id")).from(database.getTableWrapper().getOsm()).getQuery();

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Long id = null;

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                id = rs.getLong("id");
            }


        } catch (SQLException e) {

            logger.error("Exception on generateKey() ", e);
            throw new DatabaseException("Exception on generating key for " + database.getTableWrapper().getOsm());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return ++id;
    }

    public void testConnection(String connection, String user, String password) throws SQLException {
        try {
            Class.forName(database.getDriver());
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found ", e);
            throw new DatabaseException("PostgreSQL driver not found");
        }

        try {
            DriverManager.getConnection(connection, user,
                    password);
        } catch (SQLException e) {
            logger.error("Connection not established ", e);
            throw new DatabaseException("Connection can't be established");
        }
    }

    public List<String> getSchemas() throws SQLException {

        List<String> schema = new ArrayList<>();

        Connection conn = null;

        try {
            conn = getDBConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet schemas = meta.getSchemas();
            while (schemas.next()) {
                String tableSchema = schemas.getString(1); //"TABLE_CATALOG"
                schema.add(tableSchema);
            }
        } catch (SQLException e) {
            logger.error("Exception on getSchemas()", e);
            throw new DatabaseException("Error on getting schemas from DB");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return schema;
    }

    public List<String> getTables(String schema) throws SQLException {

        List<String> table = new ArrayList<>();

        Connection conn = null;

        try {
            conn = getDBConnection();

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, schema, null, new String[]{"TABLE"});
            while (tables.next()) {
                table.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            logger.error("Exception on getTables()", e);
            throw new DatabaseException("Error on getting tables list from DB");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return table;
    }

    public List<Column> getColumns(String schema, String table) throws SQLException {

        List<Column> columns = new ArrayList<>();

        Connection conn = null;

        try {
            conn = getDBConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet resultSet = meta.getColumns(null, schema, table, null);

            ResultSet rs = meta.getPrimaryKeys(null, schema, table);
            String primaryKey = null;
            while (rs.next()) {
                primaryKey = rs.getString("COLUMN_NAME");
            }

            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                String type = resultSet.getString("TYPE_NAME");
                int size = resultSet.getInt("COLUMN_SIZE");
                if (name.equals(primaryKey)) {
                    columns.add(new Column(name, "primary", size));
                } else {
                    columns.add(new Column(name, type, size));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception on getColumns()", e);
            throw new DatabaseException("Error on getting columns of table");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return columns;
    }

    @Override
    public void updateFeature(Feature feature) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        GeoJSONWriter writer = new GeoJSONWriter();
        int osmSRID = getTableSRID(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm(), database.getTableWrapper().getOsmGeom());

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.UPDATE).schema(database.getTableWrapper().getOsmSchema()).update(database.getTableWrapper().getOsm()).
                set(database.getTableWrapper().getOsmGeom(), "(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(feature.getGeometry()) + "')," + SRID + ")," + osmSRID + "))").
                where(ClauseUtil.equal(database.getTableWrapper().getOsmKey(), "?")).getQuery();

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            preparedStatement.setLong(1, feature.getId());

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), feature.getId(), Action.UPDATE);

        } catch (SQLException e) {

            logger.error("Exception on updateFeature()", e);
            throw new DatabaseException("Error on updating geometry of object");

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }

    }
}
