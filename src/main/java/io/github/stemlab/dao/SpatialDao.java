package io.github.stemlab.dao;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;

import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */

public interface SpatialDao {
    List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope, String table);

    List<Feature> getKRIntersectsWithTopologyType(Envelope envelope, String table);

    void addToKR(String from, String dest, Long id);

    Double getHausdorffDistance(Long krID, Long osmID);

    Double getSurfaceDistance(Long krID, Long osmID);
}
