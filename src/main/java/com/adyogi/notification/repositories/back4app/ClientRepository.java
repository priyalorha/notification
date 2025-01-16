package com.adyogi.notification.repositories.back4app;

import com.adyogi.notification.database.mongo.entities.ParseClient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;

import static com.adyogi.notification.utils.constants.MongoConstants.CLIENT_COLLECTION_NAME;


@Repository
public interface ClientRepository extends MongoRepository<ParseClient, String> {
    // You can add custom queries here if needed, for example:
    @Cacheable(value=CLIENT_COLLECTION_NAME)
    ParseClient findByClientId(String clientId);
}