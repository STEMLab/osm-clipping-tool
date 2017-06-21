package io.github.stemlab.dao.impl;

import io.github.stemlab.dao.SpatialDao;
import io.github.stemlab.entity.Envelope;
import io.github.stemlab.entity.Feature;
import io.github.stemlab.entity.mapper.KRFeatureMapper;
import io.github.stemlab.entity.mapper.OSMFeatureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by Azamat on 6/2/2017.
 */
@Repository
public class SpatialDaoImpl implements SpatialDao {

    public static String GEOM = "geom";
    public static String OSM_ID = "id";
    public static String OSM_NAME = "name";
    private static String TABLE = "kz.lakes";
    private static String SRID = "3857";
    private static String SCHEMA_NAME = "converter_data";
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope, String table) {
        final String query = "select id, name, st_asgeojson(geom), case when (ST_Within(geom,ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'))) THEN 'crosses'\n" +
                "      when (ST_Crosses(geom,ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'))) THEN 'within' " +
                "when (ST_Overlaps(geom,ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'overlaps' END AS topology_type\n" +
                " from converter_data." + table + " where \n" +
                "ST_Intersects(geom,ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'));";
        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(), envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new OSMFeatureMapper());
    }

    public List<Feature> getKRIntersectsWithTopologyType(Envelope envelope, String table) {
        final String query = "select gid, name, st_asgeojson(ST_Transform (geom, " + SRID + ")), case when (ST_Within(geom,ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'crosses'\n" +
                "      when (ST_Crosses(geom,ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'within' " +
                "when (ST_Overlaps(geom,ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'overlaps' END AS topology_type\n" +
                " from kr." + table + " where \n" +
                "ST_Intersects(geom,ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179));";
        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(), envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new KRFeatureMapper());
    }
}
