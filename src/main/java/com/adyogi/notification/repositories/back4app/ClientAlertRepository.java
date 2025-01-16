package com.adyogi.notification.repositories.back4app;
import com.adyogi.notification.database.mongo.entities.ClientAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface ClientAlertRepository extends MongoRepository<ClientAlert, String> {

    @Query(value = "{}", fields = "{'clientId': 1}")
    List<String> findDistinctClientIds();

    @Query("{'clientId': ?0}")
    List <ClientAlert> findByClientId(String clientId);

    @Query("{'status': ?1, 'clientId': ?0}")
    List<ClientAlert> findAlertsByClientIdAndStatus(String clientId, String status);

    @Query("{'_id': ?0, 'clientId': ?1}")
    ClientAlert findByObjectIdAndClientId(String objectId , String ClientId);

}
