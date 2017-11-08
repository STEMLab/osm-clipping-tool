package io.github.stemlab.controllers.proxy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @brief Proxy controller to avoid browser security issues on sending data to back end
 *
 * @author Bolat Azamat.
 */
@Controller
public class ProxyController {
    @RequestMapping(value = "proxy", method = {RequestMethod.GET, RequestMethod.POST})
    public void makeProxy(@RequestParam String url, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (url == null || url.trim().length() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        boolean doPost = request.getMethod().equalsIgnoreCase("POST");
        URL urlt = new URL(url);
        HttpURLConnection http = (HttpURLConnection) urlt.openConnection();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            if (!key.equalsIgnoreCase("Host")) {
                http.setRequestProperty(key, request.getHeader(key));
            }
        }

        http.setDoInput(true);
        http.setDoOutput(doPost);

        byte[] buffer = new byte[8192];
        int read = -1;

        if (doPost) {
            OutputStream os = http.getOutputStream();
            ServletInputStream sis = request.getInputStream();
            while ((read = sis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.close();
        }

        InputStream is = http.getInputStream();
        response.setStatus(http.getResponseCode());

        Map headerKeys = http.getHeaderFields();
        Set keySet = headerKeys.keySet();
        Iterator iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = http.getHeaderField(key);
            if (key != null && value != null) {
                response.setHeader(key, value);
            }
        }

        ServletOutputStream sos = response.getOutputStream();
        response.resetBuffer();
        while ((read = is.read(buffer)) != -1) {
            sos.write(buffer, 0, read);
        }
        response.flushBuffer();
        sos.close();
    }
}
