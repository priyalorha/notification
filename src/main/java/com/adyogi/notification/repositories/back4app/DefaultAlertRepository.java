package com.adyogi.notification.repositories.back4app;
import com.adyogi.notification.database.mongo.entities.DefaultAlert;
import com.adyogi.notification.utils.constants.TableConstants;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultAlertRepository
        extends MongoRepository<DefaultAlert, String> {

    @Query("{'status': ?0}")
    List<DefaultAlert> findByStatus(TableConstants.STATUS status);
}
