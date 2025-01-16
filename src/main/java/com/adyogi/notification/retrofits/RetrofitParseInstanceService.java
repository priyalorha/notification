package com.adyogi.notification.retrofits;


import com.adyogi.notification.utils.constants.MongoConstants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class RetrofitParseInstanceService {

    public Retrofit retrofit;

    public Retrofit retrofitWithJacksonConverter;

    @Value("${back4AppAdyogiProdApi.url}")
    private String back4AppApiUrl;

    @Value("${back4AppAdyogiProdApi.parseApplicationId}")
    private String parseApplicationId;

    @Value("${back4AppAdyogiProdApi.parseRestApiKey}")
    private String parseRestApiKey;

    @Value("${back4AppAdyogiProdApi.parseSessionToken}")
    private String parseSessionToken;

    public Retrofit getRetrofitInstance(){

        if(null == retrofit){
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.callTimeout(5, TimeUnit.MINUTES);
            httpClient.readTimeout(5,TimeUnit.MINUTES);
            httpClient.writeTimeout(5,TimeUnit.MINUTES);
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    requestBuilder.header(MongoConstants.BACK4APP_PARSE_APPLICATION_ID_HEADER, parseApplicationId)
                            .header(MongoConstants.BACK4APP_PARSE_REST_API_KEY_HEADER,parseRestApiKey)
                            .header(MongoConstants.BACK4APP_PARSE_SESSION_TOKEN_HEADER,parseSessionToken)
                            .method(original.method(), original.body());
                    Request request= requestBuilder.build();
                    return chain.proceed(request);
                }
            });
            retrofit = new Retrofit.Builder()
                    .baseUrl(back4AppApiUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public Retrofit getRetrofitInstanceWithJacksonConverter(){

        if(null == retrofitWithJacksonConverter){
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.callTimeout(5, TimeUnit.MINUTES);
            httpClient.readTimeout(5,TimeUnit.MINUTES);
            httpClient.writeTimeout(5,TimeUnit.MINUTES);
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    requestBuilder.header(MongoConstants.BACK4APP_PARSE_APPLICATION_ID_HEADER, parseApplicationId)
                            .header(MongoConstants.BACK4APP_PARSE_REST_API_KEY_HEADER,parseRestApiKey)
                            .header(MongoConstants.BACK4APP_PARSE_SESSION_TOKEN_HEADER,parseSessionToken)
                            .method(original.method(), original.body());
                    Request request= requestBuilder.build();
                    return chain.proceed(request);
                }
            });


            retrofitWithJacksonConverter = new Retrofit.Builder()
                    .baseUrl(back4AppApiUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofitWithJacksonConverter;
    }
}
