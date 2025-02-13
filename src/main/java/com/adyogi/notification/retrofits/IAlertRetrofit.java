package com.adyogi.notification.retrofits;

import com.adyogi.notification.database.mongo.entities.Alert;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IAlertRetrofit {
    @Headers({"content-type: application/json"})
    @POST("/classes/Alert")
    Call<Alert> saveConfiguration(@Body Alert alert);

}
