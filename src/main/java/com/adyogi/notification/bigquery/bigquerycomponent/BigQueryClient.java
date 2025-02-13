package com.adyogi.notification.bigquery.bigquerycomponent;

import com.adyogi.notification.utils.logging.LogUtil;
import com.adyogi.notification.utils.rollbar.RollbarManager;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.adyogi.notification.utils.constants.ErrorConstants.INSERT_ERRORS_MESSAGE;

@Component
public class BigQueryClient {
    private BigQuery bigQuery = null;
    Logger logger= LogUtil.getInstance();


    @Value("${BIGQUERY_SERVICE_ACCOUNT_CREDENTIALS}")
    private String bqCredentials;

    public BigQuery getBigQueryConnection() {
        if (bigQuery == null) {
            postConstruct();
        }
        return bigQuery;

    }

    @PostConstruct
    private void postConstruct() {
        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(bqCredentials.getBytes(StandardCharsets.UTF_8))) {
            bigQuery = BigQueryOptions.newBuilder().setCredentials(
                    ServiceAccountCredentials.fromStream(inputStream)).build().getService();
        } catch (IOException e) {
            RollbarManager.sendExceptionOnCriticalRollBar("Error in post construct of BQ object ", e);
        }
    }

    public TableResult executeQuery(QueryJobConfiguration queryJobConfiguration) throws InterruptedException {
        return bigQuery.query(queryJobConfiguration);
    }

    public InsertAllResponse insertRows(List<InsertAllRequest.RowToInsert> rowsToInsert, String datasetId, String tableId) {
        InsertAllRequest request = InsertAllRequest.newBuilder(datasetId, tableId)
                .setRows(rowsToInsert)
                .build();

        InsertAllResponse status  = bigQuery.insertAll(request);
        if (status.hasErrors()) {
            logger.error(INSERT_ERRORS_MESSAGE + status.getInsertErrors());
        }
        return status;

    }

}
