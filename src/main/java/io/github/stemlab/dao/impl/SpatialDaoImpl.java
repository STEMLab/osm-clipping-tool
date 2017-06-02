package io.github.stemlab.dao.impl;

import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.mapper.FeatureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static io.github.stemlab.entity.mapper.FeatureMapper.ID;

/**
 * Created by Azamat on 6/2/2017.
 */
@Repository
public class SpatialDaoImpl implements SpatialDao {

    private static String TABLE = "planet_osm_line";
    private static String GEOM = "way";
    private static String SRID = "3857";
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Feature> getWithin(Envelope envelope) {
        final String query = "select " + ID + ",st_asgeojson(" + GEOM + "), 'within' as spatial_type from " + TABLE + " where ST_Within(ST_SetSRID(" + GEOM + ", '" + SRID + "'),\n" +
                "        ST_SetSRID(ST_MakeEnvelope(\n" +
                "        ?, ?, ?, ?), '" + SRID + "'))";

        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new FeatureMapper());
    }

    public List<Feature> getCrosses(Envelope envelope) {
        final String query = "select " + ID + ",st_asgeojson(" + GEOM + "), 'crosses' as spatial_type from " + TABLE + " where ST_Crosses(ST_SetSRID(" + GEOM + ", '" + SRID + "'),\n" +
                "        ST_SetSRID(ST_MakeEnvelope(\n" +
                "        ?, ?, ?, ?), '" + SRID + "'))";
        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new FeatureMapper());
    }
}
