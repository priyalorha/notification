//package com.adyogi.notification;
//
//import com.adyogi.notification.controller.ClientMetricsController;
//import com.adyogi.notification.database.sql.entities.Metrics;
//import com.adyogi.notification.entities.MetricsDTO;
//import com.adyogi.notification.exceptions.GlobalExceptionHandler;
//import com.adyogi.notification.services.ClientValidationService;
//import com.adyogi.notification.services.MetricsService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.annotation.Description;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.RequestBuilder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(MockitoExtension.class)
//public class ClientMetricsControllerTest {
//
//    @Mock
//    private ClientValidationService clientValidationService;
//
//    @Mock
//    private MetricsService metricsService;
//
//    @InjectMocks
//    private ClientMetricsController clientMetricsController;
//
//    private MockMvc mockMvc;
//
//    @Mock
//    private MetricsService metricsProcessingService;
//
//
//    @BeforeEach
//    public void setUp() {
//        // Initialize MockMvc with the controller under test
//        mockMvc = MockMvcBuilders.standaloneSetup(clientMetricsController)
//                .setControllerAdvice(new GlobalExceptionHandler()).build();
//
//    }
//
//    @Test
//    @DisplayName("Valid Scenario: Create Metric with Integration Failure")
//    @Description("When metric value is Integration failure, it should return 201 Created status")
//    void createMetric() throws Exception {
//        // Arrange
//        String clientId = "123";
//
//        when(clientValidationService.isClientIdValid(clientId)).thenReturn(true);
//
//        when(metricsService.saveMetrics(any(MetricsDTO.class))).thenReturn(new Metrics("123", "INTEGRATION_FAILURE", "CLIENT_ID", "1", "1", "String",
//                LocalDateTime.of(2024, 12, 12, 0, 0, 0, 0), LocalDateTime.of(2024, 12, 12, 0, 0, 0, 0)));
//
//
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/client/123/metrics")
//                .accept(MediaType.APPLICATION_JSON).content("{\"client_id\":\"123\", \"metric_name\":\"INTEGRATION_FAILURE\", " +
//                        "\"object_type\":\"CLIENT_ID\", " +
//                        "\"object_id\":1, \"value\":1, \"value_datatype\":true}")
//                .contentType(MediaType.APPLICATION_JSON);
//
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//
//        MockHttpServletResponse response = result.getResponse();
//        assertEquals(response.getStatus(), HttpStatus.CREATED.value());
//
//        assertEquals(response.getContentAsString(),
//                "{\"client_id\":\"123\",\"metric_name\":\"INTEGRATION_FAILURE\",\"object_type\":\"CLIENT_ID\",\"object_id\":\"1\",\"value\":\"1\",\"value_datatype\":\"String\",\"created_at\":\"2024-12-12T00:00\",\"updated_at\":\"2024-12-12T00:00\"}");
//    }
//
//    @Test
//    @DisplayName("Invalid Scenario: Invalid ClientID")
//    @Description("When client ID does not exist, it should return 404 Not Found status")
//    void createInvalidClientId() throws Exception {
//        // Arrange
//        String clientId = "123";
//
//        when(clientValidationService.isClientIdValid(clientId)).thenReturn(false);
//
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/client/123/metrics")
//                .accept(MediaType.APPLICATION_JSON).content("{\"client_id\":\"123\", \"metric_name\":\"INTEGRATION_FAILURE\", " +
//                        "\"object_type\":\"CLIENT_ID\", " +
//                        "\"object_id\":1, \"value\":1, \"value_datatype\":true}")
//                .contentType(MediaType.APPLICATION_JSON);
//
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//
//        MockHttpServletResponse response = result.getResponse();
//
//        assertEquals(response.getStatus(), HttpStatus.NOT_FOUND.value());
//
//        assertEquals(response.getContentAsString(),
//                "{\"errors\":[\"Client ID not found.\"]}");
//    }
//
//    @Test
//    @DisplayName("Invalid Scenario: Client ID Mismatch")
//    @Description("When client ID in path is different from client ID in request body, it should return 400 Bad Request status")
//    void createOrUpdateMetrics_ClientIdMismatch() throws Exception {
//        // Arrange
//        String clientId = "456";
//
//
//
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/client/456/metrics")
//                .accept(MediaType.APPLICATION_JSON).content("{\"client_id\":\"123\", \"metric_name\":\"INTEGRATION_FAILURE\", " +
//                        "\"object_type\":\"CLIENT_ID\", " +
//                        "\"object_id\":1, \"value\":1, \"value_datatype\":true}")
//                .contentType(MediaType.APPLICATION_JSON);
//
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//
//        MockHttpServletResponse response = result.getResponse();
//
//        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
//
//        assertEquals(response.getContentAsString(),
//                "{\"errors\":[\"Client ID in path and body do not match.\"]}");
//    }
//
//
//    @Test
//    @DisplayName("Invalid Scenario: Missing Request Body")
//    @Description("When the request body is missing required fields, it should return 400 Bad Request with error messages")
//    void createOrUpdateMetrics_MissingRequestBody() throws Exception {
//        // Arrange
//        String clientId = "456";
//
//        List<String> expectedResponseList = Arrays.asList(
//                "Value Data Type is required",
//                "Client ID is required",
//                "Object Type is required",
//                "Object ID is required",
//                "Metrics Name is required",
//                "Value is required"
//        );
//
//
//
//        // Act
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//                .post("/client/{client_id}/metrics", clientId) // Use clientId dynamically
//                .accept(MediaType.APPLICATION_JSON)
//                .content("{}") // Missing required fields
//                .contentType(MediaType.APPLICATION_JSON);
//
//
//        mockMvc.perform(requestBuilder)
//                .andDo(print()) // Prints the response body to the console for debugging
//                .andExpect(status().isBadRequest());
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//        MockHttpServletResponse response = result.getResponse();
//
//        assertEquals(response.getContentAsString(),
//                "{\"errors\":[\"Object Type is required\",\"Object ID is required\",\"Client ID is required\",\"Metrics Name is required\",\"Value Data Type is required\",\"Value is required\"]}");
//
//
//    }
//
//}
//
//
//
//
