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

import static io.github.stemlab.service.impl.SpatialServiceImpl.*;

/**
 * Created by Azamat on 6/2/2017.
 */
@Repository
public class SpatialDaoImpl implements SpatialDao {

    public static String GEOM = "geom";
    public static String OSM_ID = "id";
    public static String KR_ID = "gid";
    public static String NAME = "name";
    private static String SRID = "3857";
    private static String OSM_SCHEMA_NAME = "converter_data";
    private static String KR_SCHEMA_NAME = "kr";
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Feature> getOSMIntersectsWithTopologyType(Envelope envelope, String table) {
        final String query = "select " + OSM_ID + ", " + NAME + ", '" + table + "' as tablename, st_asgeojson(" + GEOM + "), case when (ST_Within(" + GEOM + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'))) THEN 'crosses'\n" +
                "      when (ST_Crosses(" + GEOM + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'))) THEN 'within' " +
                "when (ST_Overlaps(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'overlaps' END AS topology_type\n" +
                " from " + OSM_SCHEMA_NAME + "." + table + " where added_to_kr = FALSE and \n" +
                "ST_Intersects(" + GEOM + ",ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'));";
        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(), envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new OSMFeatureMapper());
    }

    public List<Feature> getKRIntersectsWithTopologyType(Envelope envelope, String table) {
        final String query = "select " + KR_ID + ", " + NAME + ", '" + table + "' as tablename, st_asgeojson(ST_Transform (" + GEOM + ", " + SRID + ")), case when (ST_Within(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'crosses'\n" +
                "      when (ST_Crosses(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'within' " +
                "when (ST_Overlaps(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179))) THEN 'overlaps' END AS topology_type\n" +
                " from " + KR_SCHEMA_NAME + "." + table + " where is_del = FALSE AND\n" +
                "ST_Intersects(" + GEOM + ",ST_Transform(ST_SetSRID(ST_MakeEnvelope(?,?,?,?), '" + SRID + "'),5179));";
        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(), envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax(),
                envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new KRFeatureMapper());
    }

    public void addToKR(String from, String dest, Long id) {
        final String query = "insert into " + KR_SCHEMA_NAME + "." + dest + "(name, geom, osm)\n" +
                "select name,ST_Multi(ST_Transform(geom,5179)),true from " + OSM_SCHEMA_NAME + "." + from + " where " + OSM_ID + "= ?;";
        jdbcTemplate.update(query, id);
        if (jdbcTemplate.update(query, id) > 0) {
            setAddedToKR(from, id);
        }
    }

    public void setAddedToKR(String table, Long id) {
        final String query = "update " + OSM_SCHEMA_NAME + "." + table + " set added_to_kr = TRUE where " + OSM_ID + " = ?;";
        jdbcTemplate.update(query, id);
    }

    public Double getHausdorffDistance(Long krID, Long osmID) {
        final String query = "select ST_HausdorffDistance((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + KR_ROAD_NAME + " where " + KR_ID + " = ?),(select ST_Transform(" + GEOM + ",5179) from " + OSM_SCHEMA_NAME + "." + OSM_ROAD_NAME + " where " + OSM_ID + " = ?))";
        return jdbcTemplate.queryForObject(query, new Object[]{krID, osmID}, Double.class);
    }

    public Double getSurfaceDistance(Long krID, Long osmID) {
        final String query = "select (ST_Area(ST_Intersection((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + KR_BUILDING_NAME + " where " + KR_ID + " = ?),(select ST_Transform(" + GEOM + ",5179) from " + OSM_SCHEMA_NAME + "." + OSM_BUILDING_NAME + " where " + OSM_ID + " = ?)))\n" +
                "/ST_Area(ST_Union((select " + GEOM + " from " + KR_SCHEMA_NAME + "." + KR_BUILDING_NAME + " where " + KR_ID + " = ?),(select ST_Transform(" + GEOM + ",5179) from " + OSM_SCHEMA_NAME + "." + OSM_BUILDING_NAME + " where " + OSM_ID + " = ?)))) * 100";
        return jdbcTemplate.queryForObject(query, new Object[]{krID, osmID, krID, osmID}, Double.class);
    }

    public void replaceObjects(String tableTo, String tableFrom, Long idTo, Long idFrom) {
        final String query = "update " + KR_SCHEMA_NAME + "." + tableTo + " set " + GEOM + " = ST_Multi(ST_Transform((select " + GEOM + " from " + OSM_SCHEMA_NAME + "." + tableFrom + " where " + OSM_ID + " = ?),5179)) where " + KR_ID + " = ?";
        if (jdbcTemplate.update(query, idFrom, idTo) > 0) {
            setAddedToKR(tableFrom, idFrom);
        }
    }

    public void deleteObjects(String table, Long id) {
        final String query = "UPDATE " + KR_SCHEMA_NAME + "." + table + " SET is_del = true where " + KR_ID + " = ?";
        jdbcTemplate.update(query, id);
    }
}
