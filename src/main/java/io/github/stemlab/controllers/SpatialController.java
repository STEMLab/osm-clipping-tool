package io.github.stemlab.controllers;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.FeatureCollection;
import io.github.stemlab.service.SpatialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Azamat on 6/2/2017.
 */
@Controller
public class SpatialController {

    @Autowired
    SpatialService spatialService;

    @RequestMapping("/intersects")
    public
    @ResponseBody
    FeatureCollection getIntersects(Envelope envelope, @RequestParam(value = "tables[]") String[] tables) {
        return new FeatureCollection(spatialService.getintersectsWithTopologyType(envelope, tables));
    }
}
