package com.adyogi.notification.services;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static com.adyogi.notification.utils.constants.ErrorConstants.CACHE_NOT_FOUND;

@Service
public class RateLimiterService {

    @Autowired
    private CacheManager cacheManager;

    public boolean canTrigger(String cacheName, String clientId) {

        Cache cache = cacheManager.getCache(cacheName);
//        Cache cache = cacheManager.getCache(EMAIL_CACHE);
        if (cache == null) {
            cache = new Cache(cacheName, MAX_CACHE_RATE_LIMITER, false, false, 0, 0);
            cacheManager.addCache(cache);
        }

        Element element = cache.get(clientId);
        if (element == null) {
            cache.put(new Element(clientId, LocalDateTime.now()));
            return true;
        }

        LocalDateTime lastEmailSentTime = (LocalDateTime) element.getObjectValue();
        long minutesSinceLastTrigger = Duration.between(lastEmailSentTime, LocalDateTime.now()).toMinutes();

        if (minutesSinceLastTrigger >= rateLimits.getOrDefault(cacheName,
                MIN_LAST_INCIDENT_TRIGGER)) {
            cache.put(new Element(clientId, LocalDateTime.now()));
            return true;
        }

        return false;
    }
}
