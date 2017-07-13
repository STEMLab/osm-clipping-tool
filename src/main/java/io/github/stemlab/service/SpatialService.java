package io.github.stemlab.service;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.exception.OSMToolException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public interface SpatialService {
    List<Feature> getIntersectsWithTopology(Envelope envelope, String... tables) throws OSMToolException, SQLException;

    void addToOsmDataSet(Feature[] features) throws OSMToolException;

    void replaceObjects(Feature[] features) throws OSMToolException;

    void deleteObjects(Feature[] features) throws OSMToolException;

    Double getHausdorffDistance(Feature[] features) throws OSMToolException;

    Double getSurfaceDistance(Feature[] features) throws OSMToolException;

    List<Feature> getProcessedFeatures() throws OSMToolException;
}
