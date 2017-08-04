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

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Service
public class SpatialServiceImpl implements SpatialService {

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
            if (table.equals(database.getTableWrapper().getOsm())) {
                features.addAll(spatialDao.getOSMIntersectsWithTopologyType(envelope));
            } else if (table.equals(database.getTableWrapper().getOrigin())) {
                features.addAll(spatialDao.getKRIntersectsWithTopologyType(envelope));
            } else {
                throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + table);
            }
        }

        return features;
    }

    //temporal method
    @Override
    public List<Feature> getFeatures(String table) throws OSMToolException, SQLException {
        if (table.equals(database.getTableWrapper().getOrigin())) {
            return spatialDao.getUNFeatures();
        } else if (table.equals(database.getTableWrapper().getOsm())) {
            return spatialDao.getOSMFeatures();
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
    public void logAction(String ip, Long osm_id, Action action) throws SQLException {
        spatialDao.logAction(ip, osm_id, action);
    }

    public void addToOsmDataSet(Feature[] features) throws OSMToolException, SQLException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getOrigin())) {
                    spatialDao.addToOSM(feature);
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }
    }

    public void replaceObjects(Feature[] features) throws OSMToolException, SQLException {

        if (features.length != 2) {
            throw new OSMToolException("number of features not equal to 2");
        }

        if (features[0].getProperties().containsKey(TABLE_NAME_PROPERTY) && features[1].getProperties().containsKey(TABLE_NAME_PROPERTY)) {
            if (features[0].getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getOrigin())) {
                spatialDao.replaceObjects(features[0], features[1]);
            } else if (features[1].getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getOrigin())) {
                spatialDao.replaceObjects(features[1], features[0]);
            } else throw new OSMToolException("Table 'from' deosn't exist");
        } else throw new OSMToolException("table name doesn't exist");
    }

    public void deleteObjects(Feature[] features) throws OSMToolException, SQLException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getOrigin()) || feature.getProperties().get(TABLE_NAME_PROPERTY).equals(database.getTableWrapper().getOsm())) {
                    spatialDao.deleteObjects(feature);
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

    public void testConnection() throws SQLException {
        spatialDao.testConnection();
    }

    public List<String> getSchemas() throws SQLException {
        return spatialDao.getSchemas();
    }

    public List<String> getTables(String schema) throws SQLException {
        return spatialDao.getTables(schema);
    }

    public List<Column> getColumns(String schema, String table) throws SQLException {
        return spatialDao.getColumns(schema, table);
    }

    ;
}
