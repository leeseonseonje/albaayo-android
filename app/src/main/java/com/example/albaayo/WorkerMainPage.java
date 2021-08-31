package com.example.albaayo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.mypage.UserMyPage;
import com.example.http.Http;
import com.example.http.dto.CompanyDto;
import com.example.http.dto.Id;
import com.example.http.dto.ResponseLoginDto;
import com.example.http.dto.Result;
import com.example.list.accept_company.CompanyListAdapter;
import com.example.list.accept_company.NotAcceptCompanyListAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerMainPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button acceptListButton, inviteListButton, myPageButton;
    private Boolean isPage = true;
    private TextView headerName, emptyText, countText;
    private ImageView countImage;
    private ResponseLoginDto data;

    private ProgressDialog progressDialog;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_main_page);

        Intent intent = getIntent();
        data = intent.getParcelableExtra("login");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        if (data != null) {
            editor.putString("accessToken", data.getAccessToken());
            editor.putLong("id", data.getId());
            editor.putString("userId", data.getUserId());
            editor.putString("role", data.getRole());
            editor.putString("name", data.getName());
            editor.commit();
        }
        Id.getInstance().setAccessToken(sf.getString("accessToken", ""));
        Id.getInstance().setId(sf.getLong("id", 0L));
        Id.getInstance().setUserId(sf.getString("userId", ""));
        Id.getInstance().setName(sf.getString("name", ""));
        Id.getInstance().setRole(sf.getString("role", ""));

        initData();

        headerName.setText(Id.getInstance().getName());

        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");
        acceptCompanyApi();

        headerButton();
        footerButton();
    }

    private void footerButton() {
        myPageButton = findViewById(R.id.my_page);
        myPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(WorkerMainPage.this, UserMyPage.class);
            startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void headerButton() {
        acceptListButton.setOnClickListener(v -> {
            if (!isPage) {
                isPage = true;
                acceptListButton.setTextColor(Color.parseColor("#8ABFE8"));
                inviteListButton.setTextColor(Color.BLACK);
                acceptCompanyApi();
            }
        });

        inviteListButton.setOnClickListener(v -> {
            if (isPage) {
                isPage = false;
                inviteListButton.setTextColor(Color.parseColor("#8ABFE8"));
                acceptListButton.setTextColor(Color.BLACK);
                notAcceptCompanyApi();
            }
        });
    }

    private void initData() {
        emptyText = findViewById(R.id.empty_list);
        headerName = findViewById(R.id.header_name_text);
        countText = findViewById(R.id.count);
        countImage = findViewById(R.id.count_image);
        recyclerView = findViewById(R.id.recycler_view);
        acceptListButton = findViewById(R.id.accept);
        inviteListButton = findViewById(R.id.not_accept);
    }

    private void recyclerViewSetting(List<CompanyDto> result) {

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(WorkerMainPage.this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new CompanyListAdapter(result, this));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void acceptCompanyApi() {
        progressDialog.show();
        Call<Result<List<CompanyDto>>> call = Http.getInstance().getApiService()
                .acceptCompanyList(Id.getInstance().getAccessToken(), Id.getInstance().getId());
        call.enqueue(new Callback<Result<List<CompanyDto>>>() {
            @Override
            public void onResponse(Call<Result<List<CompanyDto>>> call, Response<Result<List<CompanyDto>>> response) {

                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();
                    Call<Result<List<CompanyDto>>> reCall = Http.getInstance().getApiService()
                            .acceptCompanyList(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                    reCall.enqueue(new Callback<Result<List<CompanyDto>>>() {
                        @Override
                        public void onResponse(Call<Result<List<CompanyDto>>> call, Response<Result<List<CompanyDto>>> response) {
                            if (!response.body().getData().isEmpty()) {
                                recyclerViewSetting(response.body().getData());
                                emptyText.setVisibility(View.GONE);
                            } else {
                                emptyText.setVisibility(View.VISIBLE);
                            }

                            if (response.body().getCount() > 0) {
                                countImage.setVisibility(View.VISIBLE);
                                countText.setVisibility(View.VISIBLE);
                                countText.setText(response.body().getCount() + "");
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<Result<List<CompanyDto>>> call, Throwable t) {
                            Toast.makeText(WorkerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (!response.body().getData().isEmpty()) {
                    recyclerViewSetting(response.body().getData());
                    emptyText.setVisibility(View.GONE);

                    if (response.body().getCount() > 0) {
                        countImage.setVisibility(View.VISIBLE);
                        countText.setVisibility(View.VISIBLE);
                        countText.setText(response.body().getCount() + "");
                    }
                } else {
                    emptyText.setVisibility(View.VISIBLE);
                    if (response.body().getCount() > 0) {
                        countImage.setVisibility(View.VISIBLE);
                        countText.setVisibility(View.VISIBLE);
                        countText.setText(response.body().getCount() + "");
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Result<List<CompanyDto>>> call, Throwable t) {
                Toast.makeText(WorkerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notAcceptRecyclerViewSetting(List<CompanyDto> result) {

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(WorkerMainPage.this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new NotAcceptCompanyListAdapter(result, emptyText, sf, editor));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notAcceptCompanyApi() {

        Call<List<CompanyDto>> call = Http.getInstance().getApiService()
                .notAcceptCompanyList(Id.getInstance().getAccessToken(), Id.getInstance().getId());
        call.enqueue(new Callback<List<CompanyDto>>() {
            @Override
            public void onResponse(Call<List<CompanyDto>> call, Response<List<CompanyDto>> response) {

                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<List<CompanyDto>> reCall = Http.getInstance().getApiService()
                            .notAcceptCompanyList(Id.getInstance().getAccessToken(), Id.getInstance().getId());
                    reCall.enqueue(new Callback<List<CompanyDto>>() {
                        @Override
                        public void onResponse(Call<List<CompanyDto>> call, Response<List<CompanyDto>> response) {
                            System.out.println("response.code() = " + response.code());
                            if (!response.body().isEmpty()) {
                                notAcceptRecyclerViewSetting(response.body());
                                emptyText.setVisibility(View.GONE);
                            } else {
                                notAcceptRecyclerViewSetting(response.body());
                                emptyText.setVisibility(View.VISIBLE);
                            }

                            countImage.setVisibility(View.GONE);
                            countText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(Call<List<CompanyDto>> call, Throwable t) {
                            Toast.makeText(WorkerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (!response.body().isEmpty()) {
                    notAcceptRecyclerViewSetting(response.body());
                    emptyText.setVisibility(View.GONE);
                } else {
                    notAcceptRecyclerViewSetting(response.body());
                    emptyText.setVisibility(View.VISIBLE);
                }

                countImage.setVisibility(View.GONE);
                countText.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<CompanyDto>> call, Throwable t) {
                Toast.makeText(WorkerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

