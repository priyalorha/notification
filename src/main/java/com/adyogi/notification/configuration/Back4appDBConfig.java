package com.adyogi.notification.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static java.util.Collections.singletonList;

@Configuration
@EnableMongoRepositories(basePackages = {"com.adyogi.notification.repositories.back4app"}, mongoTemplateRef = BACK4APP_MONGO_TEMPLATE)
@EnableConfigurationProperties
public class Back4appDBConfig {
    @Bean(name = BACK4APP_PROPERTIES)
    @ConfigurationProperties(prefix = MONGODB_NAME_BACK4APP_ADYOGI_ADS)
    @Primary
    public MongoProperties back4appProperties() {
        return new MongoProperties();
    }

    @Bean(name = BACK4APP_MONGO_CLIENT)
    public MongoClient mongoClient(@Qualifier(BACK4APP_PROPERTIES) MongoProperties mongoProperties,
                                   @Value("${back4app.connection.timeout}") Integer connectionTimeout,
                                   @Value("${back4app.read.timeout}") Integer readTimeout,
                                   @Value("${back4app.pool.min.size}") Integer minPoolSize,
                                   @Value("${back4app.pool.max.size}") Integer maxPoolSize,
                                   @Value("${back4app.pool.max.connection.idle.time}") Integer maxconnectionIdleTime) {

        MongoCredential credential = MongoCredential
                .createCredential(mongoProperties.getUsername(), mongoProperties.getAuthenticationDatabase(), mongoProperties.getPassword());

        return MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder
                        .hosts(singletonList(new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort()))))
                .applyToSocketSettings(builder -> builder.connectTimeout(connectionTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS))
                .applyToConnectionPoolSettings(builder -> builder.minSize(minPoolSize).maxSize(maxPoolSize).maxConnectionIdleTime(maxconnectionIdleTime, TimeUnit.SECONDS))
                .credential(credential)
                .build());
    }

    @Primary
    @Bean(name = BACK4APP_MONGODB_FACTORY)
    public MongoDatabaseFactory mongoDatabaseFactory(@Qualifier(BACK4APP_MONGO_CLIENT) MongoClient mongoClient, @Qualifier(BACK4APP_PROPERTIES) MongoProperties mongoProperties) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoProperties.getDatabase());
    }

    @Primary
    @Bean(name = BACK4APP_MONGO_TEMPLATE)
    public MongoTemplate mongoTemplate(@Qualifier(BACK4APP_MONGODB_FACTORY) MongoDatabaseFactory mongoDatabaseFactory) {
        MappingMongoConverter converter =
                new MappingMongoConverter(mongoDatabaseFactory, new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDatabaseFactory, converter);
    }
}
