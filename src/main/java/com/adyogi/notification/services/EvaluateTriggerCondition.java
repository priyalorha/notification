package com.adyogi.notification.services;

import com.adyogi.notification.database.mongo.entities.*;
import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.*;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.adyogi.notification.utils.constants.ErrorConstants.UNSUPPORTED_VALUE_TYPE;


public class EvaluateTriggerCondition {

    private final TriggerConditionDTO triggerCondition;
    private final Baseline baseline;
    private final Metrics metrics;

//    @Autowired
    private ObjectMapper objectMapper;
    Logger logger= LogUtil.getInstance();

        Map<Class<?>, Function<Object, Boolean>> handlers = Map.of(
            StaticIntValueDTO.class, v -> processStaticIntValue((StaticIntValueDTO) v),
            StaticValueDTO.class, v -> processStaticValue((StaticValueDTO) v),
            PercentageValueDTO.class, v -> processPercentageValue((PercentageValueDTO) v),
            DynamicDayValueDTO.class, v -> processDynamicDay((DynamicDayValueDTO) v),
            StaticBooleanValueDTO.class, v -> processStaticBooleanValue((StaticBooleanValueDTO) v)
    );





    public EvaluateTriggerCondition(TriggerConditionDTO triggerCondition,
                                    Baseline baseline,
                                    Metrics metrics) {
        this.triggerCondition = triggerCondition;
        this.metrics = metrics;
        this.baseline = baseline;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generic method to compare two values using the specified operator.
     * @param metricValue   The value to be compared from metrics.
     * @param baseValue     The baseline value.
     * @param operator      The operator for comparison.
     * @param <T>           The type of values being compared (must extend Comparable).
     * @return true if the condition matches, false otherwise.
     */
    public <T extends Comparable<T>> boolean checkCondition(T metricValue, T baseValue, String operator) {

        logger.info("Comparing values: metricValue: " + metricValue + ", baseValue: " + baseValue + ", operator: " + operator);
        if (metricValue == null || baseValue == null) {
            logger.error("Value is null Comparing values: metricValue: " + metricValue + ", baseValue: " + baseValue + ", operator: " + operator);
            throw new IllegalArgumentException("Values cannot be null");
        }

        switch (operator) {
            case "EQUALS":
                return metricValue.compareTo(baseValue) == 0;
            case "GREATER_THAN_EQUAL_TO":
                return metricValue.compareTo(baseValue) >= 0;
            case "LESS_THAN_EQUAL_TO":
                return metricValue.compareTo(baseValue) <= 0;
            case "GREATER_THAN":
                return metricValue.compareTo(baseValue) > 0;
            case "LESS_THAN":
                return metricValue.compareTo(baseValue) < 0;
            case "NOT_EQUAL":
                return metricValue.compareTo(baseValue) != 0;
            case "DELTA":
                return metricValue.compareTo(baseValue) > 0;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    private boolean processStaticValue(StaticValueDTO staticValue) {
        return staticValue.getCompareWithPrevious() ?
                checkCondition(metrics.getValue(), baseline.getValue(), triggerCondition.getOperator().toString()) :
                checkCondition(metrics.getValue(), staticValue.getValue(), triggerCondition.getOperator().toString());
    }

    private boolean processStaticIntValue(StaticIntValueDTO staticIntValue) {
        if (!metrics.getValueDataType().equalsIgnoreCase("INTEGER")) {
            throw new IllegalArgumentException("Data type mismatch");
        }

        int metricValue = Integer.parseInt(metrics.getValue());
        int baselineValue = baseline.getValue() != null ? Integer.parseInt(baseline.getValue()) : 0;
        int staticValue = staticIntValue.getValue();

        if (!Objects.equals(triggerCondition.getOperator().toString(), "DELTA"))
            return staticIntValue.getCompareWithPrevious() ?
                checkCondition(metricValue, baselineValue, triggerCondition.getOperator().toString()) :
                checkCondition(metricValue, staticValue, triggerCondition.getOperator().toString());
        else
        {
            int change = (metricValue - baselineValue);
            change = Math.abs(change);
            return checkCondition(change, staticIntValue.getValue(), triggerCondition.getOperator().toString() );
        }
    }

    private boolean processPercentageValue(PercentageValueDTO percentageValue) {
        if ("MATCH_RATE".equals(metrics.getMetricName().toString())) {
            return checkCondition(Float.parseFloat(metrics.getValue()),
                    percentageValue.getPercentage(), triggerCondition.getOperator().toString());
        }

        float initial_value = Float.parseFloat(baseline.getValue());
        float changed_value = Float.parseFloat(metrics.getValue());
        float change = (changed_value - initial_value) / initial_value * 100;
        if (!Objects.equals(triggerCondition.getOperator().toString(), "DELTA"))
            return checkCondition(change, percentageValue.getPercentage(), triggerCondition.getOperator().toString());
        else {
            change = Math.abs(change);
            return checkCondition(change, percentageValue.getPercentage(), triggerCondition.getOperator().toString());
        }
    }

    private boolean processDynamicDay(DynamicDayValueDTO dynamicDay) {
        int dateOffset = dynamicDay.getDayOffset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        return checkCondition(LocalDateTime.parse(metrics.getValue(), formatter).minusDays(dateOffset),
                LocalDateTime.parse(baseline.getValue(), formatter), triggerCondition.getOperator().toString());
    }

    private boolean processStaticBooleanValue(StaticBooleanValueDTO staticBooleanValue) {
        boolean metricBooleanValue = Boolean.parseBoolean(metrics.getValue());
        return staticBooleanValue.isValue() == metricBooleanValue;
    }


    private <T> T parseValue(String value, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(value, clazz);
    }


    public Boolean evaluateCondition() { // Changed to return Boolean
        Object value = triggerCondition.getValue();
        Function<Object, Boolean> handler = handlers.get(value.getClass());
        if (handler != null) {
            return handler.apply(value);
        } else {
            logger.error(UNSUPPORTED_VALUE_TYPE + triggerCondition);
            throw new IllegalArgumentException(UNSUPPORTED_VALUE_TYPE + triggerCondition);
        }
    }

}
