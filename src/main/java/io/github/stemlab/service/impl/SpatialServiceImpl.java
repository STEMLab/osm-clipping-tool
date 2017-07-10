package io.github.stemlab.service.impl;

import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.exception.OSMToolException;
import io.github.stemlab.service.SpatialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Service
public class SpatialServiceImpl implements SpatialService {

    public static String OSM_BUILDING_NAME = "building";
    public static String KR_BUILDING_NAME = "building_kr";
    public static String KR_ROAD_NAME = "road_kr";
    public static String OSM_ROAD_NAME = "road";
    private static String UNDEFINED_TABLE_EXCEPTION = "UNDEFINED TABLE:";
    private static double MAX_SURFACE_DISTANCE = 0.0;
    private static double MAX_HAUSDORFF_DISTANCE = 15.0;
    private static String TABLE_NAME_PROPERTY = "tablename";
    @Autowired
    SpatialDao spatialDao;
    List<Feature> OSMBuildingFeatures;
    List<Feature> KRBuildingFeatures;
    List<Feature> KRRoadFeatures;
    List<Feature> OSMRoadFeatures;

    public List<Feature> getIntersectsWithTopology(Envelope envelope, String... tables) throws OSMToolException {


        OSMBuildingFeatures = new LinkedList<Feature>();
        KRBuildingFeatures = new LinkedList<Feature>();

        KRRoadFeatures = new LinkedList<Feature>();
        OSMRoadFeatures = new LinkedList<Feature>();

        List<Feature> features = new LinkedList<Feature>();

        for (String table : tables) {
            if (table.equals(OSM_BUILDING_NAME)) {
                OSMBuildingFeatures = spatialDao.getOSMIntersectsWithTopologyType(envelope, OSM_BUILDING_NAME);
                features.addAll(OSMBuildingFeatures);
            } else if (table.equals(OSM_ROAD_NAME)) {
                OSMRoadFeatures = spatialDao.getOSMIntersectsWithTopologyType(envelope, OSM_ROAD_NAME);
                features.addAll(OSMRoadFeatures);
            } else if (table.equals(KR_ROAD_NAME)) {
                KRRoadFeatures = spatialDao.getKRIntersectsWithTopologyType(envelope, KR_ROAD_NAME);
                features.addAll(KRRoadFeatures);
            } else if (table.equals(KR_BUILDING_NAME)) {
                KRBuildingFeatures = spatialDao.getKRIntersectsWithTopologyType(envelope, KR_BUILDING_NAME);
                features.addAll(KRBuildingFeatures);
            } else {
                throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + table);
            }
        }
        return features;
    }

    public List<Feature> getProcessedFeatures() throws OSMToolException {

        List<Feature> listSurface = new LinkedList<Feature>();
        List<Feature> listLine = new LinkedList<Feature>();

        for (Feature feature : KRBuildingFeatures) {
            double max = MAX_SURFACE_DISTANCE;
            Feature chosenFeature = null;
            for (Feature secondFeature : OSMBuildingFeatures) {
                double distance = getSurfaceDistance(feature, secondFeature);
                if (distance > max) {
                    max = distance;
                    chosenFeature = secondFeature;
                }
            }
            if (chosenFeature != null) {
                feature.addProperty("candidate", chosenFeature.getId().toString());
                feature.addProperty("candidateDistance", getSurfaceDistance(feature, chosenFeature).toString());
                listSurface.add(feature);
            }
        }


        Comparator<Feature> matchDistanceComparator = Comparator.comparingDouble(o -> Double.parseDouble(o.getProperties().get("candidateDistance")));
            listSurface.sort(matchDistanceComparator.reversed());

        for (Feature feature : KRRoadFeatures) {
            double min = MAX_HAUSDORFF_DISTANCE;
            Feature chosenFeature = null;
            for (Feature secondFeature : OSMRoadFeatures) {
                double distance = getHausdorffDistance(feature, secondFeature);
                ;
                if (distance < min) {
                    min = distance;
                    chosenFeature = secondFeature;
                }
            }
            if (chosenFeature != null) {
                feature.addProperty("candidate", chosenFeature.getId().toString());
                feature.addProperty("candidateDistance", getHausdorffDistance(feature, chosenFeature).toString());
                listLine.add(feature);
            }
        }

        listLine.sort(matchDistanceComparator);

        List<Feature> list = new LinkedList<Feature>();
        list.addAll(listSurface);
        list.addAll(listLine);

        return list;
    }

    public void addToOsmDataSet(Feature[] features) throws OSMToolException {
        for (Feature feature : features) {
            if (feature.getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(KR_BUILDING_NAME)) {
                    spatialDao.addToOSM(KR_BUILDING_NAME, OSM_BUILDING_NAME, feature.getId());
                } else if (feature.getProperties().get(TABLE_NAME_PROPERTY).equals(KR_ROAD_NAME)) {
                    spatialDao.addToOSM(KR_ROAD_NAME, OSM_ROAD_NAME, feature.getId());
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
                    spatialDao.deleteObjects(OSM_BUILDING_NAME,feature.getId());
                } else {
                    throw new OSMToolException(UNDEFINED_TABLE_EXCEPTION + feature.getProperties().get(TABLE_NAME_PROPERTY));
                }
            }
        }
    }

    public Double getHausdorffDistance(Feature... features) throws OSMToolException {
        if (features.length == 2) {
            if (features[0].getProperties().containsKey(TABLE_NAME_PROPERTY) && features[1].getProperties().containsKey(TABLE_NAME_PROPERTY)) {
                if (features[0].getProperties().get(TABLE_NAME_PROPERTY).equals(OSM_ROAD_NAME)) {
                    return spatialDao.getHausdorffDistance(features[1].getId(), features[0].getId());
                } else {
                    return spatialDao.getHausdorffDistance(features[0].getId(), features[1].getId());
                }
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
                if (features[0].getProperties().get(TABLE_NAME_PROPERTY).equals(OSM_BUILDING_NAME)) {
                    return spatialDao.getSurfaceDistance(features[1].getId(), features[0].getId());
                } else {
                    return spatialDao.getSurfaceDistance(features[0].getId(), features[1].getId());
                }
            } else {
                throw new OSMToolException("NO KEY TABLENAME");
            }
        } else {
            throw new OSMToolException("Features size more than 2: " + features.length);
        }
    }
}
