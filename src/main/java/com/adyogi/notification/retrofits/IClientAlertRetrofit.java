package com.adyogi.notification.retrofits;

import com.adyogi.notification.database.mongo.entities.ClientAlert;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IClientAlertRetrofit {
    @Headers({"content-type: application/json"})
    @POST("/classes/ClientNotificationConfiguration")
    Call<ClientAlert> saveConfiguration(@Body ClientAlert clientAlert);

}
