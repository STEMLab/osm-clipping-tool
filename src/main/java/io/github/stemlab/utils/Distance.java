package io.github.stemlab.utils;

import com.vividsolutions.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Created by Azamat on 7/11/2017.
 */
public class Distance {

    public static double hausdorff(Geometry a, Geometry b) {

        double distance = DiscreteHausdorffDistance.distance(a, b, 0.25D);
        double measure = new HausdorffSimilarityMeasure().measure(a, b) * 100;
        System.out.println("Distance: " +distance + " and matching is: " + measure);
        return measure;
    }

    public static double surface(Geometry a, Geometry b) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon polygon;
        Polygon polygon1;
        try {
            polygon = geometryFactory.createPolygon(a.getCoordinates());
            polygon1 = geometryFactory.createPolygon(b.getCoordinates());
        } catch (IllegalArgumentException exception) {
            System.out.println("Not closed geometry");
            return 0.0;
        }


        Geometry intersection = polygon.intersection(polygon1);
        Geometry union = polygon.union(polygon1);

        return (intersection.getArea() / union.getArea()) * 100;
    }
}
