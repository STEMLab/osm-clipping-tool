package io.github.stemlab.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Util for getting user IP address.
 *
 * @author Bolat Azamat
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
