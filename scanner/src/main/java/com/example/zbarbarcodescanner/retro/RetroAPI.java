package com.example.zbarbarcodescanner.retro;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetroAPI {
    public static String BASE_URL="http://10.77.4.181";

    @POST("/getdatafromscanner.php")
    Call<String> sendXmlString(@Body String xmlString);

}
