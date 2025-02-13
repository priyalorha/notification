package com.adyogi.notification.database.mongo.entities;

import com.adyogi.notification.utils.constants.TableConstants;
import lombok.*;

import javax.validation.Valid;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Valid
public class TriggerCondition {
    private TableConstants.METRIC metricName;
    private TableConstants.Operator operator;
    private String value;

}

