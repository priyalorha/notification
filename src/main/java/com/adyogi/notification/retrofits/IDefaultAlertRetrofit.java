package com.adyogi.notification.retrofits;

import com.adyogi.notification.database.mongo.entities.DefaultAlert;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IDefaultAlertRetrofit {

    @Headers({"content-type: application/json"})
    @POST("/classes/DefaultAlert")
    Call<DefaultAlert> saveConfiguration(@Body DefaultAlert defaultAlert);

}
