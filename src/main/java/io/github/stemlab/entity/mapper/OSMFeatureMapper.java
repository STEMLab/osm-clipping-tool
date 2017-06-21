package io.github.stemlab.entity.mapper;

import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.Geometry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static io.github.stemlab.utils.JsonUtils.fromJson;

/**
 * Created by Azamat on 6/21/2017.
 */
public class OSMFeatureMapper implements RowMapper {

    private static final String ID = "id";
    private static final String TOPOLOGY_TYPE = "topology_type";
    private static final String GEOMETRY_FUNCTION_ST_ASGEOJSON = "st_asgeojson";

    public Feature mapRow(ResultSet resultSet, int i) throws SQLException {
        Feature feature = new Feature();
        feature.setId(resultSet.getLong(ID));
        feature.setGeometry(fromJson(resultSet.getString(GEOMETRY_FUNCTION_ST_ASGEOJSON), Geometry.class));
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(TOPOLOGY_TYPE, resultSet.getString(TOPOLOGY_TYPE));
        properties.put("name", resultSet.getString("name"));
        properties.put("source", "osm");
        feature.setProperties(properties);
        return feature;
    }
}
