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

    @Query("SELECT m FROM Metrics m WHERE m.clientId = :clientId AND m.metric IN :metrics")
    List<Metrics> findAllByMetricAndClientId(@Param("clientId") String clientId,
                                                 @Param("metrics") List<TableConstants.METRIC> metrics);

    @Query("SELECT m FROM Metrics m WHERE m.metric IN :metrics and clientId IN :clientIds")
    Page<Metrics> findAllByMetricNameWhereClientId (@Param("metrics") List<TableConstants.METRIC> metrics,
                                                       @Param("clientIds") List<String> clientId,
                                                       Pageable pageable);

    @Query("SELECT distinct m.clientId FROM Metrics m")
    List<String> getDistinctClientId();

    @Query("SELECT m FROM Metrics m WHERE m.clientId = :clientId AND m.objectType = :objectType")
    List<Metrics> findMetricsByClientIdAndObjectType(String clientId, String objectType);

}