package io.github.stemlab.session;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import io.github.stemlab.entity.Feature;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @brief In memory indexing using Rtree
 * <p>
 * <p> Current implementation support work with lines and surfaces.
 * After bbox intersection search, all data saved in session,
 * From where it can be extracted to process using Hausdorff or Surface Distance @see {@link io.github.stemlab.utils.Distance}
 * Session also stores user IP
 * </p>
 *
 * @author Bolat Azamat
 * @see RTree
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionStore {

    RTree<Feature, Geometry> lineTree;
    RTree<Feature, Geometry> surfaceTree;

    String IP;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    /**
     * Initialize trees
     */
    public void initialize() {
        this.lineTree = RTree.star().create();
        this.surfaceTree = RTree.star().create();
    }

    /**
     * Put lines to tree
     *
     * @param feature
     * @see Feature
     */
    public void putToLineTree(Feature feature) {
        int size = feature.getGeometry().getCoordinates().length;
        lineTree = lineTree.add(feature, Geometries.line(feature.getGeometry().getCoordinates()[0].x, feature.getGeometry().getCoordinates()[0].y, feature.getGeometry().getCoordinates()[size - 1].x, feature.getGeometry().getCoordinates()[size - 1].y));
    }

    /**
     * Put surface to tree
     *
     * @param feature
     * @see Feature
     */
    public void putToSurfaceTree(Feature feature) {
        surfaceTree = surfaceTree.add(feature, Geometries.rectangle(feature.getGeometry().getEnvelopeInternal().getMinX(), feature.getGeometry().getEnvelopeInternal().getMinY(), feature.getGeometry().getEnvelopeInternal().getMaxX(), feature.getGeometry().getEnvelopeInternal().getMaxY()));
    }

    public RTree<Feature, Geometry> getLineTree() {
        return lineTree;
    }

    public void setLineTree(RTree<Feature, Geometry> lineTree) {
        this.lineTree = lineTree;
    }

    public RTree<Feature, Geometry> getSurfaceTree() {
        return surfaceTree;
    }

    public void setSurfaceTree(RTree<Feature, Geometry> surfaceTree) {
        this.surfaceTree = surfaceTree;
    }
}
