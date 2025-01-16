package com.adyogi.notification.repositories.mysql;

import com.adyogi.notification.database.sql.entities.Incident;
import com.adyogi.notification.database.sql.entities.IncidentId;
import com.adyogi.notification.utils.constants.TableConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


@Repository
public interface IncidentRepository extends JpaRepository<Incident, IncidentId> {

    Optional<Incident> findByIncidentId(@Param("incident_id") Long incident_id);

    Optional<Incident> findByIncidentIdAndClientId(@Param("incident_id")Long id, @Param("clientId")String ClientId);

    Page<Incident> findByClientId(String clientId, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE " +
            "i.clientId = :clientId AND " +
            "i.incidentStatus = :status")

    List<Incident> findByClientIdAndStatus(
            @Param("clientId") String clientId,
            @Param("status") TableConstants.INCIDENT_STATUS status);


    @Query("SELECT i FROM Incident i WHERE " +
            "i.alertId = :alertId AND " +
            "i.clientId = :clientId AND " +
            "i.metricName = :metricName AND " +
            "i.objectType = :objectType AND " +
            "i.objectId = :objectId AND " +
            "i.incidentStatus = :status")

    Optional<Incident> findByAlertIdAndClientIdAndMetricNameAndObjectTypeAndObjectIdAndIncidentStatus(
            @Param("alertId") String alertId,
            @Param("clientId") String clientId,
            @Param("metricName") TableConstants.METRIC_NAME metricName,
            @Param("objectType") TableConstants.OBJECT_TYPE objectType,
            @Param("objectId") String objectId,
            @Param("status") TableConstants.INCIDENT_STATUS status);


    @Query("SELECT i FROM Incident i WHERE " +
            "i.clientId = :clientId AND " +
            "i.incidentStatus = :incidentStatus AND " +
            "i.status = :status")
    List<Incident> findOpenEnabledIncidents(@Param("clientId") String clientId,
                                            @Param("incidentStatus") TableConstants.INCIDENT_STATUS incidentStatus,
                                            @Param("status") TableConstants.STATUS status);




    @Query("SELECT distinct i.clientId FROM Incident i WHERE " +
            "i.incidentStatus = :incidentStatus AND " +
            "i.status = :status")

    List<String> findClientIdsWithOpenIncidents(@Param("incidentStatus") TableConstants.INCIDENT_STATUS incidentStatus,
                                                @Param("status") TableConstants.STATUS status);

}