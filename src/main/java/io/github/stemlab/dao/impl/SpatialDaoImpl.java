package io.github.stemlab.dao.impl;

import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.wololo.jts2geojson.GeoJSONReader;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static io.github.stemlab.service.impl.SpatialServiceImpl.*;

/**
 * Created by Azamat on 6/2/2017.
 */
@Repository
public class SpatialDaoImpl implements SpatialDao {

    private static final String ID = "id";
    private static final String TOPOLOGY_TYPE = "topology_type";
    private static final String GEOMETRY_FUNCTION_ST_ASGEOJSON = "st_asgeojson";
    private static final String TABLENAME = "tablename";
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://127.0.0.1:5432/osm2pgsql";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "zxc123";
    public static String GEOM = "geom";
    public static String OSM_ID = "id";
    public static String KR_ID = "gid";
    public static String NAME = "name";
    private static String SRID = "3857";
    private static String OSM_SCHEMA_NAME = "converter_data";
    private static String KR_SCHEMA_NAME = "kr";
    @Autowired
    SessionStore sessionStore;
    private JdbcTemplate jdbcTemplate;

    private static Connection getDBConnection() {

        Connection dbConnection = null;

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
                    DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;

    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope, String table) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        String query = "select " + OSM_ID + ", " + NAME + ", '" + table + "' as tablename, st_asgeojson(" + GEOM + "), case when (ST_Within(" + GEOM + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'))) THEN 'crosses'\n" +
                "      when (ST_Crosses(" + GEOM + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'))) THEN 'within' " +
                "when (ST_Overlaps(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'overlaps' END AS topology_type\n" +
                " from " + OSM_SCHEMA_NAME + "." + table + " where is_del = FALSE AND \n" +
                "ST_Intersects(" + GEOM + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'));";

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
                feature.setId(rs.getLong(ID));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TOPOLOGY_TYPE, rs.getString(TOPOLOGY_TYPE));
                properties.put("name", rs.getString("name"));
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("source", "osm");
                feature.setProperties(properties);

                GeoJSONReader reader = new GeoJSONReader();
                Geometry geometry = reader.read(rs.getString(GEOMETRY_FUNCTION_ST_ASGEOJSON));
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

    public List<Feature> getKRIntersectsWithTopologyType(Envelope envelope, String table) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        String query = "select " + KR_ID + ", " + NAME + ", '" + table + "' as tablename, st_asgeojson(ST_Transform (" + GEOM + ", " + SRID + ")), case when (ST_Within(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'crosses'\n" +
                "      when (ST_Crosses(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'within' " +
                "when (ST_Overlaps(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'overlaps' END AS topology_type\n" +
                " from " + KR_SCHEMA_NAME + "." + table + " where added_to_osm = FALSE AND \n" +
                "ST_Intersects(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179));";

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
                feature.setId(rs.getLong("gid"));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TOPOLOGY_TYPE, rs.getString(TOPOLOGY_TYPE));
                properties.put("name", rs.getString("name"));
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("source", "kr");
                feature.setProperties(properties);

                GeoJSONReader reader = new GeoJSONReader();
                Geometry geometry = reader.read(rs.getString(GEOMETRY_FUNCTION_ST_ASGEOJSON));
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

    public void addToOSM(String from, String dest, Long id) {
        final String query = "insert into " + OSM_SCHEMA_NAME + "." + dest + "(name, geom, kr)\n" +
                "select name,(ST_Transform(geom,3857)),true from " + KR_SCHEMA_NAME + "." + from + " where " + KR_ID + "= ?;";
        jdbcTemplate.update(query, id);
        if (jdbcTemplate.update(query, id) > 0) {
            setAddedToOSM(from, id);
        }
    }

    public void setAddedToOSM(String table, Long id) {
        final String query = "update " + KR_SCHEMA_NAME + "." + table + " set added_to_osm = TRUE where " + KR_ID + " = ?;";
        jdbcTemplate.update(query, id);
    }

    public Double getHausdorffDistance(Long krID, Long osmID) {
        final String query = "select ST_HausdorffDistance((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + KR_ROAD_NAME + " where " + KR_ID + " = ?),(select ST_Transform(" + GEOM + ",5179) from " + OSM_SCHEMA_NAME + "." + OSM_ROAD_NAME + " where " + OSM_ID + " = ?))";
        return jdbcTemplate.queryForObject(query, new Object[]{krID, osmID}, Double.class);
    }

    public Double getSurfaceDistance(Long krID, Long osmID) {
        final String query = "select (ST_Area(ST_Intersection((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + KR_BUILDING_NAME + " where " + KR_ID + " = ?),(select ST_Transform(" + GEOM + ",5179) from " + OSM_SCHEMA_NAME + "." + OSM_BUILDING_NAME + " where " + OSM_ID + " = ?)))\n" +
                "/ST_Area(ST_Union((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + KR_BUILDING_NAME + " where " + KR_ID + " = ?),(select ST_Transform(" + GEOM + ",5179) from " + OSM_SCHEMA_NAME + "." + OSM_BUILDING_NAME + " where " + OSM_ID + " = ?)))) * 100";
        return jdbcTemplate.queryForObject(query, new Object[]{krID, osmID, krID, osmID}, Double.class);
    }

    public void replaceObjects(String tableTo, String tableFrom, Long idTo, Long idFrom) {
        final String query = "update " + OSM_SCHEMA_NAME + "." + tableTo + " set " + GEOM + " = (ST_Transform((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + tableFrom + " where " + KR_ID + " = ?),3857)) where " + OSM_ID + " = ?";
        if (jdbcTemplate.update(query, idFrom, idTo) > 0) {
            setAddedToOSM(tableFrom, idFrom);
        }
    }

    public void deleteObjects(String table, Long id) {
        final String query = "UPDATE " + OSM_SCHEMA_NAME + "." + table + " SET is_del = true where " + OSM_ID + " = ?";
        jdbcTemplate.update(query, id);
    }
}
