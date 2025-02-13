package com.adyogi.notification.configuration;

import com.adyogi.notification.dto.emails.EmailNotificationDTO;
import com.adyogi.notification.services.EmailSubscriber;
import com.adyogi.notification.services.IncidentHandlingService;
import com.adyogi.notification.services.IncidentToEmailService;
import com.adyogi.notification.utils.FailureHandler;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.PubSubHeaderMapper;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.Header;

import java.io.IOException;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static com.adyogi.notification.utils.constants.ErrorConstants.*;


@Configuration
public class QueueConfig {

    private final Logger logger = LogUtil.getInstance();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailSubscriber emailSubscriber;

    @Autowired
    private IncidentToEmailService incidentToEmailService;

    @Autowired
    private IncidentHandlingService incidentHandlingService;

    @Autowired
    FailureHandler failureHandler;

    @Bean(name = EMAIL_SUB)
    public SubscribableChannel pubSubInputChannel() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();
        channel.setMaxSubscribers(5);
        return channel;
    }


    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(@Qualifier(value = EMAIL_SUB)
                                                             MessageChannel pubSubInputChannel,
                                                             PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, EMAIL_SUB);
        adapter.setOutputChannel(pubSubInputChannel);
        adapter.setHeaderMapper(new PubSubHeaderMapper());
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @ServiceActivator(inputChannel = EMAIL_SUB)
    public void messageReceiver(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) throws IOException {
        logger.info("Message arrived! Payload: " + payload);

        try {

            EmailNotificationDTO emailNotificationDTO = objectMapper.readValue(payload,
                    EmailNotificationDTO.class);

            emailSubscriber.sendEmail(emailNotificationDTO);

            message.ack();
        }
        catch (Exception e) {
            logger.error("Error in processing message: " + payload + e.getCause() + e.getMessage());
            message.nack();
            failureHandler.handleFailure(ERROR_ADDING_EMAIL_FOR_PROCESSING, e);
        }
    }

    @Bean(name = GENERATE_EMAIL_SUB_TOPIC)
    public SubscribableChannel pubSubIncidentChannel() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();
        channel.setMaxSubscribers(5);
        return channel;
    }

    @Bean
    public PubSubInboundChannelAdapter incidentMessageChannelAdapter(
            @Qualifier(value = GENERATE_EMAIL_SUB_TOPIC) MessageChannel pubSubIncidentChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, GENERATE_EMAIL_SUB_TOPIC);
        adapter.setOutputChannel(pubSubIncidentChannel);
        adapter.setHeaderMapper(new PubSubHeaderMapper());
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }


    @ServiceActivator(inputChannel = GENERATE_EMAIL_SUB_TOPIC)
    public void processEmail(String payload,
                             @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) throws IOException {
        logger.info("Incident Message arrived! Payload: " + payload);
        try {

            incidentToEmailService.sendEmail(payload);
            message.ack();
        } catch (Exception e) {
            logger.error("Error in processing incident message: " + payload + e.getCause() + e.getMessage());
            message.nack();
            failureHandler.handleFailure(String.format(ERROR_SENDING_EMAIL_TO_CLIENT, payload), e);
        }
    }


    @Bean(name = INCIDENT_COMPUTE_SUB)
    public SubscribableChannel pubSubBulkIncidentProcessSub() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();
        channel.setMaxSubscribers(3);
        return channel;
    }


    @Bean
    public PubSubInboundChannelAdapter processBulkIncidentAdapter(
            @Qualifier(value = INCIDENT_COMPUTE_SUB) MessageChannel pubSubIncidentChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, INCIDENT_COMPUTE_SUB);
        adapter.setOutputChannel(pubSubIncidentChannel);
        adapter.setHeaderMapper(new PubSubHeaderMapper());
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }


    @ServiceActivator(inputChannel = INCIDENT_COMPUTE_SUB)
    public void bulkIncidentComputeReceiver(String payload,
                                            @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) throws IOException {
        logger.info("Message arrived! Payload: " + payload);
        try {
            message.ack();
            incidentHandlingService.processIncidentsForClient(payload);
        }
        catch (Exception e) {
            logger.error("Error in processing message: " + payload + e.getCause() + e.getMessage());
            message.nack();
            failureHandler.handleFailure(ERROR_ADDING_EMAIL_FOR_PROCESSING, e);
        }
    }



}
