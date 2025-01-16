package com.adyogi.notification.repositories.mysql;
import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.BaselineId;
import com.adyogi.notification.utils.constants.TableConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaselineRepository extends JpaRepository<Baseline, BaselineId> {


    public List<Baseline> findBaselineByClientId(String clientId);

    @Query("SELECT m FROM Baseline m WHERE m.alertId = :alertId AND m.clientId = :clientId AND m.objectType = :objectType AND m.objectId = :objectId")
    Baseline findBaselineById(
            @Param("alertId") String alertId,
            @Param("clientId") String clientId,
            @Param("objectType") TableConstants.OBJECT_TYPE objectType,
            @Param("objectId") String objectId
    );

    @Query("SELECT m FROM Baseline m WHERE m.clientId = :clientId AND m.metricName = :metricName AND m.objectType = :objectType AND m.objectId = :objectId")
    List<Baseline> findBaselineByIdExceptAlertId(
            @Param("clientId") String clientId,
            @Param("metricName") TableConstants.METRIC_NAME metricName,
            @Param("objectType") TableConstants.OBJECT_TYPE objectType,
            @Param("objectId") String objectId
    );
}
