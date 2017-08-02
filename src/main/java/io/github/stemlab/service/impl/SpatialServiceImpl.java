package io.github.stemlab.service.impl;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Column;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.exception.OSMToolException;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import io.github.stemlab.session.SessionStore;
import io.github.stemlab.utils.Distance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Service
public class SpatialServiceImpl implements SpatialService {

    public static String OSM_BUILDING_NAME = "building";
    public static String KR_BUILDING_NAME = "roadl_50k";
    public static String KR_ROAD_NAME = "roadl_urban";
    public static String OSM_ROAD_NAME = "road_sa";
    private static String UNDEFINED_TABLE_EXCEPTION = "UNDEFINED TABLE:";
    private static double MAX_SURFACE_DISTANCE = 50.0;
    private static double MAX_HAUSDORFF_DISTANCE = 60.0;
    private static String TABLE_NAME_PROPERTY = "tablename";

    @Autowired
    SpatialDao spatialDao;

    @Autowired
    SessionStore sessionStore;
    @Autowired
    Database database;

    public List<Feature> getIntersectsWithTopology(Envelope envelope, String... tables) throws OSMToolException, SQLException {

        List<Feature> features = new LinkedList<>();


        for (String table : tables) {
            if (table.equals(OSM_BUILDING_NAME)) {
                features.addAll(spatialDao.getOSMIntersectsWithTopologyType(envelope, OSM_BUILDING_NAME));
            } else if (table.equals(OSM_ROAD_NAME)) {
                features.addAll(spatialDao.getOSMIntersectsWithTopologyType(envelope, OSM_ROAD_NAME));
            } else if (table.equals(KR_ROAD_NAME)) {
                features.addAll(spatialDao.getKRIntersectsWithTopologyType(envelope, KR_ROAD_NAME));
            } else if (table.equals(KR_BUILDING_NAME)) {
                features.addAll(spatialDao.getKRIntersectsWithTopologyType(envelope, KR_BUILDING_NAME));
            } else {
                throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + table);
            }
        }

        return features;
    }

    //temporal method
    @Override
    public List<Feature> getFeatures(String table) throws OSMToolException, SQLException {
        if (table.equals(KR_ROAD_NAME)) {
            return spatialDao.getUNFeatures(KR_ROAD_NAME);
        } else if (table.equals(OSM_ROAD_NAME)) {
            return spatialDao.getOSMFeatures(OSM_ROAD_NAME);
        } else if (table.equals(KR_BUILDING_NAME)) {
            return spatialDao.getUNFeatures(KR_BUILDING_NAME);
        } else {
            throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + table);
        }
    }

    public List<Feature> getProcessedFeatures() throws OSMToolException {

        List<Feature> listSurface = new LinkedList<Feature>();
        List<Feature> listLine = new LinkedList<Feature>();

        Instant startSurface = Instant.now();

        List<Entry<Feature, Geometry>> buildings = sessionStore.getSurfaceTree().entries().filter(entry -> entry.value().getProperties().get("source").equals("un")).toList().toBlocking().single();
        List<Entry<Feature, Geometry>> roads = sessionStore.getLineTree().entries().filter(entry -> entry.value().getProperties().get("source").equals("un")).toList().toBlocking().single();

        for (Entry<Feature, Geometry> feature : buildings) {
            double max = MAX_SURFACE_DISTANCE;
            Long chosenFeature = null;

            com.vividsolutions.jts.geom.Envelope g = feature.value().getGeometry().getEnvelopeInternal();
            Observable<Entry<Feature, Geometry>> entries =
                    sessionStore.getSurfaceTree().search(Geometries.rectangle(g.getMinX(), g.getMinY(), g.getMaxX(), g.getMaxY())).filter(entry -> !entry.value().getProperties().get("source").equals("un"));

            List<Entry<Feature, Geometry>> myList = entries.toList().toBlocking().single();

            for (Entry<Feature, Geometry> entry : myList) {
                double distance = Distance.surface(feature.value().getGeometry(), entry.value().getGeometry());
                System.out.println("Surface distance beetween : " + feature.value().getId() + " and " + entry.value().getId() + " = " + distance);
                if (distance > max) {
                    max = distance;
                    chosenFeature = entry.value().getId();
                }
            }

            if (chosenFeature != null) {
                feature.value().addProperty("candidate", String.valueOf(chosenFeature));
                feature.value().addProperty("candidateDistance", String.valueOf(max));
                listSurface.add(feature.value());
            }
        }


        Instant endSurface = Instant.now();

        System.out.println("Surface time : " + Duration.between(startSurface, endSurface));


        Comparator<Feature> matchDistanceComparator = Comparator.comparingDouble(o -> Double.parseDouble(o.getProperties().get("candidateDistance")));
        listSurface.sort(matchDistanceComparator.reversed());
        Instant startLine = Instant.now();
        for (Entry<Feature, Geometry> feature : roads) {
            com.vividsolutions.jts.geom.Envelope g = feature.value().getGeometry().getEnvelopeInternal();
            Observable<Entry<Feature, Geometry>> entries =
                    sessionStore.getLineTree().search(Geometries.rectangle(g.getMinX(), g.getMinY(), g.getMaxX(), g.getMaxY())).filter(entry -> !entry.value().getProperties().get("source").equals("un"));
            Long chosenFeature = null;
            double min = MAX_HAUSDORFF_DISTANCE;
            List<Entry<Feature, Geometry>> myList = entries.toList().toBlocking().single();

            for (Entry<Feature, Geometry> entry : myList) {


                double distance = Distance.hausdorff(feature.value().getGeometry(), entry.value().getGeometry());
                if (distance > min) {
                    min = distance;
                    chosenFeature = entry.value().getId();
                }
            }

            if (chosenFeature != null) {
                System.out.print("Match Beetween : " + feature.value().getId() + " and " + chosenFeature + ": " + String.valueOf(min));
                feature.value().addProperty("candidate", String.valueOf(chosenFeature));
                feature.value().addProperty("candidateDistance", String.valueOf(min));
                listLine.add(feature.value());
            }
        }

        Instant endLine = Instant.now();

        System.out.println("Line time : " + Duration.between(startLine, endLine));


        listLine.sort(matchDistanceComparator.reversed());

        List<Feature> list = new LinkedList<Feature>();
        list.addAll(listSurface);
        list.addAll(listLine);

        return list;
    }

    @Override
    public void logAction(String ip, Long osm_id, Action action) {
        spatialDao.logAction(ip, osm_id, action);
    }

    public void addToOsmDataSet(Feature[] features) throws OSMToolException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(KR_BUILDING_NAME)) {
                    //temp
                    spatialDao.addToOSM(feature.getProperties().get(TABLE_NAME_PROPERTY), OSM_ROAD_NAME, feature.getId());
                } else if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(KR_ROAD_NAME)) {
                    spatialDao.addToOSM(feature.getProperties().get(TABLE_NAME_PROPERTY), OSM_ROAD_NAME, feature.getId());
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }
    }

    public void replaceObjects(Feature[] features) throws OSMToolException {
        Feature featureTo = features[0];
        Feature featureFrom = features[1];
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(KR_BUILDING_NAME)) {
                    featureFrom = feature;
                } else if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(OSM_BUILDING_NAME)) {
                    featureTo = feature;
                } else if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(KR_ROAD_NAME)) {
                    featureFrom = feature;
                } else if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(OSM_ROAD_NAME)) {
                    featureTo = feature;
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }
        spatialDao.replaceObjects(featureTo.getProperties().get(TABLE_NAME_PROPERTY), featureFrom.getProperties().get(TABLE_NAME_PROPERTY), featureTo.getId(), featureFrom.getId());
    }

    public void deleteObjects(Feature[] features) throws OSMToolException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(OSM_ROAD_NAME)) {
                    spatialDao.deleteObjects(OSM_ROAD_NAME, feature.getId());
                } else if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(OSM_BUILDING_NAME)) {
                    spatialDao.deleteObjects(OSM_BUILDING_NAME, feature.getId());
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }
    }

    public Double getHausdorffDistance(Feature... features) throws OSMToolException {
        if (features.length == 2) {
            if (features[0].getProperties().containsKey(TABLE_NAME_PROPERTY) && features[1].getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                return Distance.hausdorff(features[1].getGeometry(), features[0].getGeometry());
            } else {
                throw new OSMToolException("NO KEY TABLENAME");
            }
        } else {
            throw new OSMToolException("Features size more than 2: " + features.length);
        }
    }

    public Double getSurfaceDistance(Feature... features) throws OSMToolException {
        if (features.length == 2) {
            if (features[0].getProperties().containsKey(TABLE_NAME_PROPERTY) && features[1].getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                return Distance.surface(features[0].getGeometry(), features[1].getGeometry());
            } else {
                throw new OSMToolException("NO KEY TABLENAME");
            }
        } else {
            throw new OSMToolException("Features size more than 2: " + features.length);
        }
    }

    public void testConnection() throws ClassNotFoundException, SQLException {

        Class.forName(database.getDriver());

        Connection conn = DriverManager.getConnection(database.getConnection(), database.getUser(), database.getPassword());

        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT true as test");
        } finally {
            conn.close();
        }

    }

    public List<String> getSchemas() throws ClassNotFoundException, SQLException {

        List<String> schema = new ArrayList<>();

        Class.forName(database.getDriver());

        Connection conn = DriverManager.getConnection(database.getConnection(), database.getUser(), database.getPassword());

        DatabaseMetaData meta = conn.getMetaData();
        ResultSet schemas = meta.getSchemas();
        while (schemas.next()) {
            String tableSchema = schemas.getString(1); //"TABLE_CATALOG"
            schema.add(tableSchema);
        }

        conn.close();

        return schema;
    }

    public List<String> getTables(String schema) throws ClassNotFoundException, SQLException {

        Class.forName(database.getDriver());

        List<String> table = new ArrayList<>();

        Connection conn = DriverManager.getConnection(database.getConnection(), database.getUser(), database.getPassword());

        DatabaseMetaData meta = conn.getMetaData();
        ResultSet tables = meta.getTables(null, schema, null, new String[]{"TABLE"});
        while (tables.next()) {
            table.add(tables.getString("TABLE_NAME"));
        }

        conn.close();

        return table;
    }

    public List<Column> getColumns(String schema, String table) throws ClassNotFoundException, SQLException {

        Class.forName(database.getDriver());

        List<Column> columns = new ArrayList<>();

        Connection conn = DriverManager.getConnection(database.getConnection(), database.getUser(), database.getPassword());

        DatabaseMetaData meta = conn.getMetaData();
        ResultSet resultSet = meta.getColumns(null, schema, table, null);
        while (resultSet.next()) {
            String name = resultSet.getString("COLUMN_NAME");
            String type = resultSet.getString("TYPE_NAME");
            int size = resultSet.getInt("COLUMN_SIZE");
            columns.add(new Column(name, type, size));
            System.out.println("Column name: [" + name + "]; type: [" + type + "]; size: [" + size + "]");
        }

        conn.close();

        return columns;
    }


}
