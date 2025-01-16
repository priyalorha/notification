package com.adyogi.notification.services;

import com.adyogi.notification.database.mongo.entities.TriggerCondition;
import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.*;
import com.adyogi.notification.utils.constants.MongoConstants;
import com.adyogi.notification.utils.constants.TableConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.adyogi.notification.utils.constants.TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE;
import static com.adyogi.notification.utils.constants.TableConstants.Operator.EQUALS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluateTriggerConditionTest {

    private EvaluateTriggerCondition evaluator;
    private TriggerConditionDTO triggerCondition;
    private Baseline baseline;
    private Metrics metrics;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        triggerCondition = mock(TriggerConditionDTO.class);
        baseline = mock(Baseline.class);
        metrics = mock(Metrics.class);
        objectMapper = new ObjectMapper();
        evaluator = new EvaluateTriggerCondition(triggerCondition, baseline, metrics);
    }

    @Test
    void testCheckCondition_Equals() {
        assertTrue(evaluator.checkCondition(10, 10, "EQUALS"));
        assertFalse(evaluator.checkCondition(10, 20, "EQUALS"));
    }

    @Test
    void testCheckCondition_GreaterThan() {
        assertTrue(evaluator.checkCondition(20, 10, "GREATER_THAN"));
        assertFalse(evaluator.checkCondition(5, 10, "GREATER_THAN"));
    }

    @Test
    void testCheckCondition_LessThan() {
        assertTrue(evaluator.checkCondition(5, 10, "LESS_THAN"));
        assertFalse(evaluator.checkCondition(20, 10, "LESS_THAN"));
    }

    @Test
    void testCheckCondition_ThrowsExceptionForNullValues() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                evaluator.checkCondition(null, 10, "EQUALS"));
        assertEquals("Values cannot be null", exception.getMessage());
    }

    @Test
    void testEvaluateCondition_StaticValue() throws JsonProcessingException {
        StaticValueDTO staticValueDTO = new StaticValueDTO();
        staticValueDTO.setValue("50");
        staticValueDTO.setCompareWithPrevious(false);

        when(triggerCondition.getValue()).thenReturn(staticValueDTO);
        when(metrics.getValue()).thenReturn("50");
        when(baseline.getValue()).thenReturn("40");
        when(triggerCondition.getOperator()).thenReturn(EQUALS);

        assertTrue(evaluator.evaluateCondition());
    }

//    @Test
//    void testEvaluateCondition_UnsupportedValueType() {
//
//        when(triggerCondition.getValue()).thenReturn("UNKNOWN_TYPE");
//        Exception exception = assertThrows(IllegalArgumentException.class, (Executable) evaluator::evaluateCondition);
//        assertTrue(  exception.getMessage().contains("Unsupported value type: UNKNOWN_TYPE"));
//    }


    @DisplayName("Valid Scenario: testing Metric with Integration Failure")
    @Description("When metric value is true, it should return false")
    @Test
    void testIntegrationFailure() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("INTEGRATION_FAILURE"));
        triggerCondition1.setOperator(TableConstants.Operator.valueOf("EQUALS"));
        StaticBooleanValueDTO staticBooleanValueDTO = new StaticBooleanValueDTO();
        staticBooleanValueDTO.setValue(true);
        staticBooleanValueDTO.setCompareWithPrevious(false);
        staticBooleanValueDTO.setType(MongoConstants.ValueType.STATIC_BOOLEAN);
        triggerCondition1.setValue(staticBooleanValueDTO);

        metrics.setValue("true");
        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }


    @DisplayName("Valid Scenario: testing Metric with Integration Failure")
    @Description("When metric value is true, it should return false")
    @Test
    void testIntegrationFailure1() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("INTEGRATION_FAILURE"));
        triggerCondition1.setOperator(TableConstants.Operator.valueOf("EQUALS"));
        StaticBooleanValueDTO staticBooleanValueDTO = new StaticBooleanValueDTO();
        staticBooleanValueDTO.setValue(true);
        staticBooleanValueDTO.setCompareWithPrevious(false);
        staticBooleanValueDTO.setType(MongoConstants.ValueType.STATIC_BOOLEAN);
        triggerCondition1.setValue(staticBooleanValueDTO);

        when(metrics.getValue()).thenReturn("false"); //
        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }


    @DisplayName("Valid Scenario: testing Metric with MATCH RATE")
    @Description("When match value is below threshold, it should return true")
    @Test
    void testMatchRateWhenMatchRateBelowThreshold() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("MATCH_RATE"));
        triggerCondition1.setOperator(TableConstants.Operator.valueOf("LESS_THAN_EQUAL_TO"));
        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(80);
        percentageValueDTO.setCompareWithPrevious(false);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.MATCH_RATE); // Define mock behavior
        when(metrics.getValue()).thenReturn("75"); // Define mock behavior

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }

    @DisplayName("Valid Scenario: testing Metric with MATCH RATE")
    @Description("When match value is above threshold, it should return false")
    @Test
    void testMatchRateWhenMatchRateAboveThreshold() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("MATCH_RATE"));
        triggerCondition1.setOperator(TableConstants.Operator.valueOf("LESS_THAN_EQUAL_TO"));
        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(80);
        percentageValueDTO.setCompareWithPrevious(false);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.MATCH_RATE); // Define mock behavior
        when(metrics.getValue()).thenReturn("100"); // Define mock behavior

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }

    @DisplayName("Valid Scenario: testing Metric with MATCH_RATE ")
    @Description("When match value is exactly equal to threshold, it should return true")
    @Test
    void testMatchRateWhenMatchRateExactlyEqualThreshold() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("MATCH_RATE"));
        triggerCondition1.setOperator(TableConstants.Operator.valueOf("LESS_THAN_EQUAL_TO"));
        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(80);
        percentageValueDTO.setCompareWithPrevious(false);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.MATCH_RATE); // Define mock behavior
        when(metrics.getValue()).thenReturn("100"); // Define mock behavior

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }


    @DisplayName("Valid Scenario: testing Metric with STOPLOSS_LIMIT_REACHED")
    @Description("When metric value is exactly false, it should return false")
    @Test
    void testStopLossWhenLimitReached() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("STOPLOSS_LIMIT_REACHED"));
        triggerCondition1.setOperator(TableConstants.Operator.valueOf("EQUALS"));
        StaticBooleanValueDTO staticBooleanValueDTO = new StaticBooleanValueDTO();
        staticBooleanValueDTO.setValue(true);
        triggerCondition1.setValue(staticBooleanValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_LIMIT_REACHED); // Define mock behavior
        when(metrics.getValue()).thenReturn("false"); // Define mock behavior

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }


    @DisplayName("Valid Scenario: testing Metric with STOPLOSS_LIMIT_REACHED")
    @Description("When metric value is exactly true, it should return true")
    @Test
    void testStopLossWhenLimitNotReached() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.STOPLOSS_LIMIT_REACHED);
        triggerCondition1.setOperator(EQUALS);
        StaticBooleanValueDTO staticBooleanValueDTO = new StaticBooleanValueDTO();
        staticBooleanValueDTO.setValue(true);
        triggerCondition1.setValue(staticBooleanValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_LIMIT_REACHED); // Define mock behavior
        when(metrics.getValue()).thenReturn("true"); // Define mock behavior

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }



    @DisplayName("Valid Scenario: testing Metric with PRODUCT_SET_COUNT is exactly zero")
    @Description("When metric value is below exactly zero, it should return true")
    @Test
    void testProductSetCountBelowThreshold() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();

        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(EQUALS);
        StaticIntValueDTO staticIntValueDTO = new StaticIntValueDTO();
        staticIntValueDTO.setValue(0);
        staticIntValueDTO.setCompareWithPrevious(true);
        triggerCondition1.setValue(staticIntValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("0");
        when(metrics.getValueDataType()).thenReturn("INTEGER");
        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }

    @DisplayName("Valid Scenario: testing Metric with PRODUCT_SET_COUNT is metric.value below baseline.value")
    @Description("When metric value is below baseline, it should return true")
    @Test
    void testProductSetCountBelowBaseline() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();

        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.LESS_THAN);
        StaticIntValueDTO staticIntValueDTO = new StaticIntValueDTO();
        staticIntValueDTO.setValue(0);
        staticIntValueDTO.setCompareWithPrevious(true);
        triggerCondition1.setValue(staticIntValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("80"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("800");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }

    @DisplayName("Valid Scenario: testing Metric with PRODUCT_SET_COUNT metric.value is above metric.baseline")
    @Description("When metric value is above baseline, it should return false")
    @Test
    void testProductSetCountAboveBaseline() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();

        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.LESS_THAN);

        StaticIntValueDTO staticIntValueDTO = new StaticIntValueDTO();
        staticIntValueDTO.setValue(0);
        staticIntValueDTO.setCompareWithPrevious(true);
        triggerCondition1.setValue(staticIntValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("805"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("800");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }

    @DisplayName("Valid Scenario: testing Metric with PRODUCT_SET_COUNT value equals baseline")
    @Description("When metric value is less than equal baseline, it should return false")
    @Test
    void testProductSetCountLessThanEqualBaseline() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();

        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.LESS_THAN);

        StaticIntValueDTO staticIntValueDTO = new StaticIntValueDTO();
        staticIntValueDTO.setValue(0);
        staticIntValueDTO.setCompareWithPrevious(true);
        triggerCondition1.setValue(staticIntValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("805"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("800");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }



    @DisplayName("Valid Sceniario: testing Metric with PRODUCT_SET_COUNT falls below threshold percentage")
    @Description("When change in product_set_count percentage is below threshold percentage, it should return true")
    @Test
    void testProductSetCountPercentageLessThanBaseLine() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();

        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.LESS_THAN);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("70"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("100");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());

    }


    @DisplayName("Valid Sceniario: testing Metric with PRODUCT_SET_COUNT falls above threshold percentage")
    @Description("When change in product_set_count percentage is above threshold percentage, it should return false")
    @Test
    void testProductSetCountPercentageAboveThanBaseLine() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();

        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.LESS_THAN);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);

        triggerCondition1.setValue(percentageValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("130"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("100");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());

    }


    @DisplayName("Valid Sceniario: testing Metric with PRODUCT_SET_COUNT equals above baseline")
    @Description("When change in product_set_count percentage is zero, it should return true")
    @Test
    void testProductSetCountPercentageEqaulBaseLine() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.LESS_THAN);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);


        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("100"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.PRODUCT_SET_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("100");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());

    }


    @DisplayName("Valid Scenario: testing Metric with DELTA")
    @Description("Significative percent change in STOPLOSS_EXCLUSION_COUNT should return true")
    @Test

    void testDelta() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("STOPLOSS_EXCLUSION_COUNT"));
        triggerCondition1.setOperator(TableConstants.Operator.DELTA);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("100"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("50");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }


    @DisplayName("Valid Scenario: testing Metric with DELTA")
    @Description("Significative percent change in STOPLOSS_EXCLUSION_COUNT, should return true")
    @Test

    void testDelta1() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("STOPLOSS_EXCLUSION_COUNT"));
        triggerCondition1.setOperator(TableConstants.Operator.DELTA);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("50"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");

        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("100");
        when(baseline.getValueDataType()).thenReturn("INTEGER");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }


    @DisplayName("Valid Scenario: testing Metric with DELTA")
    @Description("When delta change for STOPLOSS_EXCLUSION_COUNT is less, should return false")
    @Test
    void testDelta2() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.valueOf("STOPLOSS_EXCLUSION_COUNT"));
        triggerCondition1.setOperator(TableConstants.Operator.DELTA);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("71"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");
        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("100");
        when(baseline.getValueDataType()).thenReturn("INTEGER");
        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }

    @DisplayName("Valid Scenario: testing Metric with DELTA")
    @Description("When delta change for STOPLOSS_EXCLUSION_COUNT is less, should return false")
    @Test
    void testDelta3() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT);
        triggerCondition1.setOperator(TableConstants.Operator.DELTA);

        PercentageValueDTO percentageValueDTO = new PercentageValueDTO();
        percentageValueDTO.setPercentage(30);
        percentageValueDTO.setCompareWithPrevious(true);
        percentageValueDTO.setType(MongoConstants.ValueType.PERCENTAGE);
        triggerCondition1.setValue(percentageValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(metrics.getValue()).thenReturn("70"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("INTEGER");
        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_COUNT); // Define mock behavior
        when(baseline.getValue()).thenReturn("100");
        when(baseline.getValueDataType()).thenReturn("INTEGER");
        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }


    @DisplayName("stoploss Not Excluding Products for Past 3 Days")
    @Description("when STOPLOSS_RAN_DATE is 3days from today, should return true")
    @Test
    void testStoplossExclusionDate() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE);
        triggerCondition1.setOperator(TableConstants.Operator.GREATER_THAN);

        DynamicDayValueDTO dynamicDayValueDTO = new DynamicDayValueDTO();
        dynamicDayValueDTO.setDayOffset(3);
        dynamicDayValueDTO.setCompareWithPrevious(true);
        dynamicDayValueDTO.setType(MongoConstants.ValueType.DATE_DYNAMIC);
        triggerCondition1.setValue(dynamicDayValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE); // Define mock behavior
        when(metrics.getValue()).thenReturn("2025-02-12 12:09:08.055476"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("DATE");


        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE); // Define mock behavior
        when(baseline.getValue()).thenReturn("2025-02-09 12:08:08.055476"); // Define mock behavior
        when(baseline.getValueDataType()).thenReturn("DATE");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertTrue(evaluator.evaluateCondition());
    }


    @DisplayName("stoploss Not Excluding Products for less than 3 Days")
    @Description("when STOPLOSS_RAN_DATE is 3days from today, should return false")
    @Test
    void testStoplossExclusionDat1e() throws Exception {

        TriggerConditionDTO triggerCondition1 = new TriggerConditionDTO();
        triggerCondition1.setMetricName(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE);
        triggerCondition1.setOperator(TableConstants.Operator.GREATER_THAN);

        DynamicDayValueDTO dynamicDayValueDTO = new DynamicDayValueDTO();
        dynamicDayValueDTO.setDayOffset(3);
        dynamicDayValueDTO.setCompareWithPrevious(true);
        dynamicDayValueDTO.setType(MongoConstants.ValueType.DATE_DYNAMIC);
        triggerCondition1.setValue(dynamicDayValueDTO);

        when(metrics.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE); // Define mock behavior
        when(metrics.getValue()).thenReturn("2025-02-05 12:09:08.055476"); // Define mock behavior
        when(metrics.getValueDataType()).thenReturn("DATE");


        when(baseline.getMetricName()).thenReturn(TableConstants.METRIC_NAME.STOPLOSS_EXCLUSION_DATE); // Define mock behavior
        when(baseline.getValue()).thenReturn("2025-02-03 12:09:08.055476"); // Define mock behavior
        when(baseline.getValueDataType()).thenReturn("DATE");

        evaluator = new EvaluateTriggerCondition(triggerCondition1, baseline, metrics);
        assertFalse(evaluator.evaluateCondition());
    }




}

