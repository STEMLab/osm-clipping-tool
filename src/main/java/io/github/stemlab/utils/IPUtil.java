package io.github.stemlab.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Azamat on 7/21/2017.
 */
public class IPUtil {
    public static String getUserIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
