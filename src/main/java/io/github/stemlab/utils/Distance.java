package io.github.stemlab.utils;

import com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Bolat Azamat
 *         @brief Utility class to calculate hausdorff and surface distance
 * @see com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure
 */
public class Distance {

    /**
     * @param a
     * @param b
     * @return calculated measure of geometry A and B
     * @see Geometry
     */
    public static double hausdorff(Geometry a, Geometry b) {

        /*double distance = DiscreteHausdorffDistance.distance(a, b, 0.25D);*/
        double measure = new HausdorffSimilarityMeasure().measure(a, b) * 100;
        return measure;
    }

    /**
     * Calculating surface distance. Ratio of A and B intersection to it's union
     *
     * @param a
     * @param b
     * @return A intersection B / A union B
     */
    public static double surface(Geometry a, Geometry b) {

        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon polygon;
        Polygon polygon1;
        try {
            polygon = geometryFactory.createPolygon(a.getCoordinates());
            polygon1 = geometryFactory.createPolygon(b.getCoordinates());
        } catch (IllegalArgumentException exception) {
            //Not closed geometry
            return 0.0;
        }


        Geometry intersection = polygon.intersection(polygon1);
        Geometry union = polygon.union(polygon1);

        return (intersection.getArea() / union.getArea()) * 100;
    }
}
