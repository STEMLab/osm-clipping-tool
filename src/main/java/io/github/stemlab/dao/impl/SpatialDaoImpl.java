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

    private static String TABLE = "kz.lakes";
    public static String GEOM = "geom";
    private static String SRID = "3857";
    private static String SCHEMA_NAME = "converter_data";
    public static String OSM_ID = "id";
    public static String OSM_NAME = "name";

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
        final String query = "select " + ID + ",st_asgeojson(" + GEOM + "), 'crosses' as spatial_type from " + TABLE + " where ST_Overlaps(ST_SetSRID(" + GEOM + ", '" + SRID + "'),\n" +
                "        ST_SetSRID(ST_MakeEnvelope(\n" +
                "        ?, ?, ?, ?), '" + SRID + "'))";
        return jdbcTemplate.query(query, new Object[]{envelope.getxMin(), envelope.getyMin(), envelope.getxMax(), envelope.getyMax()}, new FeatureMapper());
    }

    public List<Feature> getintersects(Envelope envelope, String... tables) {
        String[] sqls = new String[tables.length];
        for (int i=0; i<tables.length;i++) {
            sqls[i] = "select " + OSM_ID + "," + OSM_NAME+ ",'"+tables[i]+"' as tablename, st_asgeojson(" + GEOM + ") from "+ SCHEMA_NAME +"."+ tables[i];
        }

        String query = "";
        for (int i=0; i<sqls.length;i++){
            if(i!=sqls.length-1){
                query += sqls[i] + " UNION ";
            }else{
                query += sqls[i];
            }
        }

        String clause = " where ST_Intersects(" + GEOM + ",\n" +
                "        ST_SetSRID(ST_MakeEnvelope(\n" +
                envelope.getxMin()+", "+envelope.getyMin()+", "+envelope.getxMax()+", "+envelope.getyMax()+"), '" + SRID + "'))";
        return jdbcTemplate.query(query+clause, new FeatureMapper());
    }
}
