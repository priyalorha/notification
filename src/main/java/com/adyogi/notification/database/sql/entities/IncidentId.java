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

    @Column(name = ALERT_ID)
    private String alertId;
    @Column(name = CLIENT_ID)
    private String clientId;
    @Enumerated(EnumType.STRING)
    @Column(name = OBJECT_TYPE_COL_NAME)
    private TableConstants.OBJECT_TYPE objectType;
    private String objectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentId that = (IncidentId) o;
        return  Objects.equals(alertId, that.alertId) &&
                Objects.equals(clientId, that.clientId) &&
                objectType == that.objectType &&
                Objects.equals(objectId, that.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId, clientId, objectType, objectId);
    }
}