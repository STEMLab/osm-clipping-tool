package io.github.stemlab.entity.mapper;

/**
 * Created by Azamat on 6/2/2017.
 */

import com.vividsolutions.jts.geom.Geometry;
import io.github.stemlab.entity.Feature;
import org.springframework.jdbc.core.RowMapper;
import org.wololo.jts2geojson.GeoJSONReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class KRFeatureMapper implements RowMapper {

    private static final String ID = "gid";
    private static final String TOPOLOGY_TYPE = "topology_type";
    private static final String GEOMETRY_FUNCTION_ST_ASGEOJSON = "st_asgeojson";
    private static final String TABLENAME = "tablename";

    public Feature mapRow(ResultSet resultSet, int i) throws SQLException {
        Feature feature = new Feature();
        feature.setId(resultSet.getLong(ID));
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(TOPOLOGY_TYPE, resultSet.getString(TOPOLOGY_TYPE));
        properties.put("name", resultSet.getString("name"));
        properties.put(TABLENAME, resultSet.getString(TABLENAME));
        properties.put("source", "kr");
        feature.setProperties(properties);

        GeoJSONReader reader = new GeoJSONReader();
        Geometry geometry = reader.read(resultSet.getString(GEOMETRY_FUNCTION_ST_ASGEOJSON));
        feature.setGeometry(geometry);

        return feature;
    }
}
