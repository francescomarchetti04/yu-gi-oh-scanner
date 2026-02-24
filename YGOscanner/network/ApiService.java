package YGOscanner.network;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/scan")
    Call<Void> sendScan(
            @Header("Authorization") String apiKey,
            @Body ScanRequest request
    );

    @GET("api/collection")
    Call<List<CardResponse>> getCollection(
            @Header("Authorization") String apiKey
    );
}