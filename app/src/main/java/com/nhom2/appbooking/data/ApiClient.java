package com.nhom2.appbooking.data;

import android.content.Context;

import com.nhom2.appbooking.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/** Tạo Retrofit dùng chung cho cả app. */
public final class ApiClient {

    private static volatile ApiService service;

    private ApiClient() {
    }

    public static ApiService get(Context context) {
        if (service == null) {
            synchronized (ApiClient.class) {
                if (service == null) {
                    service = create(new SessionManager(context));
                }
            }
        }
        return service;
    }

    /** true nếu request gửi tới backend của app (không phải VietQR hay máy chủ khác). */
    public static boolean isOwnApi(Request request) {
        return request.url().host().equals(HttpUrl.get(BuildConfig.API_BASE_URL).host());
    }

    private static ApiService create(SessionManager session) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    String token = session.getToken();
                    // Chỉ gắn JWT cho backend của mình, không lộ ra máy chủ khác
                    if (token != null && isOwnApi(request)) {
                        request = request.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                    }
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                // Scalars trước: các API tài khoản trả về chuỗi trơn
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }
}
