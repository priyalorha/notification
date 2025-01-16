package com.adyogi.notification.services;

import com.adyogi.notification.dto.ParseClientDTO;
import com.adyogi.notification.retrofits.IAdyogiAdsApiRetrofit;
import com.adyogi.notification.retrofits.RetrofitClient;
import com.adyogi.notification.utils.logging.LogUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.adyogi.notification.utils.constants.ErrorConstants.ERROR_FAILED_API_CALL;

@Service
public class ClientIdListingService {

    Logger logger= LogUtil.getInstance();

    @Value("${adyogi.api.baseUrl}")
    private String baseUrl; // Inject base URL via Spring configuration

    @Cacheable(value = "clientListCache")
    public List<String> getClientList() {
        // Make the API call

        RetrofitClient retrofitClient = new RetrofitClient();
        IAdyogiAdsApiRetrofit adyogiAdsApi = retrofitClient.getInstance(baseUrl).create(IAdyogiAdsApiRetrofit.class);

        Call<List<ParseClientDTO>> call = adyogiAdsApi.getClients();

        List<String> clientIds = new ArrayList<>();
        try {
            Response<List<ParseClientDTO>> response = call.execute(); // Synchronous call

            if (response.isSuccessful() && response.body() != null) {
                // Extract client IDs
                for (ParseClientDTO client : response.body()) {
                    clientIds.add(client.getClientId());
                }
            } else {
                logger.warn(ERROR_FAILED_API_CALL + response.code());
            }
        } catch (IOException e) {
            logger.error(ERROR_FAILED_API_CALL + e.getMessage());
        }
        return clientIds;
    }
}

