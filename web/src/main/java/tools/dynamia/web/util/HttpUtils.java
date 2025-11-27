/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.commons.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Utility class for HTTP-related operations, including request handling, IP utilities, device detection, and HTTP requests.
 *
 * @author Mario A. Serrano Leones
 */
public class HttpUtils {

    /**
     * Constant representing a tablet device type.
     */
    public static final String DEVICE_TABLET = "tablet";

    /**
     * Constant representing a smartphone device type.
     */
    public static final String DEVICE_SMARTPHONE = "smartphone";

    /**
     * Constant representing a standard screen device type.
     */
    public static final String DEVICE_SCREEN = "screen";

    private static final String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private static final Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");

    /**
     * System property name to setup default server path. Useful when app is running in none servlet context
     */
    public static final String DEFAULT_SERVER_PATH_PROP = "defaultServerPath";

    /**
     * Environment variable name for default server path.
     */
    public static final String DEFAULT_SERVER_PATH_ENV = "DEFAULT_SERVER_PATH";

    /**
     * Executes a simple HTTP GET request to the specified URL and returns the response body as a string.
     * Throws HttpServiceException if the request fails.
     *
     * @param url the URL to send the GET request to
     * @return the response body as a string
     * @throws HttpServiceException if an error occurs during the HTTP request
     */
    public static String executeHttpRequest(String url) {
        try {
            var request = HttpRequest.newBuilder().GET()
                    .uri(new URI(url)).build();


            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new HttpServiceException("Cannot perform http GET request: " + url, e);
        }

    }

    /**
     * Executes an HTTP GET request to the specified URL with custom headers and query parameters.
     * Returns the response body as a string.
     *
     * @param url     the URL to send the GET request to
     * @param headers a map of HTTP headers to include in the request
     * @param params  a map of query parameters to append to the URL
     * @return the response body as a string
     * @throws IOException          if an I/O error occurs
     * @throws HttpServiceException if an error occurs during the HTTP request
     */
    public static String executeHttpRequest(String url, Map<String, String> headers, Map<String, Object> params) throws IOException {

        try {
            var request = HttpRequest.newBuilder().GET()
                    .uri(new URI(url + "?" + formatRequestParams(params)));

            headers.forEach(request::header);

            return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new HttpServiceException("Cannot perform http GET request: " + url, e);
        }


    }

    /**
     * Retrieves the current HttpServletRequest from the Spring request context.
     * Returns null if not in a web context or if an error occurs.
     *
     * @return the current HttpServletRequest, or null if unavailable
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes requestAttrb = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return requestAttrb.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Retrieves the session ID from the current request.
     *
     * @return the session ID as a string
     */
    public static String getSessionId() {
        ServletRequestAttributes requestAttrb = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttrb.getSessionId();
    }

    /**
     * Retrieves the client IP address from the current request.
     *
     * @return the client IP address as a string
     */
    public static String getClientIp() {
        return getIpFromRequest(getCurrentRequest());
    }

    /**
     * Retrieves the user-agent string from the current request.
     *
     * @return the user-agent header value, or null if unavailable
     */
    public static String getBrowser() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader("user-agent");
    }

    /**
     * Converts a long value representing an IPv4 address to its dotted decimal string representation.
     *
     * @param longIp the IPv4 address as a long
     * @return the IPv4 address as a string in dotted decimal format
     */
    public static String longToIpV4(long longIp) {
        int octet3 = (int) ((longIp >> 24) % 256);
        int octet2 = (int) ((longIp >> 16) % 256);
        int octet1 = (int) ((longIp >> 8) % 256);
        int octet0 = (int) ((longIp) % 256);
        return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
    }

    /**
     * Converts an IPv4 address in dotted decimal format to its long representation.
     *
     * @param ip the IPv4 address as a string
     * @return the IPv4 address as a long
     */
    public static long ipV4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) + ((long) Integer.parseInt(octets[1]) << 16)
                + ((long) Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
    }

    /**
     * Checks if the given IPv4 address is a private address (e.g., 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16).
     *
     * @param ip the IPv4 address as a string
     * @return true if the address is private, false otherwise
     */
    public static boolean isIPv4Private(String ip) {
        long longIp = ipV4ToLong(ip);
        return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255"))
                || (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255"))
                || longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
    }

    /**
     * Validates if the given string is a valid IPv4 address.
     *
     * @param ip the string to validate
     * @return true if the string is a valid IPv4 address, false otherwise
     */
    public static boolean isIPv4Valid(String ip) {
        return pattern.matcher(ip).matches();
    }

    /**
     * Extracts the client IP address from the HttpServletRequest, considering forwarded headers.
     * Prefers non-private IPs from the X-Forwarded-For header, falls back to remote address.
     *
     * @param request the HttpServletRequest
     * @return the client IP address as a string, or "0.0.0.0" if request is null
     */
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

    /**
     * Retrieves user agent information from the current request.
     *
     * @return a UserAgentInfo object containing user agent details
     */
    public static UserAgentInfo getUserAgentInfo() {

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return new UserAgentInfo(request);
        } else {
            return new UserAgentInfo("", "");
        }

    }

    /**
     * Detects the device type (tablet, smartphone, or screen) based on the user agent of the current request.
     *
     * @return the device type as a string: "tablet", "smartphone", or "screen"
     */
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

    /**
     * Checks if the current request is from a smartphone.
     *
     * @return true if the device is a smartphone, false otherwise
     */
    public static boolean isSmartphone() {
        return detectDevice().equals(DEVICE_SMARTPHONE);
    }

    /**
     * Checks if the current request is from a tablet.
     *
     * @return true if the device is a tablet, false otherwise
     */
    public static boolean isTablet() {
        return detectDevice().equals(DEVICE_TABLET);
    }

    /**
     * Returns the server path including scheme, server name, port, and context path using the current request.
     * If no current request is available, uses the DEFAULT_SERVER_PATH system property or environment variable.
     *
     * @return the server path as a string
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
            String serverPath = StringUtils.getSystemPropertyOrEnv(DEFAULT_SERVER_PATH_PROP,
                    StringUtils.getSystemPropertyOrEnv(DEFAULT_SERVER_PATH_ENV));

            if (serverPath != null && !serverPath.isBlank()) {
                resultPath = serverPath;
            }
        }
        return resultPath;

    }

    /**
     * Formats a map of parameters into a URL query string.
     *
     * @param params a map of parameter names to values
     * @return the formatted query string, or empty string if params is null
     */
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

    /**
     * Checks if the current execution is within a web request scope.
     *
     * @return true if in web scope, false otherwise
     */
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
     * Detects if the current request is from an iPhone.
     *
     * @return true if the device is an iPhone, false otherwise
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
     * Detects if the current request is from an iOS device.
     *
     * @return true if the device is iOS, false otherwise
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
     * Detects if the current request is from an Android device.
     *
     * @return true if the device is Android, false otherwise
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
     * Returns the subdomain name from the server name of the request, or null if no subdomain is present.
     *
     * @param request the HttpServletRequest
     * @return the subdomain as a string, or null
     */
    public static String getSubdomain(HttpServletRequest request) {
        if (request != null) {
            String host = request.getServerName();
            if (host != null && host.contains(".")) {
                return host.substring(0, host.indexOf("."));
            }
        }
        return null;
    }
}

