package com.adyogi.notification.database.sql.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

import static com.adyogi.notification.utils.constants.TableConstants.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Embeddable
public class MetricId implements Serializable {

    @Column(name = CLIENT_ID)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = METRIC_NAME_COL_NAME)
    private TableConstants.METRIC_NAME metricName;

    @Enumerated(EnumType.STRING)
    @Column(name = OBJECT_TYPE_COL_NAME)
    private TableConstants.OBJECT_TYPE objectType;

    @Column(name = OBJECT_ID)
    private String objectId; // Composite key

}
