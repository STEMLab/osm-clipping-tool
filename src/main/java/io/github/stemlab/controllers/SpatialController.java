package io.github.stemlab.controllers;

import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.FeatureCollection;
import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.exception.OSMToolException;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.service.TargetTableService;
import io.github.stemlab.service.log.ActionLogService;
import io.github.stemlab.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

/**
 * @author Bolat Azamat
 * @brief controller to work with all special operations
 */
@Controller
public class SpatialController {

    @Autowired
    SpatialService spatialService;

    @Autowired
    SessionStore sessionStore;

    @Autowired
    TargetTableService targetTableService;

    @Autowired
    ActionLogService actionLogService;

    /**
     * Get all intersected with bbox features;
     * This method also will log box view action @see {@link Action}
     *
     * @param envelope coordinates of bounding box (square) drawn on front-end
     * @return collection of intersected features
     * @throws OSMToolException
     * @throws SQLException
     * @see Envelope
     * @see SessionStore
     * @see FeatureCollection
     */
    @RequestMapping(value = "/intersects", method = RequestMethod.GET)
    public
    @ResponseBody
    FeatureCollection getIntersects(Envelope envelope) throws OSMToolException, SQLException {
        sessionStore.initialize();
        actionLogService.log(sessionStore.getIP(), null, Action.BOX_VIEW);
        return new FeatureCollection(spatialService.getIntersectionWithTopologyType(envelope));
    }

    /**
     * Get processed features (matched) @see {@link SpatialService#getProcessedFeatures()}
     *
     * @return collection of matched geometry features
     * @throws OSMToolException
     */
    @RequestMapping(value = "/processed_intersects", method = RequestMethod.GET)
    public
    @ResponseBody
    FeatureCollection getProcessedIntersects() throws OSMToolException {
        return new FeatureCollection(spatialService.getProcessedFeatures());
    }

    /**
     * Add objects from one table to another
     *
     * @param features
     * @return http status 200 on success
     * @throws OSMToolException
     * @throws SQLException
     * @see Feature
     */
    @RequestMapping(value = "/add_to_dataset", method = RequestMethod.POST)
    public ResponseEntity addToDataSet(@RequestBody Feature[] features) throws OSMToolException, SQLException {
        targetTableService.add(features);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Replace objects in another table
     *
     * @param features
     * @return http status 200 on success
     * @throws OSMToolException
     * @throws SQLException
     * @see Feature
     */
    @RequestMapping(value = "/replace", method = RequestMethod.POST)
    public ResponseEntity replaceInDataSet(@RequestBody Feature[] features) throws OSMToolException, SQLException {
        targetTableService.replace(features);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Update attributes of feature in table
     *
     * @param feature
     * @return https status 200 on success
     * @throws OSMToolException
     * @throws SQLException
     * @see Feature
     */
    @RequestMapping(value = "/update_feature", method = RequestMethod.POST)
    public ResponseEntity updateFeature(@RequestBody Feature feature) throws OSMToolException, SQLException {
        targetTableService.update(feature);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Delete objects from table
     *
     * @param features
     * @return http status 200 on success
     * @throws OSMToolException
     * @throws SQLException
     * @see Feature
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseEntity deleteInDataSet(@RequestBody Feature[] features) throws OSMToolException, SQLException {
        targetTableService.delete(features);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Get hausdorff distance between two features @see {@link SpatialService#getHausdorffDistance(Feature[])}
     *
     * @param features
     * @return distance in ratio
     * @throws OSMToolException
     */
    @RequestMapping(value = "/hausdorff_distance", method = RequestMethod.POST)
    public
    @ResponseBody
    Double hausdorffDistance(@RequestBody Feature[] features) throws OSMToolException {
        return spatialService.getHausdorffDistance(features);
    }

    /**
     * Get surface distance between two features @see {@link SpatialService#getSurfaceDistance(Feature[])}
     *
     * @param features
     * @return distance in ratio
     * @throws OSMToolException
     */
    @RequestMapping(value = "/surface_distance", method = RequestMethod.POST)
    public
    @ResponseBody
    Double surfaceDistance(@RequestBody Feature[] features) throws OSMToolException {
        return spatialService.getSurfaceDistance(features);
    }

    /**
     * Get all features from both tables
     *
     * @return collection of features
     * @throws Exception
     * @see Feature
     * @see FeatureCollection
     */
    @RequestMapping(value = "/features", method = RequestMethod.GET)
    public
    @ResponseBody
    FeatureCollection getFeatures() throws Exception {
        return new FeatureCollection(spatialService.getFeatures());
    }

}
