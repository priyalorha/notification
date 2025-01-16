package com.adyogi.notification.retrofits;

import com.adyogi.notification.dto.ParseClientDTO;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface IAdyogiAdsApiRetrofit {
    @GET("/clients")
    Call<List<ParseClientDTO>> getClients();
}

