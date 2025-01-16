package com.adyogi.notification.database.sql.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.adyogi.notification.utils.constants.TableConstants.*;

@Entity
@Table(name = TABLE_NAME_METRICS) // Maps to the "metrics" table
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MetricId.class)
@DynamicUpdate

public class Metrics{

    @Id
    @Column(name = CLIENT_ID, nullable = false, length = 50)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = METRIC_NAME_COL_NAME, nullable = false)
    private TableConstants.METRIC_NAME metricName;// Composite key

    @Id
    @Column(name = OBJECT_TYPE_COL_NAME, nullable = false)
    @Enumerated(EnumType.STRING) // Map ENUM as string
    private TableConstants.OBJECT_TYPE objectType;

    @Id
    @Column(name = OBJECT_ID, nullable = false, length = 50)
    private String objectId; // Composite key

    @Column(name = VALUE)
    private String value;

    @Column(name = VALUE_DATA_TYPE)
    private String valueDataType;

    @Column(name = CREATED_AT, nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = UPDATED_AT, nullable = false)
    private LocalDateTime updatedAt;

    // this is for reference, we have used this for lookup,
    public String getMetricId(){
        return this.getClientId() + this.getMetricName() + this.getObjectType() + this.getObjectId();
    }

}
