package io.github.stemlab.dao.impl;

import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.Relation;
import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.exception.DatabaseException;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import io.github.stemlab.session.SessionStore;
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
            System.out.println(e);
            throw new DatabaseException("PostgreSQL driver not found");
        }

        try {
            dbConnection = DriverManager.getConnection(database.getConnection(), database.getUser(),
                    database.getPassword());
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("Connection can't be established");
        }
    }

    public List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        int srid = getTableSRID(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm(), database.getTableWrapper().getOsmGeom());
        List<Column> columns = getColumns(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm());
        columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOsmKey()));
        columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOsmGeom()));

        String query = "select *," + database.getTableWrapper().getOsmKey() + ", '" + database.getTableWrapper().getOsm() + "' as tablename, st_asgeojson(ST_Transform (" + database.getTableWrapper().getOsmGeom() + ", " + SRID + ")) as geojson" +
                ", case when (ST_Within(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))) THEN 'crosses'\n" +
                "      when (ST_Crosses(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))) THEN 'within' " +
                "when (ST_Overlaps(" + database.getTableWrapper().getOsmGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))) THEN 'overlaps' END AS topology_type\n" +
                " from " + database.getTableWrapper().getOsmSchema() + "." + database.getTableWrapper().getOsm() + " where " +
                "ST_Intersects(" + database.getTableWrapper().getOsmGeom() + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'));";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            for (int i = 1; i <= 16; i = i + 4) {
                preparedStatement.setDouble(i, envelope.getxMin());
                preparedStatement.setDouble((i) + 1, envelope.getyMin());
                preparedStatement.setDouble((i) + 2, envelope.getxMax());
                preparedStatement.setDouble((i) + 3, envelope.getyMax());
            }

            System.out.println(query);

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

            System.out.println(e.getMessage());

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
        List<Column> columns = getColumns(database.getTableWrapper().getOriginSchema(), database.getTableWrapper().getOrigin());
        columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOriginKey()));
        columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOriginGeom()));

        String query = "select *, " + database.getTableWrapper().getOriginKey() + ",'" + database.getTableWrapper().getOrigin() + "' as tablename, st_asgeojson(ST_Transform (" + database.getTableWrapper().getOriginGeom() + ", " + SRID + ")) as geojson, " +
                "case when (ST_Within(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))) THEN 'crosses'\n" +
                "      when (ST_Crosses(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))) THEN 'within' " +
                "when (ST_Overlaps(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))) THEN 'overlaps' END AS topology_type\n" +
                " from " + database.getTableWrapper().getOriginSchema() + "." + database.getTableWrapper().getOrigin() + " where " +
                "ST_Intersects(" + database.getTableWrapper().getOriginGeom() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "));";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            for (int i = 1; i <= 16; i = i + 4) {
                preparedStatement.setDouble(i, envelope.getxMin());
                preparedStatement.setDouble((i) + 1, envelope.getyMin());
                preparedStatement.setDouble((i) + 2, envelope.getxMax());
                preparedStatement.setDouble((i) + 3, envelope.getyMax());
            }

            System.out.println(query);

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

            System.out.println(e.getMessage());

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

        String query = "select *, '" + database.getTableWrapper().getOrigin() + "' as tablename, st_asgeojson(ST_Transform (" + database.getTableWrapper().getOriginGeom() + ", " + SRID + ")) as geojson from " + database.getTableWrapper().getOriginSchema() + "." + database.getTableWrapper().getOrigin() + ";";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            System.out.println(query);

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();
            List<Column> columns = getColumns(database.getTableWrapper().getOriginSchema(), database.getTableWrapper().getOrigin());
            columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOriginKey()));
            columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOriginGeom()));

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

            System.out.println(e.getMessage());

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

        String query = "select *, '" + database.getTableWrapper().getOsm() + "' as tablename, st_asgeojson(ST_Transform (" + database.getTableWrapper().getOsmGeom() + ", " + SRID + ")) as geojson from " + database.getTableWrapper().getOsmSchema() + "." + database.getTableWrapper().getOsm() + ";";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            System.out.println(query);

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();
            List<Column> columns = getColumns(database.getTableWrapper().getOsmSchema(), database.getTableWrapper().getOsm());
            columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOsmKey()));
            columns.removeIf(p -> p.getName().equals(database.getTableWrapper().getOsmGeom()));

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

            System.out.println(e.getMessage());

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

        String insertColumns = "";
        String insertValues = "";
        for (Relation relation : database.getTableWrapper().getRelations()) {
            insertColumns += " , " + relation.getReference();//kr
            insertValues += " , '" + feature.getProperties().get(relation.getColumn()) + "' ";
            relation.getColumn(); //osm
        }

        final String query = "insert into " + database.getTableWrapper().getOsmSchema() + "." + database.getTableWrapper().getOsm() + "(" + database.getTableWrapper().getOsmKey() + ", " + database.getTableWrapper().getOsmGeom() + insertColumns + ")\n" +
                "VALUES ('" + generateKey() + "',(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(feature.getGeometry()) + "')," + SRID + ")," + osmSRID + ")) " + insertValues + ");";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            // execute insert SQL stetement
            preparedStatement.executeUpdate();


            spatialService.logAction(sessionStore.getIP(), generateKey(), Action.ADD);


            System.out.println("Record is inserted into DBUSER table!");

        } catch (SQLException e) {

            System.out.println(e.getMessage());

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


        String setStatement = "";
        for (Relation relation : database.getTableWrapper().getRelations()) {
            setStatement += " , " + relation.getReference() + " = '" + from.getProperties().get(relation.getColumn()) + "'";
        }

        final String query = "update " + database.getTableWrapper().getOsmSchema() + "." + database.getTableWrapper().getOsm() +
                " set " + database.getTableWrapper().getOsmGeom() + " = (ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(from.getGeometry()) + "')," + SRID + ")," + osmSRID + ")) " + setStatement + " where " + database.getTableWrapper().getOsmKey() + " = ?";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            preparedStatement.setLong(1, to.getId());

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), to.getId(), Action.REPLACE);

            System.out.println("Record is replaced in table!");

        } catch (SQLException e) {

            System.out.println(e.getMessage());

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

        final String query = "DELETE from " + database.getTableWrapper().getOsmSchema() + "." + database.getTableWrapper().getOsm() + " where " + database.getTableWrapper().getOsmKey() + " = ?";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            preparedStatement.setLong(1, feature.getId());

            // execute delete SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), feature.getId(), Action.DELETE);

            System.out.println("Record is deleted!");

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    @Override
    public void logAction(String ip, Long osm_id, Action action) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        final String query = "INSERT INTO log(ip,osm_id,action) values(?,?,?)";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            preparedStatement.setString(1, ip);
            preparedStatement.setLong(2, osm_id);
            preparedStatement.setString(3, action.toString());

            // execute delete SQL stetement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {

            System.out.println(e.getMessage());

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

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return srid;
    }

    private Long generateKey() throws SQLException {

        final String query = "select max(" + database.getTableWrapper().getOsmKey() + ") as id from " + database.getTableWrapper().getOsmSchema() + "." + database.getTableWrapper().getOsm() + ";";

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Long id = null;

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                id = rs.getLong("id");
            }


        } catch (SQLException e) {

            System.out.println(e.getMessage());

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

    public void testConnection() throws SQLException {
        getDBConnection();
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
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
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
                System.out.println("Column name: [" + name + "]; type: [" + type + "]; size: [" + size + "]");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return columns;
    }
}
