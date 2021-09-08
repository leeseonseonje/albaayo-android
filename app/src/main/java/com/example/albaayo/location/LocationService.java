package com.example.albaayo.location;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import com.example.albaayo.GpsTracker;
import com.example.http.dto.Id;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {

    private Thread locationSend;
    private Geocoder geocoder;
    private GpsTracker gpsTracker;
    private boolean isStop;
    private Long companyId;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        companyId = intent.getLongExtra("companyId", 0);

        geocoder = new Geocoder(LocationService.this, Locale.getDefault());
        gpsTracker = new GpsTracker(LocationService.this);

        locationSend = new Thread(new LocationSend());
        locationSend.start();
        isStop = false;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = true;
    }

    private class LocationSend implements Runnable {
//
        private Handler handler = new Handler();

        @SneakyThrows
        @Override
        public void run() {

            while (!isStop) {
                handler.post(new Runnable() {
                    @SneakyThrows
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {

                        locationService();
                    }
                });
                Thread.sleep(10000);
            }
        }
    }

    private void locationService() throws IOException {
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        List<Address> fromLocation = geocoder.getFromLocation(latitude, longitude, 1);
        Address address = fromLocation.get(0);
        String add = address.getAddressLine(0);
        LocationSaveDto locationSaveDto = LocationSaveDto.builder()
                .memberId(Id.getInstance().getId())
                .companyId(companyId)
                .location(add).build();
        Call<Void> call = Http.getInstance().getApiService()
                .saveLocation(Id.getInstance().getAccessToken(), locationSaveDto);
        call.enqueue(new Callback<Void>() {
            @SneakyThrows
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<Void> reCall = Http.getInstance().getApiService()
                            .saveLocation(Id.getInstance().getAccessToken(), locationSaveDto);
                    reCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                        }
                    });
                }
            }

            @SneakyThrows
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }
}