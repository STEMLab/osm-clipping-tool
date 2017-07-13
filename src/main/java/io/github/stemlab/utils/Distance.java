package io.github.stemlab.utils;

import com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Created by Azamat on 7/11/2017.
 */
public class Distance {

    public static double hausdorff(Geometry a, Geometry b) {
        return new HausdorffSimilarityMeasure().measure(a, b) * 100;
    }

    public static double surface(Geometry a, Geometry b) {

        GeometryFactory geometryFactory = new GeometryFactory();

        Polygon polygon = geometryFactory.createPolygon(a.getCoordinates());
        Polygon polygon1 = geometryFactory.createPolygon(b.getCoordinates());

        Geometry intersection = polygon.intersection(polygon1);
        Geometry union = polygon.union(polygon1);

        return (intersection.getArea() / union.getArea()) * 100;
    }
}
