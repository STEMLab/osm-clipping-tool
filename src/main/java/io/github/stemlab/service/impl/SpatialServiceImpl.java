package io.github.stemlab.service.impl;

import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.service.SpatialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Service
public class SpatialServiceImpl implements SpatialService {

    @Autowired
    SpatialDao spatialDao;

    public List<Feature> getintersectsWithTopologyType(Envelope envelope, String... tables) {
        List<Feature> features = new LinkedList<Feature>();
        for (String table : tables) {
            if (table.equals("building")) {
                features.addAll(spatialDao.getOSMIntersectsWithTopologyType(envelope, "building"));
            } else if (table.equals("road")) {
                features.addAll(spatialDao.getOSMIntersectsWithTopologyType(envelope, "road"));
            } else if (table.equals("road_kr")) {
                features.addAll(spatialDao.getKRIntersectsWithTopologyType(envelope, "road_kr"));
            } else if (table.equals("building_kr")) {
                features.addAll(spatialDao.getKRIntersectsWithTopologyType(envelope, "building_kr"));
            } else {
                System.out.println("undefined table");
            }
        }
        return features;
    }
}
