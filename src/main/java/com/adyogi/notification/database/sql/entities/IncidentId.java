package com.adyogi.notification.database.sql.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;

import static com.adyogi.notification.utils.constants.TableConstants.*;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class IncidentId implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentId that = (IncidentId) o;
        return  Objects.equals(alertId, that.alertId) &&
                Objects.equals(clientId, that.clientId) &&
                objectType == that.objectType &&
                Objects.equals(objectIdentifier, that.objectIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId, clientId, objectType, objectIdentifier);
    }
}