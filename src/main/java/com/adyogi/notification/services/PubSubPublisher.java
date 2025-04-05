package com.adyogi.notification.services;

import com.adyogi.notification.configuration.PubSubConfig;
import com.adyogi.notification.utils.rollbar.RollbarManager;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PubSubPublisher {

    private static final Logger logger = Logger.getLogger(PubSubPublisher.class.getName());

    private final PubSubConfig pubSubConfig;

    // Constructor injection for configuration
    @Autowired
    public PubSubPublisher(PubSubConfig pubSubConfig) {
        this.pubSubConfig = pubSubConfig;
    }

    public void publishToPubSub(String topicId, String message) {
        // Load projectId from configuration
        String projectId = pubSubConfig.getProjectId();

        // Set up the Pub/Sub publisher
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(message))
                    .build();

            // Publish the message and block until it's published
            publisher.publish(pubsubMessage).get();
            logger.info("Message published to topic " + topicId + " successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to publish message to Pub/Sub.", e);
            RollbarManager.sendExceptionOnRollBar("Failed to publish message to Pub/Sub ", e);
        } finally {
            // Ensure the publisher is shut down properly
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }


        /**
         * Publishes a message to a specified Pub/Sub topic.
         *
         * @param topicId        The Pub/Sub topic to publish to.
         * @param messageDetails A map of key-value pairs to be published in the message.
         * @throws IOException          If an error occurs during publishing.
         * @throws ExecutionException   If an error occurs when getting the result of the publish.
         * @throws InterruptedException If the publishing thread is interrupted.
         */



        public void publishToPubSub(String topicId, Map<String, Object> messageDetails) {
        // Load projectId from configuration
        String projectId = pubSubConfig.getProjectId();

        // Set up the Pub/Sub publisher
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            // Convert the message details to JSON
            String messageJson = new Gson().toJson(messageDetails);

            // Create a PubsubMessage
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(messageJson))
                    .build();

            // Publish the message and block until it's published
            publisher.publish(pubsubMessage).get();
            logger.info("Message published to topic " + topicId + " successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to publish message to Pub/Sub.", e);
            RollbarManager.sendExceptionOnRollBar("Failed to publish message to Pub/Sub " , e);
        } finally {
            // Ensure the publisher is shut down properly
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}
