package com.adyogi.notification.components;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

import static com.adyogi.notification.utils.constants.ConfigConstants.ALERT_INTERVAL_MS;


@Component
public class AlertRateLimiter {
    private final ConcurrentHashMap<String, Long> lastAlertTimes = new ConcurrentHashMap<>();

    public boolean shouldAlert(String errorKey) {
        long now = System.currentTimeMillis();
        Long lastAlert = lastAlertTimes.get(errorKey);
        if (lastAlert == null || now - lastAlert >= ALERT_INTERVAL_MS) {
            lastAlertTimes.put(errorKey, now);
            return true;
        }
        return false;
    }
}



