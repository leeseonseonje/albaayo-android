package com.example.albaayo.employer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.albaayo.R;
import com.example.http.dto.Id;
import com.example.http.dto.RequestInviteWorkerDto;
import com.example.http.dto.ResponseFindWorkerDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerInvite extends AppCompatActivity {

    private Long companyId;
    private String companyName;
    private TextView headerName;
    private Button searchButton;
    private EditText idEditText;
    private TextView resultId, resultName, resultBirth;
    private TextView userId, username, userBirth;
    private Button invite;
    private ProgressDialog progressDialog;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_invite);

        initData();

        workerFind();
        workerInvite();
    }

    private void initData() {
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");

        headerName = findViewById(R.id.header_name_text);
        headerName.setText(companyName);

        searchButton = findViewById(R.id.id_search);
        invite = findViewById(R.id.invite_button);
    }

    private void workerFind() {
        searchButton.setOnClickListener(v -> {
            progressDialog.show();
            idEditText = findViewById(R.id.id_edit_text);
            Call<ResponseFindWorkerDto> call = Http.getInstance().getApiService()
                    .workerFind(Id.getInstance().getAccessToken(), idEditText.getText().toString());
            call.enqueue(new Callback<ResponseFindWorkerDto>() {
                @Override
                public void onResponse(Call<ResponseFindWorkerDto> call, Response<ResponseFindWorkerDto> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<ResponseFindWorkerDto> reCall = Http.getInstance().getApiService()
                                .workerFind(Id.getInstance().getAccessToken(), idEditText.getText().toString());
                        reCall.enqueue(new Callback<ResponseFindWorkerDto>() {
                            @Override
                            public void onResponse(Call<ResponseFindWorkerDto> call, Response<ResponseFindWorkerDto> response) {
                                if (response.code() != 500) {
                                    resultId = findViewById(R.id.result_id);
                                    resultName = findViewById(R.id.result_name);
                                    resultBirth = findViewById(R.id.result_birth);
                                    userId = findViewById(R.id.user_id);
                                    username = findViewById(R.id.username);
                                    userBirth = findViewById(R.id.user_birth);

                                    resultId.setVisibility(View.VISIBLE);
                                    resultName.setVisibility(View.VISIBLE);
                                    resultBirth.setVisibility(View.VISIBLE);
                                    userId.setVisibility(View.VISIBLE);
                                    username.setVisibility(View.VISIBLE);
                                    userBirth.setVisibility(View.VISIBLE);
                                    invite.setVisibility(View.VISIBLE);

                                    userId.setText(response.body().getUserId());
                                    username.setText(response.body().getName());
                                    userBirth.setText(response.body().getBirth());
                                    InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                } else {
                                    new AlertDialog.Builder(WorkerInvite.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("존재하지 않는 사용자 입니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which){
                                                }
                                            })
                                            .show();
                                }
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<ResponseFindWorkerDto> call, Throwable t) {

                            }
                        });
                    } else if (response.code() != 500) {
                        resultId = findViewById(R.id.result_id);
                        resultName = findViewById(R.id.result_name);
                        resultBirth = findViewById(R.id.result_birth);
                        userId = findViewById(R.id.user_id);
                        username = findViewById(R.id.username);
                        userBirth = findViewById(R.id.user_birth);

                        resultId.setVisibility(View.VISIBLE);
                        resultName.setVisibility(View.VISIBLE);
                        resultBirth.setVisibility(View.VISIBLE);
                        userId.setVisibility(View.VISIBLE);
                        username.setVisibility(View.VISIBLE);
                        userBirth.setVisibility(View.VISIBLE);
                        invite.setVisibility(View.VISIBLE);

                        userId.setText(response.body().getUserId());
                        username.setText(response.body().getName());
                        userBirth.setText(response.body().getBirth());
                        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    } else {
                        new AlertDialog.Builder(WorkerInvite.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setMessage("존재하지 않는 사용자 입니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                    }
                                })
                                .show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ResponseFindWorkerDto> call, Throwable t) {
                    Toast.makeText(WorkerInvite.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void workerInvite() {
        invite.setOnClickListener(v -> {
            progressDialog.show();
            Call<ResponseFindWorkerDto> call = Http.getInstance().getApiService()
                    .workerInvite(Id.getInstance().getAccessToken(), companyId,
                    new RequestInviteWorkerDto(userId.getText().toString()));
            call.enqueue(new Callback<ResponseFindWorkerDto>() {
                @Override
                public void onResponse(Call<ResponseFindWorkerDto> call, Response<ResponseFindWorkerDto> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<ResponseFindWorkerDto> reCall = Http.getInstance().getApiService()
                                .workerInvite(Id.getInstance().getAccessToken(), companyId,
                                        new RequestInviteWorkerDto(userId.getText().toString()));
                        reCall.enqueue(new Callback<ResponseFindWorkerDto>() {
                            @Override
                            public void onResponse(Call<ResponseFindWorkerDto> call, Response<ResponseFindWorkerDto> response) {
                                if (response.code() != 500) {
                                    finish();
                                    progressDialog.dismiss();
                                } else {
                                    progressDialog.dismiss();
                                    new AlertDialog.Builder(WorkerInvite.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("이미 초대한 근로자 입니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which){
                                                }
                                            })
                                            .show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseFindWorkerDto> call, Throwable t) {

                            }
                        });
                    } else if (response.code() != 500) {
                        finish();
                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        new AlertDialog.Builder(WorkerInvite.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setMessage("이미 초대한 근로자 입니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseFindWorkerDto> call, Throwable t) {
                    Toast.makeText(WorkerInvite.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
