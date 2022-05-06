/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.web.util;

import org.apache.http.client.fluent.Request;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Mario A. Serrano Leones
 */
public class HttpUtils {

    public static final String DEVICE_TABLET = "tablet";
    public static final String DEVICE_SMARTPHONE = "smartphone";
    public static final String DEVICE_SCREEN = "screen";

    private static final String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private static final Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");
    /**
     * System property name to setup default server path. Useful when app is running in none servlet context
     */
    public static final String DEFAULT_SERVER_PATH = "defaultServerPath";


    /**
     * Execute a plain simple http get request
     *
     * @param url
     * @return
     */
    public static String executeHttpRequest(String url) throws IOException {
        return Request.Get(url).execute().returnContent().asString();
    }

    /**
     * Execute a GET request with headers and params
     *
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static String executeHttpRequest(String url, Map<String, String> headers, Map<String, Object> params) throws IOException {
        Request request = Request.Get(url + "?" + formatRequestParams(params));
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(request::addHeader);
        }

        return request.execute().returnContent().asString();
    }


    public static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes requestAttrb = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return requestAttrb.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static String getSessionId() {
        ServletRequestAttributes requestAttrb = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttrb.getSessionId();
    }

    public static String getClientIp() {
        return getIpFromRequest(getCurrentRequest());
    }

    public static String getBrowser() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader("user-agent");
    }

    public static String longToIpV4(long longIp) {
        int octet3 = (int) ((longIp >> 24) % 256);
        int octet2 = (int) ((longIp >> 16) % 256);
        int octet1 = (int) ((longIp >> 8) % 256);
        int octet0 = (int) ((longIp) % 256);
        return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
    }

    public static long ipV4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16)
                + (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
    }

    public static boolean isIPv4Private(String ip) {
        long longIp = ipV4ToLong(ip);
        return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255"))
                || (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255"))
                || longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
    }

    public static boolean isIPv4Valid(String ip) {
        return pattern.matcher(ip).matches();
    }

    public static String getIpFromRequest(HttpServletRequest request) {
        String ip = "0.0.0.0";
        if (request != null) {
            boolean found = false;
            if ((ip = request.getHeader("x-forwarded-for")) != null) {
                StringTokenizer tokenizer = new StringTokenizer(ip, ",");
                while (tokenizer.hasMoreTokens()) {
                    ip = tokenizer.nextToken().trim();
                    if (isIPv4Valid(ip) && !isIPv4Private(ip)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }

    public static UserAgentInfo getUserAgentInfo() {

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return new UserAgentInfo(request);
        } else {
            return new UserAgentInfo("", "");
        }

    }

    public static String detectDevice() {
        UserAgentInfo userAgentInfo = getUserAgentInfo();

        String device = null;
        if (userAgentInfo.detectTierTablet()) {
            device = DEVICE_TABLET;
        } else if (userAgentInfo.detectSmartphone()) {
            device = DEVICE_SMARTPHONE;
        } else {
            device = DEVICE_SCREEN;
        }

        return device;
    }

    public static boolean isSmartphone() {
        return detectDevice().equals(DEVICE_SMARTPHONE);
    }

    public static boolean isTablet() {
        return detectDevice().equals(DEVICE_TABLET);
    }

    /**
     * Return the server path including, scheme, servername, port and context
     * path using Current Request. If not current request {@link HttpUtils}.DEFAULT_SERVER_PATH system property is used.
     *
     * @return server path
     */
    public static String getServerPath() {
        HttpServletRequest request = getCurrentRequest();
        String resultPath = "";
        if (request != null) {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            String serverPortName = ":" + serverPort;
            if (serverPort == 80) { //http
                serverPortName = "";
            } else if (serverPort == 443) { //https
                serverPortName = "";
                scheme = "https";
            }

            resultPath = scheme + "://" + serverName + serverPortName + contextPath;
        } else {
            String serverPath = System.getProperty(DEFAULT_SERVER_PATH);
            if (serverPath != null && !serverPath.isBlank()) {
                resultPath = serverPath;
            }
        }
        return resultPath;

    }


    public static String formatRequestParams(Map<String, Object> params) {
        if (params == null) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                String param = entry.getKey() + "=" + entry.getValue();
                joiner.add(param);
            }
        }

        return joiner.toString();

    }

    public static boolean isInWebScope() {
        try {
            return getCurrentRequest() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private HttpUtils() {
    }

    /**
     * Detect if current request is from an iphone
     *
     * @return
     */
    public static boolean isIphone() {
        try {
            UserAgentInfo userAgentInfo = getUserAgentInfo();
            return userAgentInfo.isIphone();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Detect if current request is from an iOS browser
     *
     * @return
     */
    public static boolean isIOS() {
        try {
            UserAgentInfo userAgentInfo = getUserAgentInfo();
            return userAgentInfo.detectIos();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Detect if current request is from an Android browser
     *
     * @return
     */
    public static boolean isAndroid() {
        try {
            UserAgentInfo userAgentInfo = getUserAgentInfo();
            return userAgentInfo.detectAndroid();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Return subdomain name or null if current server host has no subdomian
     *
     * @param request
     * @return
     */
    public static String getSubdomain(HttpServletRequest request) {
        String host = request.getServerName();
        if (host != null && host.contains(".")) {
            return host.substring(0, host.indexOf("."));
        } else
            return null;
    }
}

