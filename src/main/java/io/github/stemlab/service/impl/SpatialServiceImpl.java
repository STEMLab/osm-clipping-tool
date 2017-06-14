package io.github.stemlab.service.impl;

import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.service.SpatialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Service
public class SpatialServiceImpl implements SpatialService {

    @Autowired
    SpatialDao spatialDao;

    public List<Feature> getWithin(Envelope envelope) {
        return spatialDao.getWithin(envelope);
    }

    public List<Feature> getCrosses(Envelope envelope) {
        return spatialDao.getCrosses(envelope);
    }

    public List<Feature> getintersects(Envelope envelope, String... tables) {
        return spatialDao.getintersects(envelope,tables);
    }
}
