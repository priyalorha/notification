package com.adyogi.notification.retrofits;

import com.adyogi.notification.database.mongo.entities.AlertChannel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IAlertChannelRetrofit {
    @Headers({"content-type: application/json"})
    @POST("/classes/AlertChannel")
    Call<AlertChannel> postAlertChannel(@Body AlertChannel alertChannel);
}
