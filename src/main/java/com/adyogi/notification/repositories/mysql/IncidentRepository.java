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

    Optional<Incident> findByIncidentId(Long incident_id);

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
            "i.metric = :metric AND " +
            "i.objectType = :objectType AND " +
            "i.objectIdentifier = :objectIdentifier AND " +
            "i.incidentStatus = :status")

    Optional<Incident> findByAlertIdAndClientIdAndMetricAndObjectTypeAndObjectIdentifierAndIncidentStatus(
            @Param("alertId") String alertId,
            @Param("clientId") String clientId,
            @Param("metric") TableConstants.METRIC metric,
            @Param("objectType") TableConstants.OBJECT_TYPE objectType,
            @Param("objectIdentifier") String objectIdentifier,
            @Param("status") TableConstants.INCIDENT_STATUS status);


    @Query("SELECT i FROM Incident i WHERE " +
            "i.clientId = :clientId AND " +
            "i.incidentStatus = :incidentStatus AND " +
            "i.status = :alertStatus")
    List<Incident> findOpenEnabledIncidents(@Param("clientId") String clientId,
                                            @Param("incidentStatus") TableConstants.INCIDENT_STATUS incidentStatus,
                                            @Param("alertStatus") TableConstants.ALERT_STATUS alertStatus);




    @Query("SELECT distinct i.clientId FROM Incident i WHERE " +
            "i.incidentStatus = :incidentStatus AND " +
            "i.status = :alertStatus")

    List<String> findClientIdsWithOpenIncidents(@Param("incidentStatus") TableConstants.INCIDENT_STATUS incidentStatus,
                                                @Param("alertStatus") TableConstants.ALERT_STATUS alertStatus);

}