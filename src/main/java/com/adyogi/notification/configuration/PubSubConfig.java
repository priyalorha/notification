package com.adyogi.notification.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PubSubConfig {

    @Value("${pubsub.project-id}")
    private String projectId;

    @Value("${pubsub.topic-id}")
    private String topicId;

    public String getProjectId() {
        return projectId;
    }

    public String getTopicId() {
        return topicId;
    }
}
