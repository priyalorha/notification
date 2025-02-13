package com.adyogi.notification.repositories.back4app;

import com.adyogi.notification.database.mongo.entities.ParseClient;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

import static com.adyogi.notification.utils.constants.MongoConstants.CLIENT_COLLECTION_NAME;


@Repository
public interface ClientRepository extends MongoRepository<ParseClient, String> {


    @Query(value = "{ $and: [ { 'flag_attrited': { $ne: 'Yes' } }, { 'id': ?0 } ] }", exists = true)
    boolean existingActiveClient(String clientId);

}