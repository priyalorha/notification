//package com.adyogi.notification.database.mongo.entities;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ClientNotificationConfigurationTest {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // Test case for StaticValue deserialization
//    @Test
//    void testStaticValueDeserialization() throws Exception {
//        String json = "{\n" +
//                "  \"objectId\": \"12345\",\n" +
//                "  \"clientId\": \"client_001\",\n" +
//                "  \"name\": \"Sample Notification\",\n" +
//                "  \"triggerConditions\": [\n" +
//                "    {\n" +
//                "      \"metricName\": \"METRIC_1\",\n" +
//                "      \"operator\": \">\",\n" +
//                "      \"value\":  {\n" +
//                "          \"type\": \"STATIC\",\n" +
//                "          \"value\": \"some static value\"\n" +
//                "        }\n" +
//                "    }\n" +
//                "  ],\n" +
//                "  \"status\": \"ACTIVE\",\n" +
//                "  \"message\": \"Notification message\"\n" +
//                "}";
//
//        ClientNotificationConfiguration configuration = objectMapper.readValue(json, ClientNotificationConfiguration.class);
//
//        assertNotNull(configuration);
//        assertEquals("12345", configuration.getObjectId());
//        assertEquals("client_001", configuration.getClientId());
//        assertEquals("Sample Notification", configuration.getName());
//
//        List<ClientNotificationConfiguration.TriggerCondition> triggerConditions = configuration.getTriggerConditions();
//        assertNotNull(triggerConditions);
//        assertEquals(1, triggerConditions.size());
//
//        ClientNotificationConfiguration.TriggerCondition triggerCondition = triggerConditions.get(0);
//        assertEquals("METRIC_1", triggerCondition.getMetricName());
//        assertEquals(">", triggerCondition.getOperator());
//
//        ClientNotificationConfiguration.Value value = triggerCondition.getValue();
//        assertNotNull(value);
//        assertInstanceOf(ClientNotificationConfiguration.StaticValue.class, value);
//
//        ClientNotificationConfiguration.StaticValue staticValue = (ClientNotificationConfiguration.StaticValue) value;
//        assertEquals("STATIC", staticValue.getType().toString());
//        assertEquals("some static value", staticValue.getValue());
//
//    }
//
//    // Test case for StaticIntValue deserialization
//    @Test
//    void testStaticIntValueDeserialization() throws Exception {
//        String json = "{\n" +
//                "  \"objectId\": \"12345\",\n" +
//                "  \"clientId\": \"client_001\",\n" +
//                "  \"name\": \"Sample Notification\",\n" +
//                "  \"triggerConditions\": [\n" +
//                "    {\n" +
//                "      \"metricName\": \"METRIC_2\",\n" +
//                "      \"operator\": \">\",\n" +
//                "      \"value\":  {\n" +
//                "          \"type\": \"STATIC_INT\",\n" +
//                "          \"value\": 10\n" +
//                "        }\n" +
//                "    }\n" +
//                "  ],\n" +
//                "  \"status\": \"ACTIVE\",\n" +
//                "  \"message\": \"Notification message\"\n" +
//                "}";
//
//        ClientNotificationConfiguration configuration = objectMapper.readValue(json, ClientNotificationConfiguration.class);
//
//        assertNotNull(configuration);
//        assertEquals("12345", configuration.getObjectId());
//        assertEquals("client_001", configuration.getClientId());
//        assertEquals("Sample Notification", configuration.getName());
//
//        List<ClientNotificationConfiguration.TriggerCondition> triggerConditions = configuration.getTriggerConditions();
//        assertNotNull(triggerConditions);
//        assertEquals(1, triggerConditions.size());
//
//        ClientNotificationConfiguration.TriggerCondition triggerCondition = triggerConditions.get(0);
//        assertEquals("METRIC_2", triggerCondition.getMetricName());
//        assertEquals(">", triggerCondition.getOperator());
//
//        ClientNotificationConfiguration.Value value = triggerCondition.getValue();
//        assertNotNull(value);
//        assertInstanceOf(ClientNotificationConfiguration.StaticIntValue.class, value);
//
//        ClientNotificationConfiguration.StaticIntValue staticIntValue = (ClientNotificationConfiguration.StaticIntValue) value;
////        assertEquals("STATIC_INT", staticIntValue.getType().toString());
//        assertEquals(10, staticIntValue.getValue());
//    }
//
//    // Test case for DateDynamicValue deserialization
//    @Test
//    void testDateDynamicValueDeserialization() throws Exception {
//        String json = "{\n" +
//                "  \"objectId\": \"12345\",\n" +
//                "  \"clientId\": \"client_001\",\n" +
//                "  \"name\": \"Sample Notification\",\n" +
//                "  \"triggerConditions\": [\n" +
//                "    {\n" +
//                "      \"metricName\": \"METRIC_3\",\n" +
//                "      \"operator\": \">\",\n" +
//                "      \"value\": {\n" +
//                "          \"type\": \"DATE_DYNAMIC\",\n" +
//                "          \"date_offset\": 5\n" +
//                "        }\n" +
//                "    }\n" +
//                "  ],\n" +
//                "  \"status\": \"ACTIVE\",\n" +
//                "  \"message\": \"Notification message\"\n" +
//                "}";
//
//        ClientNotificationConfiguration configuration = objectMapper.readValue(json, ClientNotificationConfiguration.class);
//
//        assertNotNull(configuration);
//        assertEquals("12345", configuration.getObjectId());
//        assertEquals("client_001", configuration.getClientId());
//        assertEquals("Sample Notification", configuration.getName());
//
//        List<ClientNotificationConfiguration.TriggerCondition> triggerConditions = configuration.getTriggerConditions();
//        assertNotNull(triggerConditions);
//        assertEquals(1, triggerConditions.size());
//
//        ClientNotificationConfiguration.TriggerCondition triggerCondition = triggerConditions.get(0);
//        assertEquals("METRIC_3", triggerCondition.getMetricName());
//        assertEquals(">", triggerCondition.getOperator());
//
//        ClientNotificationConfiguration.Value value = triggerCondition.getValue();
//        assertNotNull(value);
//        assertInstanceOf(ClientNotificationConfiguration.DateDynamicValue.class, value);
//
//        ClientNotificationConfiguration.DateDynamicValue dateDynamicValue = (ClientNotificationConfiguration.DateDynamicValue) value;
//        assertEquals("DATE_DYNAMIC", dateDynamicValue.getType().toString());
//        assertEquals(5, dateDynamicValue.getDateOffset());
//    }
//
//    // Test case for full ClientNotificationConfiguration deserialization
//    @Test
//    void testFullClientNotificationConfigurationDeserialization() throws Exception {
//        String json = "{\n" +
//                "  \"objectId\": \"12345\",\n" +
//                "  \"clientId\": \"client_001\",\n" +
//                "  \"name\": \"Sample Notification\",\n" +
//                "  \"triggerConditions\": [\n" +
//                "    {\n" +
//                "      \"metricName\": \"METRIC_4\",\n" +
//                "      \"operator\": \">\",\n" +
//                "      \"value\": {\n" +
//                "        \"type\": \"STATIC\",\n" +
//                "        \"value\": \"some static value\"\n" +
//                "      }\n" +
//                "    }\n" +
//                "  ],\n" +
//                "  \"status\": \"ACTIVE\",\n" +
//                "  \"message\": \"Notification message\",\n" +
//                "  \"alertResendIntervalMin\": 5,\n" +
//                "  \"alertChannel\": [\"email\", \"sms\"],\n" +
//                "  \"createdAt\": \"2023-01-01T12:00:00\",\n" +
//                "  \"updatedAt\": \"2023-01-01T12:00:00\"\n" +
//                "}";
//
//        ClientNotificationConfiguration configuration = objectMapper.readValue(json, ClientNotificationConfiguration.class);
//
//        assertNotNull(configuration);
//        assertEquals("12345", configuration.getObjectId());
//        assertEquals("client_001", configuration.getClientId());
//        assertEquals("Sample Notification", configuration.getName());
//        assertEquals("ACTIVE", configuration.getStatus());
//        assertEquals("Notification message", configuration.getMessage());
//        assertEquals(5, configuration.getAlertResendIntervalMin());
//        assertTrue(configuration.getAlertChannel().contains("email"));
//        assertTrue(configuration.getAlertChannel().contains("sms"));
//
//        List<ClientNotificationConfiguration.TriggerCondition> triggerConditions = configuration.getTriggerConditions();
//        assertNotNull(triggerConditions);
//        assertEquals(1, triggerConditions.size());
//
//        ClientNotificationConfiguration.TriggerCondition triggerCondition = triggerConditions.get(0);
//        assertEquals("METRIC_4", triggerCondition.getMetricName());
//        assertEquals(">", triggerCondition.getOperator());
//
//        ClientNotificationConfiguration.Value value = triggerCondition.getValue();
//        assertNotNull(value);
//        assertInstanceOf(ClientNotificationConfiguration.StaticValue.class, value);
//
//        ClientNotificationConfiguration.StaticValue staticValue = (ClientNotificationConfiguration.StaticValue) value;
//        assertEquals("STATIC", staticValue.getType().toString());
//        assertEquals("some static value", staticValue.getValue());
//    }
//
//    // Additional test case for missing type deserialization
//    @Test
//    void testMissingTypeDeserialization() throws Exception {
//        String json = "{\n" +
//                "  \"value\": \"test\"\n" +
//                "}";
//
//        assertThrows(com.fasterxml.jackson.databind.exc.InvalidTypeIdException.class, () -> objectMapper.readValue(json, ClientNotificationConfiguration.Value.class));
//    }
//
//    // Additional test case for invalid type deserialization
//    @Test
//    void testInvalidTypeDeserialization() throws Exception {
//        String json = "{\n" +
//                "  \"type\": \"INVALID\",\n" +
//                "  \"value\": \"test\"\n" +
//                "}";
//
//        assertThrows(com.fasterxml.jackson.databind.exc.InvalidTypeIdException.class, () -> objectMapper.readValue(json, ClientNotificationConfiguration.Value.class));
//    }
//}