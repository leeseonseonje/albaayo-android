package com.example.albaayo.option;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.albaayo.R;
import com.example.albaayo.WorkerMainPage;
import com.example.http.Http;
import com.example.http.dto.Id;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerGroupOption extends AppCompatActivity {

    private Long companyId;
    private String companyName;
    private Button salaryInfoButton, workContractButton, groupOutButton;
    private TextView headerText;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_group_page);

        initData();

        groupOut();
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
