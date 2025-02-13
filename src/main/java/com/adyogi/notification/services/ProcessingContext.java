package com.adyogi.notification.services;


import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.TriggerConditionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessingContext {
    private Metrics metric;
    private Baseline baseline;
    private TriggerConditionDTO triggerCondition;
}
