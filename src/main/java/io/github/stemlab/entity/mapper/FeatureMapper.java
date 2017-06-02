package io.github.stemlab.entity.mapper;

/**
 * Created by Azamat on 6/2/2017.
 */

import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.Geometry;
import io.github.stemlab.entity.enums.SpatialType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static io.github.stemlab.utils.JsonUtils.fromJson;

public class FeatureMapper implements RowMapper {

    public static final String ID = "osm_id";
    public static final String SPATIAL_TYPE = "spatial_type";
    public static final String GEOMETRY_FUNCTION_ST_ASGEOJSON = "st_asgeojson";

    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        Feature feature = new Feature();
        feature.setId(resultSet.getLong(ID));
        feature.setGeometry(fromJson(resultSet.getString(GEOMETRY_FUNCTION_ST_ASGEOJSON), Geometry.class));
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("spatialType", SpatialType.valueOf(resultSet.getString(SPATIAL_TYPE).toUpperCase()).toString());
        feature.setProperties(properties);
        return feature;
    }
}
