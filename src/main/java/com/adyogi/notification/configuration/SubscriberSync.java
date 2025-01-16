package com.adyogi.notification.configuration;

import com.adyogi.notification.dto.emails.EmailNotificationDTO;
import com.adyogi.notification.services.PubSubEmailSubscriber;
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

import static com.adyogi.notification.utils.constants.ConfigConstants.EMAIL_SUB;


@Configuration
public class SubscriberSync {

    private final Logger logger = LogUtil.getInstance();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PubSubEmailSubscriber pubSubEmailSubscriber;

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
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "email-sub");
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

            pubSubEmailSubscriber.sendEmail(emailNotificationDTO);

            message.ack();
        }
        catch (Exception e) {
            logger.error("Error in processing message: " + payload + e.getCause() + e.getMessage());
            message.nack();
        }
    }

}
