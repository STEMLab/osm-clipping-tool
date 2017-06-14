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

    @RequestMapping("/within")
    public
    @ResponseBody
    FeatureCollection getWithin(Envelope envelope) {
        return new FeatureCollection(spatialService.getWithin(envelope));
    }

    @RequestMapping("/crosses")
    public
    @ResponseBody
    FeatureCollection getCrosses(Envelope envelope) {
        return new FeatureCollection(spatialService.getCrosses(envelope));
    }

    @RequestMapping("/intersects")
    public
    @ResponseBody
    FeatureCollection getIntersects(Envelope envelope, @RequestParam(value = "tables[]") String[] tables) {
        return new FeatureCollection(spatialService.getintersects(envelope, tables));
    }
}
