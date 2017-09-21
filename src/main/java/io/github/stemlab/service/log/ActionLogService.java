package io.github.stemlab.service.log;

import io.github.stemlab.entity.enums.Action;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Created by Azamat on 9/21/2017.
 */
@Service
public class ActionLogService {

    private static final Logger logger = Logger.getLogger(ActionLogService.class);

    public void log(String ip, Long osm_id, Action action) {
        logger.info("User from IP " + ip + "; ACTION: " + action + "; OBJECT_ID: " + osm_id);
    }
}
