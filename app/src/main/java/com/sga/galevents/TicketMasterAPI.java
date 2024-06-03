package com.sga.galevents;

import com.sga.galevents.io.TicketMasterService;
import com.sga.galevents.io.response.TicketMasterResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//NO SE ESTA USANDO ACTUALMENTE
public class TicketMasterAPI {
    private static final String url = "https://app.ticketmaster.com/discovery/v2/events";
    private static final String key = "A2qjk4CKGbBiLT6Oc7YUL8LdrImtdkGI";
    private Retrofit retrofit;

    public TicketMasterAPI(){
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void searchEvents(String apiKey, String city, String countryCode, String radius, String unit, String locale, Callback<TicketMasterResponse> callback){
        TicketMasterService service = retrofit.create(TicketMasterService.class);
        Call<TicketMasterResponse> call = service.getEvents(apiKey, radius, unit, locale, city, countryCode);
        call.enqueue(callback);
    }
}
