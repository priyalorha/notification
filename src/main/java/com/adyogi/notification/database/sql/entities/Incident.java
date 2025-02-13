package com.adyogi.notification.database.sql.entities;


import javax.persistence.*;

import com.adyogi.notification.configuration.AlertChannelStringListConverter;
import com.adyogi.notification.utils.constants.BigQueryConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import static com.adyogi.notification.utils.constants.TableConstants.*;


@Entity
@Table(name = TABLE_NAME_INCIDENTS) // Maps to the "metrics" table
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(IncidentId.class)
@Builder
@DynamicUpdate
@TypeDef(name = "json", typeClass = JsonType.class)
public class Incident  implements Serializable {

    @Id
    @Column(name = ALERT_ID, nullable = false,  length = 50)
    private String alertId;

    @Id
    @Column(name = CLIENT_ID, nullable = false,  length = 50)
    private String clientId;

    @Id
    @Column(name = METRIC_COL_NAME, nullable = false,  length = 50)
    @Enumerated(EnumType.STRING) // Map ENUM as string
    private METRIC metric;

    @Id
    @Column(name = OBJECT_TYPE_COL_NAME, nullable = false,  length = 50)
    @Enumerated(EnumType.STRING) // Map ENUM as string
    private TableConstants.OBJECT_TYPE objectType;

    @Id
    @Column(name = OBJECT_IDENTIFIER, nullable = false,  length = 100)
    private String objectIdentifier;


    @Column(name = MESSSAGE, nullable = false)
    private String message;


    @Enumerated(EnumType.STRING)
    @Column(name = NOTIFICATION_STATUS_COL_NAME, nullable = false)
    private TableConstants.NOTIFICATION_STATUS notificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = INCIDENT_STATUS_COL_NAME, nullable = false)
    private TableConstants.INCIDENT_STATUS incidentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_COL_NAME, nullable = false)
    private ALERT_STATUS status;

    @Column(name = BASE_VALUE_INCIDENT_COL, nullable = false)
    private String baseValue;

    @Column(name = CREATED_AT, nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = UPDATED_AT, nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = NOTIFICATION_SENT_AT)
    private LocalDateTime notificationSentAt;

    @Column(name = VALUE)
    private String value;

    @Column(name = VALUE_DATA_TYPE)
    private String valueDataType;


    @Column(name = ALERT_RESEND_INTERVAL_MIN)
    private Integer alertResendIntervalMin;

    @Column(name = ALERT_CHANNEL)
    @Convert(converter = AlertChannelStringListConverter.class)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ALERT_CHANNEL> alertChannel;

    @Column(name = INCIDENT_ID, nullable = false, unique = true, updatable = false)
    private Long incidentId;



    public Map<String, Object> toMap() { // Use Object as value type
        Map<String, Object> map = new HashMap<>();
        map.put(BigQueryConstants.BIG_QUERY_ID, this.incidentId);
        map.put(BigQueryConstants.INCIDENT_ID, this.incidentId);
        map.put(BigQueryConstants.CLIENT_ID, this.clientId);
        map.put(BigQueryConstants.METRIC, String.valueOf(this.metric));
        map.put(BigQueryConstants.OBJECT_TYPE, String.valueOf(this.objectType.toString()));
        map.put(BigQueryConstants.OBJECT_IDENTIFIER, this.objectIdentifier);
        map.put(BigQueryConstants.VALUE, this.value);
        map.put(BigQueryConstants.VALUE_DATATYPE, String.valueOf(this.valueDataType));
        map.put(BigQueryConstants.CREATED_AT, this.createdAt.toString());
        map.put(BigQueryConstants.UPDATED_AT, this.updatedAt.toString());
        map.put(BigQueryConstants.ALERT_ID, this.alertId);
        map.put(BigQueryConstants.MESSSAGE, this.message);
        map.put(BigQueryConstants.ALERT_CHANNEL,  this.alertChannel.toString());
        map.put(BigQueryConstants.NOTIFICATION_STATUS, String.valueOf(this.notificationStatus));
        map.put(BigQueryConstants.INCIDENT_STATUS, String.valueOf(this.incidentStatus));
        map.put(BigQueryConstants.STATUS, String.valueOf(this.status));
        map.put(BigQueryConstants.BASE_VALUE, this.baseValue);
        if (this.notificationSentAt != null)
        {
            map.put(BigQueryConstants.NOTIFICATION_SENT_AT, this.notificationSentAt.toString());
        };;
        map.put(BigQueryConstants.ALERT_RESEND_INTERVAL_MIN, this.alertResendIntervalMin);

        return map;

    }

    // this is for reference, we have used this for lookup,
    public String getMetricId(){
        return this.getClientId() + this.getMetric() + this.getObjectType() + this.getObjectIdentifier();
    }

    // this is for reference, we have used this for lookup,
    public String getBaselineId(){
        return this.alertId + this.getClientId() + this.getMetric() + this.getObjectType() + this.getObjectIdentifier();
    }

}
