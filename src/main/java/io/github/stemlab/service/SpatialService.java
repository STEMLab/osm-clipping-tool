package io.github.stemlab.service;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;

import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
public interface SpatialService {
    List<Feature> getWithin(Envelope envelope);

    List<Feature> getCrosses(Envelope envelope);

    List<Feature> getintersects(Envelope envelope, String... tables);
}
