package com.adyogi.notification.database.sql.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;


import javax.persistence.*;
import java.io.Serializable;

import static com.adyogi.notification.utils.constants.TableConstants.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Embeddable
public class MetricId implements Serializable {

    @Column(name = CLIENT_ID, nullable = false, length = 50)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = METRIC_COL_NAME, nullable = false, length = 50)
    private METRIC metric;

    @Enumerated(EnumType.STRING)
    @Column(name = OBJECT_TYPE_COL_NAME, nullable = false, length = 50)
    private TableConstants.OBJECT_TYPE objectType;

    @Column(name = OBJECT_IDENTIFIER, nullable = false, length = 100)
    private String objectIdentifier; // Composite key

}
