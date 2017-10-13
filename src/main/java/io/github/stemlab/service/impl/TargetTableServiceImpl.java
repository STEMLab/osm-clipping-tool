package io.github.stemlab.service.impl;

import io.github.stemlab.dao.TargetTableDao;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.exception.OSMToolException;
import io.github.stemlab.service.TargetTableService;
import io.github.stemlab.session.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

/**
 * Created by Azamat on 9/21/2017.
 */
@Service
public class TargetTableServiceImpl implements TargetTableService {

    private static String TABLE_NAME_PROPERTY = "tablename";
    private static String UNDEFINED_TABLE_EXCEPTION = "UNDEFINED TABLE:";
    @Autowired
    TargetTableDao targetTableDao;
    @Autowired
    Database database;

    @Override
    public void add(Feature[] features) throws SQLException, OSMToolException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getSourceTable()) && feature.getProperties().get("table_type").equals("source")) {
                    targetTableDao.add(feature);
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }
    }

    @Override
    public void replace(Feature[] features) throws SQLException, OSMToolException {
        if (features.length != 2) {
            throw new OSMToolException("number of features not equal to 2");
        }

        if (features[0].getProperties().containsKey(TABLE_NAME_PROPERTY) && features[1].getProperties().containsKey(TABLE_NAME_PROPERTY)) {
            if (features[0].getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getSourceTable()) && features[0].getProperties().get("table_type").equals("source")) {
                targetTableDao.replace(features[0], features[1]);
            } else if (features[1].getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getSourceTable()) && features[1].getProperties().get("table_type").equals("source")) {
                targetTableDao.replace(features[1], features[0]);
            } else throw new OSMToolException("Table 'from' deosn't exist");
        } else throw new OSMToolException("table name doesn't exist");

    }

    @Override
    public void delete(Feature[] features) throws SQLException, OSMToolException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getSourceTable()) || feature.getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getTargetTable())) {
                    targetTableDao.delete(feature);
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }

    }

    @Override
    public void update(Feature feature) throws SQLException {
        targetTableDao.update(feature);
    }
}
