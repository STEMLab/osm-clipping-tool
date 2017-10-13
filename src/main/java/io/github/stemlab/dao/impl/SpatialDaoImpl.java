package io.github.stemlab.dao.impl;

import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.dao.ConnectionDao;
import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.enums.QueryType;
import io.github.stemlab.exception.DatabaseException;
import io.github.stemlab.service.ConnectionService;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import io.github.stemlab.session.SessionStore;
import io.github.stemlab.utils.ClauseUtil;
import io.github.stemlab.utils.QueryBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.wololo.jts2geojson.GeoJSONReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @brief Data access object implementation for SpatialDao class
 * @see SpatialDao
 *
 * @author Bolat Azamat.
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
    ConnectionService connectionService;

    @Autowired
    Database database;

    @Override
    public List<Feature> getTargetIntersectionWithTopologyType(Envelope envelope) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        int srid = getSRID(database.getTableWrapper().getTargetSchema(), database.getTableWrapper().getTargetTable(), database.getTableWrapper().getTargetGeomColumn());
        List<Column> columns = spatialService.getOSMColumnsWithoutMainAttributes();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getTargetSchema()).select("*", database.getTableWrapper().getTargetKeyColumn(), ClauseUtil.alias("'" + database.getTableWrapper().getTargetTable() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getTargetGeomColumn() + ", " + SRID + "))", "geojson")
        ).caseWhenThen("(ST_Within(" + database.getTableWrapper().getTargetGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'within'")
                .caseWhenThen("(ST_Crosses(" + database.getTableWrapper().getTargetGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'crosses'")
                .caseWhenThen("(ST_Overlaps(" + database.getTableWrapper().getTargetGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'overlaps'")
                .caseEndAs("topology_type")
                .from(database.getTableWrapper().getTargetTable()).where("ST_Intersects(" + database.getTableWrapper().getTargetGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))").getQuery();

        try {
            dbConnection = connectionService.getConnection();
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
                feature.setId(rs.getLong(database.getTableWrapper().getTargetKeyColumn()));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TOPOLOGY_TYPE, rs.getString(TOPOLOGY_TYPE));
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("table_type", "target");

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

    @Override
    public List<Feature> getSourceIntersectionWithTopologyType(Envelope envelope) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        int srid = getSRID(database.getTableWrapper().getSourceSchema(), database.getTableWrapper().getSourceTable(), database.getTableWrapper().getSourceGeomColumn());
        List<Column> columns = spatialService.getOriginColumnsWithoutMainAttributes();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getSourceSchema()).select("*", database.getTableWrapper().getSourceKeyColumn(), ClauseUtil.alias("'" + database.getTableWrapper().getSourceTable() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getSourceGeomColumn() + ", " + SRID + "))", "geojson")
        ).caseWhenThen("(ST_Within(" + database.getTableWrapper().getSourceGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'within'")
                .caseWhenThen("(ST_Crosses(" + database.getTableWrapper().getSourceGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'crosses'")
                .caseWhenThen("(ST_Overlaps(" + database.getTableWrapper().getSourceGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + ")))", "'overlaps'")
                .caseEndAs("topology_type")
                .from(database.getTableWrapper().getSourceTable()).where("ST_Intersects(" + database.getTableWrapper().getSourceGeomColumn() + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "')," + srid + "))").getQuery();

        try {
            dbConnection = connectionService.getConnection();
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
                feature.setId(rs.getLong(database.getTableWrapper().getSourceKeyColumn()));
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
    public List<Feature> getSourceFeatures() throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getSourceSchema()).select("*", database.getTableWrapper().getSourceKeyColumn(), ClauseUtil.alias("'" + database.getTableWrapper().getSourceTable() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getSourceGeomColumn() + ", " + SRID + "))", "geojson")
        ).from(database.getTableWrapper().getSourceTable()).getQuery();

        try {
            dbConnection = connectionService.getConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();
            List<Column> columns = spatialService.getOriginColumnsWithoutMainAttributes();

            while (rs.next()) {

                Feature feature = new Feature();
                feature.setId(rs.getLong(database.getTableWrapper().getSourceKeyColumn()));
                HashMap<String, String> properties = new HashMap<String, String>();
                properties.put(TABLENAME, rs.getString(TABLENAME));
                properties.put("table_type", "source");

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
    public List<Feature> getTargetFeatures() throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        List<Feature> features = new LinkedList<>();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getTargetSchema()).select("*", database.getTableWrapper().getTargetKeyColumn(), ClauseUtil.alias("'" + database.getTableWrapper().getTargetTable() + "'", "tablename"),
                ClauseUtil.alias("st_asgeojson(ST_Transform (" + database.getTableWrapper().getTargetGeomColumn() + ", " + SRID + "))", "geojson")
        ).from(database.getTableWrapper().getTargetTable()).getQuery();

        try {
            dbConnection = connectionService.getConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            // execute select SQL stetement
            ResultSet rs = preparedStatement.executeQuery();
            List<Column> columns = spatialService.getOSMColumnsWithoutMainAttributes();

            while (rs.next()) {

                Feature feature = new Feature();
                feature.setId(rs.getLong(database.getTableWrapper().getTargetKeyColumn()));
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

    @Override
    public Long generateFeatureId() throws SQLException {
        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.SELECT).schema(database.getTableWrapper().getTargetSchema()).select(ClauseUtil.alias("max(" + database.getTableWrapper().getTargetKeyColumn() + ")", "id")).from(database.getTableWrapper().getTargetTable()).getQuery();

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Long id = null;

        try {
            dbConnection = connectionService.getConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                id = rs.getLong("id");
            }


        } catch (SQLException e) {

            logger.error("Exception on generateKey() ", e);
            throw new DatabaseException("Exception on generating key for " + database.getTableWrapper().getTargetTable());

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

    @Override
    public int getSRID(String schema, String table, String column) throws SQLException {
        final String query = "SELECT Find_SRID(?, ?, ?) as srid;";

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        Integer srid = null;


        try {
            dbConnection = connectionService.getConnection();
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
}
