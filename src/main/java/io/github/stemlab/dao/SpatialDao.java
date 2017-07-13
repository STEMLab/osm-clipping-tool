package io.github.stemlab.dao;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */

public interface SpatialDao {
    List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope, String table) throws SQLException;

    List<Feature> getKRIntersectsWithTopologyType(Envelope envelope, String table) throws SQLException;

    void addToOSM(String from, String dest, Long id);

    Double getHausdorffDistance(Long krID, Long osmID);

    Double getSurfaceDistance(Long krID, Long osmID);

    void replaceObjects(String tableTo, String tableFrom, Long idTo, Long idFrom);

    void deleteObjects(String table, Long id);
}
