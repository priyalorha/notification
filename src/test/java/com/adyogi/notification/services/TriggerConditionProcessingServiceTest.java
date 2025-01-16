//package com.adyogi.notification.services;
//
//import com.adyogi.notification.database.sql.entities.Baseline;
//import com.adyogi.notification.database.sql.entities.Metrics;
//import com.adyogi.notification.database.mongo.entities.*;
//import com.adyogi.notification.utils.constants.TableConstants;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class TriggerConditionProcessingServiceTest {
//
//    @Mock
//    private TriggerCondition triggerCondition;
//    @Mock
//    private Baseline baseline;
//    @Mock
//    private Metrics metrics;
//
//    private TriggerConditionProcessingService service;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        service = new TriggerConditionProcessingService(triggerCondition, baseline, metrics);
//    }
//
//    @Test
//    void testProcessStaticValue() throws JsonProcessingException {
//        StaticValue staticValue = new StaticValue();
//        staticValue.setCompareWithPrevious(true);
//        staticValue.setValue(String.valueOf(100));
//
//        when(triggerCondition.getValue()).thenReturn(staticValue);
//        when(metrics.getValue()).thenReturn("100");
//        when(baseline.getValue()).thenReturn("100");
//        when(triggerCondition.getOperator()).thenReturn(TableConstants.Operator.EQUALS);
//
//        boolean result = service.processTriggerCondition();
//
//        assertTrue(result);
//    }
//
//    @Test
//    void testProcessStaticIntValue() throws JsonProcessingException {
//        StaticIntValue staticIntValue = new StaticIntValue();
//        staticIntValue.setCompareWithPrevious(true);
//        staticIntValue.setValue(100);
//
//        when(triggerCondition.getValue()).thenReturn(staticIntValue);
//        when(metrics.getValue()).thenReturn("100");
//        when(baseline.getValue()).thenReturn("100");
//        when(triggerCondition.getOperator()).thenReturn(TableConstants.Operator.EQUALS);
//        when(metrics.getValueDataType()).thenReturn("INTEGER");
//
//        boolean result = service.processTriggerCondition();
//
//        assertTrue(result);
//    }
//
//    @Test
//    void testProcessPercentageValue() throws JsonProcessingException {
//        PercentageValue percentageValue = new PercentageValue();
//        percentageValue.setPercentage(80);
//
////        metrics.setValue("MATCH_RATE");
//
//        when(triggerCondition.getValue()).thenReturn(percentageValue);
//        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.MATCH_RATE);
//        when(metrics.getValue()).thenReturn("70");
//        when(baseline.getValue()).thenReturn("60");
//        when(triggerCondition.getOperator()).thenReturn(TableConstants.Operator.LESS_THAN);
//
//        assertTrue(service.processTriggerCondition());
//
//
//
//        when(metrics.getValue()).thenReturn("90");
//        assertFalse(service.processTriggerCondition());
//
//
//        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.INTEGRATION_FAILURE);
//        assertTrue(service.processTriggerCondition());
//    }
//
//    @Test
//    void testProcessDynamicDay() throws JsonProcessingException {
//        DynamicDay dynamicDay = new DynamicDay();
//        dynamicDay.setDayOffset(2);
//
//        when(triggerCondition.getValue()).thenReturn(dynamicDay);
//        when(metrics.getValue()).thenReturn("2025-01-26T12:00:00");
//        when(baseline.getValue()).thenReturn("2025-01-24T12:00:00");
//        when(triggerCondition.getOperator()).thenReturn(TableConstants.Operator.LESS_THAN);
//
//        boolean result = service.processTriggerCondition();
//
//        assertFalse(result);
//    }
//
//    @Test
//    void testProcessStaticBooleanValue() throws JsonProcessingException {
//        StaticBooleanValue staticBooleanValue = new StaticBooleanValue();
//        staticBooleanValue.setValue(true);
//
//        when(triggerCondition.getValue()).thenReturn(staticBooleanValue);
//        when(metrics.getValue()).thenReturn("true");
//        when(triggerCondition.getOperator()).thenReturn(TableConstants.Operator.EQUALS);
//
//        boolean result = service.processTriggerCondition();
//
//        assertTrue(result);
//    }
//
//    @Test
//    void testUnsupportedValueType() {
//        // Mock an unsupported value type (using Object here to simulate an unknown type)
//        Value value = new Value();
//
//        when(triggerCondition.getValue()).thenReturn(new Value());
//
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            service.processTriggerCondition();
//        });
//
//        assertEquals("Unsupported value type: " + value.getClass().getName(), exception.getMessage());
//    }
//}
