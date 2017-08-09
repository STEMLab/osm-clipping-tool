package io.github.stemlab.controllers;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.FeatureCollection;
import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.exception.OSMToolException;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

/**
 * Created by Azamat on 6/2/2017.
 */
@Controller
public class SpatialController {

    @Autowired
    SpatialService spatialService;

    @Autowired
    SessionStore sessionStore;

    @RequestMapping(value = "/intersects", method = RequestMethod.GET)
    public
    @ResponseBody
    FeatureCollection getIntersects(Envelope envelope) throws Exception {
        sessionStore.initialize();
        spatialService.logAction(sessionStore.getIP(), null, Action.BOX_VIEW);
        return new FeatureCollection(spatialService.getIntersectsWithTopology(envelope));
    }

    @RequestMapping(value = "/intersectsProcess", method = RequestMethod.GET)
    public
    @ResponseBody
    FeatureCollection getIntersectsProcess() throws Exception {
        return new FeatureCollection(spatialService.getProcessedFeatures());
    }

    @RequestMapping(value = "/addToOsmDataSet", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void addOsmToDataSet(@RequestBody Feature[] features) throws OSMToolException, SQLException {
        spatialService.addToOsmDataSet(features);
    }

    @RequestMapping(value = "/replace", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void replaceInDataSet(@RequestBody Feature[] features) throws OSMToolException, SQLException {
        spatialService.replaceObjects(features);
    }

    @RequestMapping(value = "/updateFeature", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void updateFeature(@RequestBody Feature feature) throws OSMToolException, SQLException {
        spatialService.updateFeature(feature);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteInDataSet(@RequestBody Feature[] features) throws OSMToolException, SQLException {
        spatialService.deleteObjects(features);
    }

    @RequestMapping(value = "/hausdorffDistance", method = RequestMethod.POST)
    public
    @ResponseBody
    Double hausdorffDistance(@RequestBody Feature[] features) throws Exception {
        return spatialService.getHausdorffDistance(features);
    }

    @RequestMapping(value = "/surfaceDistance", method = RequestMethod.POST)
    public
    @ResponseBody
    Double surfaceDistance(@RequestBody Feature[] features) throws Exception {
        return spatialService.getSurfaceDistance(features);
    }

    @RequestMapping(value = "/features", method = RequestMethod.GET)
    public
    @ResponseBody
    FeatureCollection getFeatures() throws Exception {
        return new FeatureCollection(spatialService.getFeatures());
    }

}
