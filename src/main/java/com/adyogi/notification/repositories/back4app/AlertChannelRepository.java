package com.adyogi.notification.repositories.back4app;

import com.adyogi.notification.database.mongo.entities.AlertChannel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AlertChannelRepository extends MongoRepository<AlertChannel, String> {

    @Query("{'clientId': {'$eq': ?0}}")
    List<AlertChannel> findByClientId(String clientId);

    @Query("{'clientId': {'$eq': ?0} , 'alertChannel': {'$eq': ?1}}")
    AlertChannel findByClientIdAndAlertChannel(String clientId, String alertChannel);
}

