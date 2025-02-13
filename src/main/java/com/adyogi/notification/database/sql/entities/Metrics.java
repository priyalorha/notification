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
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Id
    private TableConstants.METRIC metric;// Composite key

    @Id
    @Enumerated(EnumType.STRING) // Map ENUM as string
    private TableConstants.OBJECT_TYPE objectType;

    @Id
    private String objectIdentifier;

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
        return this.getClientId() + this.getMetric() + this.getObjectType() + this.getObjectIdentifier();
    }

}
