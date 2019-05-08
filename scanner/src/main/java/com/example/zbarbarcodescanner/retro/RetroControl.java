package com.example.zbarbarcodescanner.retro;



import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetroControl {
    private final static Object LOCK=new Object();
    private static Retrofit retrofitInstance;

    public static Retrofit getRetroInstance() {
        if(retrofitInstance==null) {
            synchronized (LOCK) {
                retrofitInstance=new Retrofit.Builder()
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl(RetroAPI.BASE_URL)
                        .client(getHttpClient())
                        .build();
            }
        }
        return retrofitInstance;
    }

    public static void sendXmlResponse(RetroAPI retroAPI, String xmlString, Callback<String> callback) {
        //Call<String> nounceResponse=retroAPI.getRecipeJson();
        try {
            Call<String> xmlResponse=retroAPI.sendXmlString(xmlString);
            xmlResponse.enqueue(callback);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    private static OkHttpClient getHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
    }
}
