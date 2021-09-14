package com.example.albaayo.option;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.albaayo.R;
import com.example.albaayo.SignUp;
import com.example.albaayo.WorkerMainPage;
import com.example.http.Http;
import com.example.http.dto.Id;
import com.example.http.dto.ResponsePayInformationDto;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerGroupOption extends AppCompatActivity {

    private int year, month, day;
    private DatePickerDialog.OnDateSetListener DateSetListener;
    private Long companyId;
    private String companyName;
    private Button salaryInfoButton, workContractButton, groupOutButton;
    private TextView headerText;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;
    private String date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_group_page);

        initData();

        groupOut();

        DateSetListener = (view, year, month, day) -> {
            month = month + 1;
            Log.d("", "onDateSet: yyyy.MM.dd" + month + "." + day + "." + year );
            date = year + "-" + String.format("%02d", month) + "-" + day;

            System.out.println(date);
            Call<ResponsePayInformationDto> call = Http.getInstance().getApiService().monthPayInfo(Id.getInstance().getAccessToken(),
                    Id.getInstance().getId(), companyId, date);
            int finalMonth = month;
            call.enqueue(new Callback<ResponsePayInformationDto>() {
                @Override
                public void onResponse(Call<ResponsePayInformationDto> call, Response<ResponsePayInformationDto> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<ResponsePayInformationDto> reCall = Http.getInstance().getApiService().monthPayInfo(Id.getInstance().getAccessToken(),
                                Id.getInstance().getId(), companyId, date);
                        reCall.enqueue(new Callback<ResponsePayInformationDto>() {
                            @Override
                            public void onResponse(Call<ResponsePayInformationDto> call, Response<ResponsePayInformationDto> response) {
                                new AlertDialog.Builder(WorkerGroupOption.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setMessage(year + "년" + finalMonth + "월" + "\n" + response.body().getPay())
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                            }

                            @Override
                            public void onFailure(Call<ResponsePayInformationDto> call, Throwable t) {

                            }
                        });
                    } else {
                        new AlertDialog.Builder(WorkerGroupOption.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setMessage(year + "년" + finalMonth + "월" + "\n" + response.body().getPay())
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                    }
                }

                @Override
                public void onFailure(Call<ResponsePayInformationDto> call, Throwable t) {

                }
            });
        };

        salaryInfoButton = findViewById(R.id.salary_info);
        salaryInfoButton.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(WorkerGroupOption.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    DateSetListener,
                    year,month,day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });
    }

    private void groupOut() {
        groupOutButton.setOnClickListener(v -> {

            new AlertDialog.Builder(WorkerGroupOption.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage("그룹에서 퇴장하시겠습니까?")
                    .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        public void onClick(DialogInterface dialog, int which){
                            Call<Void> call = Http.getInstance().getApiService()
                                    .companyExit(Id.getInstance().getAccessToken(), Id.getInstance().getId(), companyId);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.code() == 401) {
                                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                        editor.putString("accessToken", response.headers().get("Authorization"));
                                        editor.commit();

                                        Call<Void> reCall = Http.getInstance().getApiService()
                                                .companyExit(Id.getInstance().getAccessToken(), Id.getInstance().getId(), companyId);
                                        reCall.enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                Intent intent = new Intent(WorkerGroupOption.this, WorkerMainPage.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {

                                            }
                                        });
                                    } else {
                                        Intent intent = new Intent(WorkerGroupOption.this, WorkerMainPage.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {

                                }
                            });
                        }
                    }).setPositiveButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
        });
    }

    private void initData() {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");

        headerText = findViewById(R.id.header_name_text);
        headerText.setText(companyName);

        salaryInfoButton = findViewById(R.id.salary_info);
        workContractButton = findViewById(R.id.work_contract);
        groupOutButton = findViewById(R.id.group_out);
    }

    public void payInformation() {
        
    }
}
