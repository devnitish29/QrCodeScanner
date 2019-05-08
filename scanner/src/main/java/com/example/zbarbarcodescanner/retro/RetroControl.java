package com.example.zbarbarcodescanner.retro;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        TrustManager tm[] = new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[1];
            }
        }};
        SSLContext contextSSL;
        try {
            contextSSL = SSLContext.getInstance("TLS");
            contextSSL.init(null, tm, null);
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            return new OkHttpClient.Builder()
                    .sslSocketFactory(contextSSL.getSocketFactory())
                    .addInterceptor(interceptor)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            Log.e("TAG", e.getMessage(), e);
        } catch (KeyManagementException e) {
            Log.e("TAG", e.getMessage(), e);
        }
        return null;
    }
}
