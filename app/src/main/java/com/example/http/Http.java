package com.example.http;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;

import com.example.http.dto.Id;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Http {

    private Retrofit retrofit;
    private ApiService apiService;
    public static final String URL = "192.168.0.4";
    public static Http Http = new Http();
    private Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }).create();


    private Http() {

        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + URL + ":9000") //api의 baseURL
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class); //실제 api Method들이 선언된 Interface객체 선언
    }

    public static Http getInstance() { //싱글톤으로 선언된 레트로핏 객체 얻는 용
        return Http;
    }


    public ApiService getApiService() { // API Interface 객체 얻는 용
        return apiService;
    }
}
