package com.adyogi.notification.database.sql.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;

import static com.adyogi.notification.utils.constants.TableConstants.*;
import static com.adyogi.notification.utils.constants.TableConstants.OBJECT_TYPE_COL_NAME;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class BaselineId implements Serializable {

    @Column(name = ALERT_ID, nullable = false, length = 50)
    private String alertId;


    @Column(name = CLIENT_ID, nullable = false, length = 50)
    private String clientId;


    @Column(name = METRIC_COL_NAME, nullable = false, length = 50)
    @Enumerated(EnumType.STRING) // Map ENUM as string
    private METRIC metric;


    @Column(name = OBJECT_TYPE_COL_NAME, nullable = false, length = 50)
    @Enumerated(EnumType.STRING) // Map ENUM as string

    private TableConstants.OBJECT_TYPE objectType;

    @Column(name = OBJECT_IDENTIFIER, nullable = false, length = 100)
    private String objectIdentifier; // Composite key

}
