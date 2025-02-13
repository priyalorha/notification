package com.adyogi.notification.services;

import com.adyogi.notification.database.sql.entities.Baseline;
import com.adyogi.notification.database.sql.entities.Metrics;
import com.adyogi.notification.dto.*;
import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.BiFunction;

import static com.adyogi.notification.utils.constants.ErrorConstants.UNSUPPORTED_VALUE_TYPE;


@Service
public class EvaluateTriggerCondition {

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LogUtil.getInstance();

    private final Map<Class<?>, BiFunction<Object, ProcessingContext, Boolean>> handlers = Map.of(
            StaticIntValueDTO.class, (v, context) -> processStaticIntValue((StaticIntValueDTO) v, context),
            StaticValueDTO.class, (v, context) -> processStaticValue((StaticValueDTO) v, context),
            PercentageValueDTO.class, (v, context) -> processPercentageValue((PercentageValueDTO) v, context),
            DynamicDayValueDTO.class, (v, context) -> processDynamicDay((DynamicDayValueDTO) v, context),
            StaticFloatValueDTO.class, (v, context) -> processStaticFloatValue((StaticFloatValueDTO) v, context),
            StaticBooleanValueDTO.class, (v, context) -> processStaticBooleanValue((StaticBooleanValueDTO) v, context)

    );

    public <T extends Comparable<T>> boolean checkCondition(T metricValue, T baseValue, String operator) {
        logger.info("Comparing values: metricValue: {}, baseValue: {}, operator: {}", metricValue, baseValue, operator);

        if (metricValue == null || baseValue == null) {
            logger.error("Value is null. Comparing values: metricValue: {}, baseValue: {}, operator: {}", metricValue, baseValue, operator);
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

    private boolean processStaticValue(StaticValueDTO staticValue, ProcessingContext context) {
        return staticValue.getCompareWithPrevious() ?
                checkCondition(context.getMetric().getValue(), context.getBaseline().getValue(), context.getTriggerCondition().getOperator().toString()) :
                checkCondition(context.getMetric().getValue(), staticValue.getValue(), context.getTriggerCondition().getOperator().toString());
    }

    private boolean processStaticIntValue(StaticIntValueDTO staticIntValue, ProcessingContext context) {
        if (!"INTEGER".equalsIgnoreCase(context.getMetric().getValueDataType())) {
            throw new IllegalArgumentException("Data type mismatch");
        }

        int metricValue = Integer.parseInt(context.getMetric().getValue());
        int baselineValue = context.getBaseline().getValue() != null ? Integer.parseInt(context.getBaseline().getValue()) : 0;
        int staticValue = staticIntValue.getValue();

        if (!"DELTA".equals(context.getTriggerCondition().getOperator().toString())) {
            return staticIntValue.getCompareWithPrevious() ?
                    checkCondition(metricValue, baselineValue, context.getTriggerCondition().getOperator().toString()) :
                    checkCondition(metricValue, staticValue, context.getTriggerCondition().getOperator().toString());
        } else {
            int change = Math.abs(metricValue - baselineValue);
            return checkCondition(change, staticIntValue.getValue(), context.getTriggerCondition().getOperator().toString());
        }
    }

    private boolean processStaticFloatValue(StaticFloatValueDTO staticFloatValue, ProcessingContext context) {
        if (!"FLOAT".equalsIgnoreCase(context.getMetric().getValueDataType())) {
            throw new IllegalArgumentException("Data type mismatch");
        }

        float metricValue = Float.parseFloat(context.getMetric().getValue());
        float baselineValue = context.getBaseline().getValue() != null ? Float.parseFloat(context.getBaseline().getValue()) : 0;
        float staticValue = staticFloatValue.getValue();

        if (!"DELTA".equals(context.getTriggerCondition().getOperator().toString())) {
            return staticFloatValue.getCompareWithPrevious() ?
                    checkCondition(metricValue, baselineValue, context.getTriggerCondition().getOperator().toString()) :
                    checkCondition(metricValue, staticValue, context.getTriggerCondition().getOperator().toString());
        } else {
            float change = Math.abs(metricValue - baselineValue);
            return checkCondition(change, staticFloatValue.getValue(), context.getTriggerCondition().getOperator().toString());
        }
    }

    private boolean processPercentageValue(PercentageValueDTO percentageValue, ProcessingContext context) {
        if ("MATCH_RATE".equals(context.getMetric().getMetric().toString())) {
            return checkCondition(Float.parseFloat(context.getMetric().getValue()),
                    percentageValue.getPercentage(), context.getTriggerCondition().getOperator().toString());
        }

        float initial_value = Float.parseFloat(context.getBaseline().getValue());
        float changed_value = Float.parseFloat(context.getMetric().getValue());
        float change = (changed_value - initial_value) / initial_value * 100;

        if (!"DELTA".equals(context.getTriggerCondition().getOperator().toString())) {
            return checkCondition(change, percentageValue.getPercentage(), context.getTriggerCondition().getOperator().toString());
        } else {
            return checkCondition(Math.abs(change), percentageValue.getPercentage(), context.getTriggerCondition().getOperator().toString());
        }
    }

    private boolean processDynamicDay(DynamicDayValueDTO dynamicDay, ProcessingContext context) {
        int dateOffset = dynamicDay.getDayOffset();

        LocalDateTime metricTime = Instant.parse(context.getMetric().getValue())
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();

        LocalDateTime baselineTime = Instant.parse(context.getBaseline().getValue())
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();

        return checkCondition(metricTime.minusDays(dateOffset), baselineTime, context.getTriggerCondition().getOperator().toString());
    }

    private boolean processStaticBooleanValue(StaticBooleanValueDTO staticBooleanValue, ProcessingContext context) {
        boolean metricBooleanValue = Boolean.parseBoolean(context.getMetric().getValue());
        return staticBooleanValue.isValue() == metricBooleanValue;
    }

    private <T> T parseValue(String value, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(value, clazz);
    }

    public Boolean evaluateCondition(TriggerConditionDTO triggerCondition, Metrics metrics, Baseline baseline) {
        Object value = triggerCondition.getValue();
        ProcessingContext context = new ProcessingContext(metrics, baseline, triggerCondition);

        BiFunction<Object, ProcessingContext, Boolean> handler = handlers.get(value.getClass());
        if (handler != null) {
            return handler.apply(value, context);
        } else {
            logger.error("{} {}", UNSUPPORTED_VALUE_TYPE, triggerCondition);
            throw new IllegalArgumentException(UNSUPPORTED_VALUE_TYPE + triggerCondition);
        }
    }
}
