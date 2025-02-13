package com.adyogi.notification.database.sql.entities;


import com.adyogi.notification.utils.constants.BigQueryConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static com.adyogi.notification.utils.constants.TableConstants.*;

@Entity
@Table(name = TABLE_NAME_BASELINE) // Maps to the "metrics" table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(BaselineId.class)
@DynamicUpdate
public class Baseline implements Serializable {

    @Id
    @Column(name = ALERT_ID)
    private String alertId;

    @Id
    @Column(name = CLIENT_ID)
    private String clientId;

    @Id
    @Column(name = METRIC_COL_NAME)
    @Enumerated(EnumType.STRING) // Map ENUM as string
    private METRIC metric;

    @Id
    @Column(name = OBJECT_TYPE_COL_NAME)
    @Enumerated(EnumType.STRING) // Map ENUM as string

    private TableConstants.OBJECT_TYPE objectType;

    @Id
    @Column(name = OBJECT_IDENTIFIER)
    private String objectIdentifier;

    @Column(name = BASE_VALUE)
    private String value;

    @Column(name = CREATED_AT, nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = UPDATED_AT, nullable = false)
    private LocalDateTime updatedAt;

    public Map<String, Object> toMap() { // Use Object as value type
        Map<String, Object> map = new HashMap<>();
        map.put(BigQueryConstants.ALERT_ID, this.alertId);
        map.put(BigQueryConstants.CLIENT_ID, this.clientId);
        map.put(BigQueryConstants.METRIC, this.metric.toString());
        map.put(BigQueryConstants.OBJECT_TYPE, this.objectType.toString());
        map.put(BigQueryConstants.OBJECT_IDENTIFIER, this.getObjectIdentifier());
        map.put(BigQueryConstants.VALUE, this.value);
        map.put(BigQueryConstants.CREATED_AT, this.createdAt.toString());
        map.put(BigQueryConstants.UPDATED_AT, this.updatedAt.toString());
        return map;
    }

    // this is for reference, we have used this for lookup,
    public String getMetricId(){
        return this.getClientId() + this.getMetric() + this.getObjectType() + this.getObjectIdentifier();
    }

    // this is for reference, we have used this for lookup,
    public String getBaselineId(){
        return this.alertId + this.getMetricId();
    }

}
