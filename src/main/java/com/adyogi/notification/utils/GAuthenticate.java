package com.adyogi.notification.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class GAuthenticate {
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static Bigquery getAuthenticated(InputStream inputStream) {

        Bigquery bigquery = null;

        if ((bigquery = createAuthorizedClient(inputStream)) != null) {
            return bigquery;
        } else {
            return null;
        }
    }

    public static Bigquery createAuthorizedClient(InputStream inputStream) {

        Credential credential;

        if ((credential = authorize(inputStream)) != null) {
            try {
                HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                return new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();

            } catch (GeneralSecurityException e) {
                // TODO Auto-generated catch block
//                logger.error("Google BigQuery Authorization unsuccessful", e);
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
//                logger.error("Google BigQuery Authorization unsuccessful", e);
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }

    }

    public static Credential authorize(InputStream inputStream) {
        GoogleCredential credential = null;
        try {
            credential = GoogleCredential.fromStream(inputStream)
                    .createScoped(BigqueryScopes.all());
            return credential;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
