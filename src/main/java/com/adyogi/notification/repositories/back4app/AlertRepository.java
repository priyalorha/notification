package com.adyogi.notification.repositories.back4app;
import com.adyogi.notification.database.mongo.entities.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {

    @Query("{'clientId': ?0}")
    List <Alert> findByClientId(String clientId);

    @Query("{'status': ?1, 'clientId': ?0}")
    List<Alert> findAlertsByClientIdAndStatus(String clientId, String status);

    Alert findByObjectId(String objectId, String clientId);

}
