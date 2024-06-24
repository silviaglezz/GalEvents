package com.sga.galevents.io;

import com.sga.galevents.io.response.TicketMasterResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TicketMasterService {
    @GET("discovery/v2/events.json")

    Call<TicketMasterResponse> getEvents(@Query("apikey") String apiKey,
                                         @Query("radius") String radius,
                                         @Query("unit") String unit,
                                         @Query("locale") String locale,
                                         @Query("city") String city,
                                         @Query("countryCode") String countryCode);

}
