package io.github.stemlab.controllers;

import io.github.stemlab.entity.enums.Action;
import io.github.stemlab.service.SpatialService;
import io.github.stemlab.session.Database;
import io.github.stemlab.session.SessionStore;
import io.github.stemlab.utils.IPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * Created by Azamat on 5/24/2017.
 */
@Controller
public class HomeController {

    @Autowired
    SpatialService spatialService;

    @Autowired
    SessionStore sessionStore;

    @Autowired
    Database database;


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(HttpServletRequest request) throws SQLException {
        sessionStore.setIP(IPUtil.getUserIpAddress(request));
        /*spatialService.logAction(sessionStore.getIP(), null, Action.VIEW);*/
        return "index";
    }

}
