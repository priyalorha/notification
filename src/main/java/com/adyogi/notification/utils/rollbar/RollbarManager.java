package com.adyogi.notification.utils.rollbar;

import com.adyogi.notification.utils.constants.ConfigConstants;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.ConfigBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class RollbarManager {
    public final static String ROLLBAR_ACCESS_TOKEN = getAccessToken("ROLLBAR_ACCESS_TOKEN");
    public final static String CRITICAL_ROLLBAR_ACCESS_TOKEN = getAccessToken("CRITICAL_ROLLBAR");

    private static String getAccessToken(String propertyName) {
        String token = System.getenv(propertyName);
        if (token == null || token.isEmpty()) {
            // Handle missing token (e.g., throw an exception, use a default value, or log a warning)
            throw new IllegalArgumentException("Access token for " + propertyName + " is missing");
        }
        return token;
    }

    final static Rollbar rollbar = new Rollbar(ConfigBuilder.withAccessToken(ROLLBAR_ACCESS_TOKEN)
            .environment(ConfigConstants.PRODUCTION_ENVIRONMENT).handleUncaughtErrors(true).build());
    final static Rollbar criticalRollbar = new Rollbar(ConfigBuilder.withAccessToken(CRITICAL_ROLLBAR_ACCESS_TOKEN)
            .environment(ConfigConstants.PRODUCTION_ENVIRONMENT).handleUncaughtErrors(true).build());

    public static void sendExceptionOnRollBar(String message, Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        Map<String, Object> map = new HashMap<>();
        map.put("message", e.getMessage());
        map.put("trace", exceptionAsString);
        rollbar.error(message, map);
    }

    public static void sendExceptionOnRollBar(String message, Map<String, Object> entries) {
        rollbar.error(message, entries);
    }

    public static void sendExceptionOnRollBar(String message, String response, Map<String, Object> entries) {

        Map<String, Object> map = new HashMap<>();
        map.put("response", response);
        map.putAll(entries);
        rollbar.error(message, map);
    }

    public static void sendExceptionOnRollBar(String message, Exception e, Map<String, Object> entries) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        Map<String, Object> map = new HashMap<>();
        map.put("message", e.getMessage());
        map.put("trace", exceptionAsString);
        map.putAll(entries);
        rollbar.error(message, map);
    }

    public static void sendExceptionOnRollBar(String message, String error) {

        Map<String, Object> map = new HashMap<>();
        map.put("message", "[" + error + "]");
        rollbar.error(message, map);

    }
    public static void sendExceptionOnCriticalRollBar(String message, Exception e) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        Map<String, Object> map = new HashMap<>();
        map.put("message", e.getMessage());
        map.put("trace", exceptionAsString);
        criticalRollbar.error(message, map);
    }

    public static void sendExceptionOnCriticalRollBar(String message, Map<String, Object> entries) {

        criticalRollbar.error(message, entries);
    }

    public static void sendExceptionOnCriticalRollBar(String message, String response, Map<String, Object> entries) {

        Map<String, Object> map = new HashMap<>();
        map.put("response", response);
        map.putAll(entries);
        criticalRollbar.error(message, map);
    }

    public static void sendExceptionOnCriticalRollBar(String message, Exception e, Map<String, Object> entries) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        Map<String, Object> map = new HashMap<>();
        map.put("message", e.getMessage());
        map.put("trace", exceptionAsString);
        map.putAll(entries);
        criticalRollbar.error(message, map);
    }

    public static void sendExceptionOnCriticalRollBar(String message, String error) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", "[" + error + "]");
        criticalRollbar.error(message, map);
    }
}
