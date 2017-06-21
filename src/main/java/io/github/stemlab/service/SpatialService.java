package io.github.stemlab.service;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;

import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public interface SpatialService {
    List<Feature> getintersectsWithTopologyType(Envelope envelope, String... tables);
}
