package com.example.albaayo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.albaayo.location.LocationService;
import com.example.http.dto.Id;
import com.example.http.dto.RequestCommuteDto;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerCommute extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private GpsTracker gpsTracker;
    private static final int MY_PERMISSION_LOCATION = 1111;
    private static final String LOG_TAG = "MainActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private Long companyId;
    private String companyName, companyLocation;
    private Geocoder geocoder;
    private double latitude, longitude;
    private double companyLatitude, companyLongitude;
    private List<Address> fromLocationName;
    private Address companyAddress;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_commute);
        checkPermission();

        initData();

        mapCircle();


        goToWork();

        offWork();

//        goToWorkButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View arg0)
//            {
//
//                gpsTracker = new GpsTracker(WorkerCommute.this);
//
//                double latitude = gpsTracker.getLatitude();
//                double longitude = gpsTracker.getLongitude();
//
//                String address = getCurrentAddress(latitude, longitude);
////                textview_address.setText(address);
//
//                Toast.makeText(WorkerCommute.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
//
//            }
//        });
    }

    private void offWork() {
        Button offWorkButton = findViewById(R.id.off_work);
        offWorkButton.setOnClickListener(v -> {

            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage("퇴근하시겠습니까?")
                    .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                            if (latitude + 0.003 >= companyLatitude && latitude - 0.003 <= companyLatitude) {
                                if (longitude + 0.003 >= companyLongitude && longitude - 0.003 <= companyLongitude) {
                                    RequestCommuteDto request = RequestCommuteDto.builder().workerId(Id.getInstance().getId()).companyId(companyId).build();
                                    Call<Void> call = Http.getInstance().getApiService()
                                            .offWork(Id.getInstance().getAccessToken(), request);
                                    call.enqueue(new Callback<Void>() {
                                        @SneakyThrows
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.code() == 401) {
                                                Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                editor.putString("accessToken", response.headers().get("Authorization"));
                                                editor.commit();

                                                Call<Void> reCall = Http.getInstance().getApiService()
                                                        .offWork(Id.getInstance().getAccessToken(), request);
                                                reCall.enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        if (response.code() != 500) {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("퇴근 완료")
                                                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which){
                                                                            Intent stopLocationService =
                                                                                    new Intent(WorkerCommute.this, LocationService.class);
                                                                            stopService(stopLocationService);
                                                                            Call<Void> deleteCall =
                                                                                    Http.getInstance().getApiService()
                                                                                            .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                            deleteCall.enqueue(new Callback<Void>() {
                                                                                @Override
                                                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                                                    if (response.code() == 401) {
                                                                                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                                                        editor.putString("accessToken", response.headers().get("Authorization"));
                                                                                        editor.commit();

                                                                                        Call<Void> deleteCall =
                                                                                                Http.getInstance().getApiService()
                                                                                                        .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                                        deleteCall.enqueue(new Callback<Void>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<Void> call, Throwable t) {
                                                                                                Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<Void> call, Throwable t) {
                                                                                    Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    })
                                                                    .show();
                                                        } else {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("출근을 하지 않았습니다.")
                                                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which){
                                                                        }
                                                                    })
                                                                    .show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else if (response.code() != 500) {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("퇴근 완료")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which){
                                                                Intent stopLocationService =
                                                                        new Intent(WorkerCommute.this, LocationService.class);
                                                                stopService(stopLocationService);
                                                                Call<Void> deleteCall =
                                                                        Http.getInstance().getApiService()
                                                                                .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                deleteCall.enqueue(new Callback<Void>() {
                                                                    @Override
                                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                                        if (response.code() == 401) {
                                                                            Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                                            editor.putString("accessToken", response.headers().get("Authorization"));
                                                                            editor.commit();

                                                                            Call<Void> deleteCall =
                                                                                    Http.getInstance().getApiService()
                                                                                            .deleteLocation(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                                                                            deleteCall.enqueue(new Callback<Void>() {
                                                                                @Override
                                                                                public void onResponse(Call<Void> call, Response<Void> response) {

                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<Void> call, Throwable t) {

                                                                                }
                                                                            });
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                                        Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .show();
                                            } else {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("출근을 하지 않았습니다.")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which){
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("위치가 맞지 않습니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which){
                                                }
                                            })
                                            .show();
                                }
                            } else {
                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setMessage("위치가 맞지 않습니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which){
                                            }
                                        })
                                        .show();
                            }

                        }
                    })
                    .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        });
    }

    private void goToWork() {
        Button goToWorkButton = (Button) findViewById(R.id.go_to_work);
        goToWorkButton.setOnClickListener(v -> {

            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage("출근하시겠습니까?")
                    .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(DialogInterface dialog, int which){

                            if (latitude + 0.003 >= companyLatitude && latitude - 0.003 <= companyLatitude) {
                                if (longitude + 0.003 >= companyLongitude && longitude - 0.003 <= companyLongitude) {
                                    RequestCommuteDto request = RequestCommuteDto.builder().workerId(Id.getInstance().getId()).companyId(companyId).build();
                                    Call<Void> call = Http.getInstance().getApiService()
                                            .goToWork(Id.getInstance().getAccessToken(), request);
                                    call.enqueue(new Callback<Void>() {
                                        @SneakyThrows
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.code() == 401) {
                                                Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                                editor.putString("accessToken", response.headers().get("Authorization"));
                                                editor.commit();

                                                Call<Void> reCall = Http.getInstance().getApiService()
                                                        .goToWork(Id.getInstance().getAccessToken(), request);
                                                reCall.enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                                        if (response.code() != 500) {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("출근 완료")
                                                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            Intent startLocationService =
                                                                                    new Intent(WorkerCommute.this, LocationService.class);
                                                                            startLocationService.putExtra("companyId", companyId);
                                                                            startService(startLocationService);
                                                                        }
                                                                    })
                                                                    .show();
                                                        } else {
                                                            new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                                    .setMessage("퇴근을 하지 않았습니다.")
                                                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which){
                                                                        }
                                                                    })
                                                                    .show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Void> call, Throwable t) {
                                                        Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else if (response.code() != 500) {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("출근 완료")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent startLocationService =
                                                                        new Intent(WorkerCommute.this, LocationService.class);
                                                                startLocationService.putExtra("companyId", companyId);
                                                                startService(startLocationService);
                                                            }
                                                        })
                                                        .show();
                                            } else {
                                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                        .setMessage("퇴근을 하지 않았습니다.")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which){
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Toast.makeText(WorkerCommute.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("위치가 맞지 않습니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which){
                                                }
                                            })
                                            .show();
                                }
                            } else {
                                new AlertDialog.Builder(WorkerCommute.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setMessage("위치가 맞지 않습니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which){
                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        });
    }

    private void initData() throws IOException {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

//        progressDialog.show();
        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");
        companyLocation = intent.getStringExtra("companyLocation");
        TextView header = findViewById(R.id.header_name_text);
        header.setText(companyName);
        //지도를 띄우자
        // java code
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        gpsTracker = new GpsTracker(WorkerCommute.this);

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        geocoder = new Geocoder(this, Locale.getDefault());
        fromLocationName = geocoder.getFromLocationName(companyLocation, 1);
        companyAddress = fromLocationName.get(0);
        companyLatitude = companyAddress.getLatitude();
        companyLongitude = companyAddress.getLongitude();
    }

    private void mapCircle() {
        MapPoint point = MapPoint.mapPointWithGeoCoord(companyLatitude, companyLongitude);

        MapPOIItem myLocation = new MapPOIItem();
        myLocation.setItemName(companyName);
        myLocation.setTag(0);
        myLocation.setMapPoint(point);
        myLocation.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 파란색 마커 모양
        myLocation.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 빨간색 마커 모양으로 변경
        mapView.addPOIItem(myLocation);

        MapCircle circle = new MapCircle(
                MapPoint.mapPointWithGeoCoord(companyLatitude, companyLongitude), // center
                50, // radius
                Color.argb(128, 255, 0, 0), // strokeColor
                Color.argb(128, 255, 255, 0) // fillColor
        );
        circle.setTag(5678);
        mapView.addCircle(circle);

        MapPointBounds[] mapPointBoundsArray = { circle.getBound() };
        MapPointBounds mapPointBounds = new MapPointBounds(mapPointBoundsArray);
        int padding = 80; // px
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
    }


    public String getCurrentAddress(double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        geocoder = new Geocoder(this, Locale.getDefault());


        List<Address> addresses;

        try {

            List<Address> fromLocationName = geocoder.getFromLocationName(companyLocation, 1000);
            companyAddress = fromLocationName.get(0);
            System.out.println("address1.getAddressLine(0).toString(); = " + companyAddress.getAddressLine(0).toString());
            System.out.println("address1 = " + companyAddress.getLatitude());
            System.out.println("address1.getLongitude() = " + companyAddress.getLongitude());
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewContainer.removeAllViews();
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }


    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }

    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음

            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있다
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    finish();
                } else {
                }
            }
        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(WorkerCommute.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(WorkerCommute.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(WorkerCommute.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(WorkerCommute.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(WorkerCommute.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 다시 보지 않기 버튼을 만드려면 이 부분에 바로 요청을 하도록 하면 됨 (아래 else{..} 부분 제거)
            // ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_CAMERA);

            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_LOCATION);
            }
        }
    }
}
