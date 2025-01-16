//package com.adyogi.notification.services;
//
//import static com.adyogi.notification.utils.constants.TableConstants.BIGQUERY_INCIDENTS_TABLE_NAME;
//import static com.adyogi.notification.utils.constants.TableConstants.BIGQUERY_NOTIFICATION_DATASET_NAME;
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.adyogi.notification.bigquery.bigquerycomponent.BigQueryConfiguration;
//import com.adyogi.notification.database.mongo.entities.ClientNotificationConfiguration;
//import com.adyogi.notification.database.mongo.entities.DefaultNotificationConfiguration;
//import com.adyogi.notification.database.mongo.entities.PercentageValue;
//import com.adyogi.notification.database.mongo.entities.TriggerCondition;
//import com.adyogi.notification.database.sql.entities.Baseline;
//import com.adyogi.notification.database.sql.entities.Incident;
//import com.adyogi.notification.database.sql.entities.Metrics;
//import com.adyogi.notification.repositories.back4app.ClientNotificationConfigurationRepository;
//import com.adyogi.notification.repositories.back4app.DefaultNotificationConfigurationRepository;
//import com.adyogi.notification.repositories.mysql.IBaselineRepository;
//import com.adyogi.notification.repositories.mysql.IncidentRepository;
//import com.adyogi.notification.utils.constants.MongoConstants;
//import com.adyogi.notification.utils.constants.TableConstants;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.util.*;
//
//public class MetricTriggerServiceTest {
//
//    @Mock private IBaselineRepository baselineRepository;
//    @Mock private ClientNotificationConfigurationRepository clientNotificationConfigurationRepository;
//    @Mock private DefaultNotificationConfigurationRepository defaultNotificationConfigurationRepository;
//    @Mock private BigQueryConfiguration bigQueryConfiguration;
//    @Mock private IncidentRepository incidentRepository;
//    @InjectMocks private MetricTriggerService metricTriggerService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testFetchClientNotification() {
//        // Given
//        String clientId = "client123";
//        ClientNotificationConfiguration mockNotification = new ClientNotificationConfiguration();
//        when(clientNotificationConfigurationRepository.findActiveAlertsByClientId(clientId))
//                .thenReturn(Collections.singletonList(mockNotification));
//
//        // When
//        List<ClientNotificationConfiguration> result = metricTriggerService.fetchClientNotification(clientId);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(clientNotificationConfigurationRepository, times(1)).findActiveAlertsByClientId(clientId);
//    }
//
//    @Test
//    public void testFetchBaselinesForMetric() {
//        // Given
//        Metrics mockMetrics = new Metrics();
//        mockMetrics.setClientId("client123");
//        mockMetrics.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockMetrics.setObjectId("client123");
//        mockMetrics.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//        mockMetrics.setValue("100");
//
//        Baseline mockBaseline = new Baseline();
//        mockBaseline.setAlertId("alert1");
//        mockBaseline.setClientId("client123");
//        mockBaseline.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockBaseline.setObjectId("client123");
//        mockBaseline.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        Incident mockIncident = new Incident();
//        mockIncident.setIncidentStatus(TableConstants.INCIDENT_STATUS.OPEN);
//
//        mockIncident.setAlertId("alert1");
//        mockIncident.setClientId("client123");
//        mockIncident.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockIncident.setObjectId("client123");
//        mockIncident.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        ClientNotificationConfiguration mockNotificationConfig = new ClientNotificationConfiguration();
//        mockNotificationConfig.setObjectId("client123");
//        mockNotificationConfig.setClientId("client123");
//
//        when(baselineRepository.findBaselineByIdExceptAlertId(anyString(), any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class), anyString()))
//                .thenReturn(Collections.singletonList(mockBaseline));
//
//        // When
//        List<Baseline> result = metricTriggerService.fetchBaselinesForMetric(mockMetrics);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(baselineRepository, times(1)).findBaselineByIdExceptAlertId(anyString(), any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class), anyString());
//    }
//
//    @Test
//    public void testInsertIncidentToBigQuery() {
//        // Given
//        Incident mockIncident = new Incident();
//        when(bigQueryConfiguration.insertRows(anyList(), eq(BIGQUERY_NOTIFICATION_DATASET_NAME), eq(BIGQUERY_INCIDENTS_TABLE_NAME)))
//                .thenReturn(null);
//
//        // When
//        metricTriggerService.insertIncidentToBigQuery(mockIncident);
//
//        // Then
//        verify(bigQueryConfiguration, times(1)).insertRows(anyList(), eq(BIGQUERY_NOTIFICATION_DATASET_NAME), eq(BIGQUERY_INCIDENTS_TABLE_NAME));
//    }
//
//    @Test
//    public void testProcessNewMetric_CreateIncident() throws JsonProcessingException {
//        // Given
//        Metrics mockMetrics = new Metrics();
//        mockMetrics.setClientId("client123");
//        mockMetrics.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockMetrics.setObjectId("client123");
//        mockMetrics.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//        mockMetrics.setValue("80");
//
//        Baseline mockBaseline = new Baseline();
//        mockBaseline.setAlertId("alert1");
//        mockBaseline.setClientId("client123");
//        mockBaseline.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockBaseline.setObjectId("client123");
//        mockBaseline.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        Incident mockIncident = new Incident();
//        mockIncident.setIncidentStatus(TableConstants.INCIDENT_STATUS.OPEN);
//
//        mockIncident.setAlertId("alert1");
//        mockIncident.setClientId("client123");
//        mockIncident.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockIncident.setObjectId("client123");
//        mockIncident.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        ClientNotificationConfiguration mockNotificationConfig = new ClientNotificationConfiguration();
//        mockNotificationConfig.setObjectId("client123");
//        mockNotificationConfig.setClientId("client123");
//        TriggerCondition triggerConditions = new TriggerCondition();
//
//        triggerConditions.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        triggerConditions.setOperator(TableConstants.Operator.LESS_THAN_EQUAL_TO);
//
//        PercentageValue percentageValue = new PercentageValue();
//        percentageValue.setPercentage(80);
//        percentageValue.setType(MongoConstants.ValueType.PERCENTAGE);
//
//        triggerConditions.setValue(percentageValue);
//
//        List <TriggerCondition> t =new ArrayList<>();
//        t.add(triggerConditions);
//
//        mockNotificationConfig.setTriggerConditions(t);
//
//
//
//        when(baselineRepository.findBaselineByIdExceptAlertId(
//                anyString(),
//                any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class),
//                anyString()))
//                .thenReturn(Collections.singletonList(mockBaseline));
//
//
//        when(clientNotificationConfigurationRepository.findActiveAlertsByClientId(anyString()))
//                .thenReturn(Collections.singletonList(mockNotificationConfig));
//
//
//
//        when(incidentRepository.findByAlertIdAndClientIdAndMetricNameAndObjectTypeAndObjectIdAndIncidentStatus(
//                mockBaseline.getAlertId(), mockBaseline.getClientId(), mockBaseline.getMetricName(),
//                mockBaseline.getObjectType(), mockBaseline.getObjectId()))
//                .thenReturn(Optional.empty());
//
//
//
//
//        TriggerConditionProcessingService mockProcessingService = mock(TriggerConditionProcessingService.class);
//        when(mockProcessingService.processTriggerCondition()).thenReturn(true);
//        // When
//        metricTriggerService.processNewMetric(mockMetrics);
//
//        // Then
//        verify(incidentRepository, times(1)).save(any(Incident.class));
//    }
//
//    @Test
//    public void testProcessNewMetric_IncidentAlreadyClosed() throws JsonProcessingException {
//        // Given
//        Metrics mockMetrics = new Metrics();
//        mockMetrics.setClientId("client123");
//        mockMetrics.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockMetrics.setObjectId("client123");
//        mockMetrics.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//        mockMetrics.setValue("90");
//
//        Baseline mockBaseline = new Baseline();
//        mockBaseline.setAlertId("alert1");
//        mockBaseline.setClientId("client123");
//        mockBaseline.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockBaseline.setObjectId("client123");
//        mockBaseline.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        Incident mockIncident = new Incident();
//        mockIncident.setIncidentStatus(TableConstants.INCIDENT_STATUS.OPEN);
//
//        mockIncident.setAlertId("alert1");
//        mockIncident.setClientId("client123");
//        mockIncident.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockIncident.setObjectId("client123");
//        mockIncident.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//
//        ClientNotificationConfiguration mockNotificationConfig = new ClientNotificationConfiguration();
//        mockNotificationConfig.setObjectId("client123");
//        mockNotificationConfig.setClientId("client123");
//        TriggerCondition triggerConditions = new TriggerCondition();
//
//        triggerConditions.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        triggerConditions.setOperator(TableConstants.Operator.LESS_THAN_EQUAL_TO);
//
//        PercentageValue percentageValue = new PercentageValue();
//        percentageValue.setPercentage(80);
//        percentageValue.setType(MongoConstants.ValueType.PERCENTAGE);
//
//        triggerConditions.setValue(percentageValue);
//
//        List <TriggerCondition> t =new ArrayList<>();
//        t.add(triggerConditions);
//
//        mockNotificationConfig.setTriggerConditions(t);
//
//
//        mockIncident.setIncidentStatus(TableConstants.INCIDENT_STATUS.RESOLVED);
//
//
//        when(baselineRepository.findBaselineByIdExceptAlertId(anyString(),
//                any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class), anyString()))
//                .thenReturn(Collections.singletonList(mockBaseline));
//
//        when(clientNotificationConfigurationRepository.findActiveAlertsByClientId(anyString()))
//                .thenReturn(Collections.singletonList(mockNotificationConfig));
//
//        when(incidentRepository.findByAlertIdAndClientIdAndMetricNameAndObjectTypeAndObjectId(anyString(), anyString(),
//                any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class), anyString()))
//                .thenReturn(Optional.of(mockIncident));
//
//        TriggerConditionProcessingService mockProcessingService = mock(TriggerConditionProcessingService.class);
//        when(mockProcessingService.processTriggerCondition()).thenReturn(false); // Assume true for the test
//
//
//        // When
//        metricTriggerService.processNewMetric(mockMetrics);
//
//        assertEquals(TableConstants.INCIDENT_STATUS.RESOLVED, mockIncident.getIncidentStatus());
//        verify(incidentRepository, times(0)).save(mockIncident);
//        verify(bigQueryConfiguration, times(0)).insertRows(anyList(), eq(BIGQUERY_NOTIFICATION_DATASET_NAME), eq(BIGQUERY_INCIDENTS_TABLE_NAME));
//    }
//
//    @Test
//    public void testProcessNewMetric_ResolvedIncident() throws JsonProcessingException {
//        // Given
//        Metrics mockMetrics = new Metrics();
//        mockMetrics.setClientId("client123");
//        mockMetrics.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockMetrics.setObjectId("client123");
//        mockMetrics.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//        mockMetrics.setValue("75");
//
//        Baseline mockBaseline = new Baseline();
//        mockBaseline.setAlertId("alert1");
//        mockBaseline.setClientId("client123");
//        mockBaseline.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockBaseline.setObjectId("client123");
//        mockBaseline.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        Incident mockIncident = new Incident();
//        mockIncident.setIncidentStatus(TableConstants.INCIDENT_STATUS.OPEN);
//
//        mockIncident.setAlertId("alert1");
//        mockIncident.setClientId("client123");
//        mockIncident.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        mockIncident.setObjectId("client123");
//        mockIncident.setObjectType(TableConstants.OBJECT_TYPE.CLIENT_ID);
//
//        ClientNotificationConfiguration mockNotificationConfig = new ClientNotificationConfiguration();
//        mockNotificationConfig.setObjectId("client123");
//        mockNotificationConfig.setClientId("client123");
//        TriggerCondition triggerConditions = new TriggerCondition();
//
//        triggerConditions.setMetricName(TableConstants.METRIC_NAME.MATCH_RATE);
//        triggerConditions.setOperator(TableConstants.Operator.LESS_THAN_EQUAL_TO);
//
//        PercentageValue percentageValue = new PercentageValue();
//        percentageValue.setPercentage(70);
//        percentageValue.setType(MongoConstants.ValueType.PERCENTAGE);
//
//        triggerConditions.setValue(percentageValue);
//
//        List <TriggerCondition> t =new ArrayList<>();
//        t.add(triggerConditions);
//
//        mockNotificationConfig.setTriggerConditions(t);
//
//
//
//        when(baselineRepository.findBaselineByIdExceptAlertId(
//                anyString(),
//                any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class),
//                anyString()))
//                .thenReturn(Collections.singletonList(mockBaseline));
//
//
//        when(clientNotificationConfigurationRepository.findActiveAlertsByClientId(anyString()))
//                .thenReturn(Collections.singletonList(mockNotificationConfig));
//
//        when(incidentRepository.findByAlertIdAndClientIdAndMetricNameAndObjectTypeAndObjectIdAndIncidentStatus(
//                anyString(), anyString(),
//                any(TableConstants.METRIC_NAME.class),
//                any(TableConstants.OBJECT_TYPE.class), anyString()))
//                .thenReturn(Optional.of(mockIncident));
//
//
//
//        TriggerConditionProcessingService mockProcessingService = mock(TriggerConditionProcessingService.class);
//        when(mockProcessingService.processTriggerCondition()).thenReturn(true); // Assume true for the test
//
//
//        // When
//        metricTriggerService.processNewMetric(mockMetrics);
//
//        assertEquals(TableConstants.INCIDENT_STATUS.RESOLVED, mockIncident.getIncidentStatus());
//        verify(incidentRepository, times(1)).save(mockIncident);
//        verify(bigQueryConfiguration, times(1)).insertRows(anyList(), eq(BIGQUERY_NOTIFICATION_DATASET_NAME), eq(BIGQUERY_INCIDENTS_TABLE_NAME));
//    }
//}
