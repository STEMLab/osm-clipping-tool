package io.github.stemlab.session;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import io.github.stemlab.entity.Feature;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Created by Azamat on 7/13/2017.
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

    public void initialize() {
        this.lineTree = RTree.star().create();
        this.surfaceTree = RTree.star().create();
    }

    public void putToLineTree(Feature feature) {
        int size = feature.getGeometry().getCoordinates().length;
        lineTree = lineTree.add(feature, Geometries.line(feature.getGeometry().getCoordinates()[0].x, feature.getGeometry().getCoordinates()[0].y, feature.getGeometry().getCoordinates()[size - 1].x, feature.getGeometry().getCoordinates()[size - 1].y));
    }

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
