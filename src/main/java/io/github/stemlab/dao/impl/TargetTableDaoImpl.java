package io.github.stemlab.dao.impl;

import io.github.stemlab.dao.TargetTableDao;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.enums.Action;
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
import org.wololo.jts2geojson.GeoJSONWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


@Repository
public class TargetTableDaoImpl implements TargetTableDao {

    private static final Logger logger = Logger.getLogger(TargetTableDaoImpl.class);
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
    public void add(Feature feature) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        GeoJSONWriter writer = new GeoJSONWriter();
        int osmSRID = spatialService.getSRID(database.getTableWrapper().getTargetSchema(), database.getTableWrapper().getTargetTable(), database.getTableWrapper().getTargetGeomColumn());
        Long key = spatialService.generateFeatureId();

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.INSERT).schema(database.getTableWrapper().getTargetSchema()).insert(database.getTableWrapper().getTargetTable())
                .values(database.getTableWrapper().getTargetKeyColumn(), key)
                .values(database.getTableWrapper().getTargetGeomColumn(), "(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(feature.getGeometry()) + "')," + SRID + ")," + osmSRID + "))")
                .valuesArrayFromFeature(database.getTableWrapper().getRelations(), feature)
                .getQuery();

        try {
            dbConnection = connectionService.getConnection();
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

    @Override
    public void replace(Feature source, Feature target) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        GeoJSONWriter writer = new GeoJSONWriter();
        int osmSRID = spatialService.getSRID(database.getTableWrapper().getTargetSchema(), database.getTableWrapper().getTargetTable(), database.getTableWrapper().getTargetGeomColumn());

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.UPDATE).schema(database.getTableWrapper().getTargetSchema()).update(database.getTableWrapper().getTargetTable())
                .set(database.getTableWrapper().getTargetGeomColumn(), "(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(source.getGeometry()) + "')," + SRID + ")," + osmSRID + "))")
                .setArrayFromFeature(database.getTableWrapper().getRelations(), source)
                .where(ClauseUtil.equal(database.getTableWrapper().getTargetKeyColumn(), "?")).getQuery();

        try {
            dbConnection = connectionService.getConnection();
            preparedStatement = dbConnection.prepareStatement(builder.toString());

            preparedStatement.setLong(1, target.getId());

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            spatialService.logAction(sessionStore.getIP(), target.getId(), Action.REPLACE);

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

    @Override
    public void delete(Feature feature) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.DELETE).schema(database.getTableWrapper().getTargetSchema()).delete(database.getTableWrapper().getTargetTable()).where(ClauseUtil.equal(database.getTableWrapper().getTargetKeyColumn(), "?")).getQuery();

        try {
            dbConnection = connectionService.getConnection();
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

    public void update(Feature feature) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        GeoJSONWriter writer = new GeoJSONWriter();
        int osmSRID = spatialService.getSRID(database.getTableWrapper().getTargetSchema(), database.getTableWrapper().getTargetTable(), database.getTableWrapper().getTargetGeomColumn());

        QueryBuilder builder = QueryBuilder.newQuery().queryType(QueryType.UPDATE).schema(database.getTableWrapper().getTargetSchema()).update(database.getTableWrapper().getTargetTable()).
                set(database.getTableWrapper().getTargetGeomColumn(), "(ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON('" + writer.write(feature.getGeometry()) + "')," + SRID + ")," + osmSRID + "))").
                where(ClauseUtil.equal(database.getTableWrapper().getTargetKeyColumn(), "?")).getQuery();

        try {
            dbConnection = connectionService.getConnection();
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
