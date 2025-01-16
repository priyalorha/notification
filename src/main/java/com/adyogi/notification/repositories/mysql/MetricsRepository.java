package com.adyogi.notification.repositories.mysql;

import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.database.sql.entities.MetricId;
import com.adyogi.notification.utils.constants.TableConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, MetricId> {


    public default void saveAll(List<Metrics> metrics) {}


    public List<Metrics> findMetricByClientId(String clientId);

    @Query("SELECT m FROM Metrics m WHERE m.clientId = :clientId AND m.metricName IN :metricNames")
    List<Metrics> findAllByMetricNameAndClientId(@Param("clientId") String clientId,
                                                 @Param("metricNames") List<TableConstants.METRIC_NAME> metricNames);

    @Query("SELECT m FROM Metrics m WHERE m.metricName IN :metricNames and clientId NOT IN :clientIds")
    Page<Metrics> findAllByMetricNameWhereNotClientId (@Param("metricNames") List<TableConstants.METRIC_NAME> metricNames,
                                                       @Param("clientIds") List<String> clientId,
                                                       Pageable pageable);

    @Query("SELECT distinct m.clientId FROM Metrics m")
    List<String> getDistinctClientId();

    @Query("SELECT m FROM Metrics m WHERE m.clientId = :clientId AND m.objectType = :objectType")
    List<Metrics> findMetricsByClientIdAndObjectType(String clientId, String objectType);

}