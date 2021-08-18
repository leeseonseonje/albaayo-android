package com.example.albaayo.employer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.CompanyNoticeRegister;
import com.example.albaayo.EmployerMainPage;
import com.example.albaayo.LoginPage;
import com.example.albaayo.NoticeUpdate;
import com.example.albaayo.R;
import com.example.company_notice.CompanyNoticeContentAdapter;
import com.example.company_notice.CompanyNoticeRegisterAdapter;
import com.example.http.Http;
import com.example.http.dto.Id;
import com.example.http.dto.ImageDto;
import com.example.http.dto.NoticeImageDto;
import com.example.http.dto.ResponseNoticeDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeContent extends AppCompatActivity {

    private TextView header;
    private Long noticeId;
    private String companyName;

    private TextView title, name, date, contents;
    private ImageView update, delete;

    private String imageBase;

    private ResponseNoticeDto result;
    private List<ImageDto> list = new ArrayList<>();
    private ProgressDialog progressDialog;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_content);

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        companyName = intent.getStringExtra("companyName");
        noticeId = intent.getLongExtra("noticeId", 0);
        header = findViewById(R.id.header_name_text);
        header.setText(companyName);

        title = findViewById(R.id.notice_title);
        name = findViewById(R.id.notice_name);
        date = findViewById(R.id.notice_date);
        contents = findViewById(R.id.notice_content);

        update = findViewById(R.id.notice_update_button);
        delete = findViewById(R.id.notice_delete_button);

        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");



        Call<ResponseNoticeDto> call = Http.getInstance().getApiService()
                .noticeContent(Id.getInstance().getAccessToken(), noticeId);
        progressDialog.show();
        call.enqueue(new Callback<ResponseNoticeDto>() {
            @Override
            public void onResponse(Call<ResponseNoticeDto> call, Response<ResponseNoticeDto> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<ResponseNoticeDto> reCall = Http.getInstance().getApiService()
                            .noticeContent(Id.getInstance().getAccessToken(), noticeId);
                    reCall.enqueue(new Callback<ResponseNoticeDto>() {
                        @Override
                        public void onResponse(Call<ResponseNoticeDto> call, Response<ResponseNoticeDto> response) {
                            if (Id.getInstance().getId().equals(response.body().getMemberId())) {
                                update.setVisibility(View.VISIBLE);
                                delete.setVisibility(View.VISIBLE);
                            }
                            title.setText(response.body().getTitle());
                            name.setText(response.body().getName());
                            date.setText(response.body().getDate());
                            contents.setText(response.body().getContents() + "\n");
                            RecyclerView recyclerView = findViewById(R.id.recycler_view);
                            LinearLayoutManager manager = new LinearLayoutManager(NoticeContent.this, LinearLayoutManager.VERTICAL,false);
                            recyclerView.setLayoutManager(manager);
                            recyclerView.setAdapter(new CompanyNoticeContentAdapter(response.body().getImageList()));
                            result = response.body();

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<ResponseNoticeDto> call, Throwable t) {

                        }
                    });
                } else if (Id.getInstance().getId().equals(response.body().getMemberId())) {
                    update.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                }
                title.setText(response.body().getTitle());
                name.setText(response.body().getName());
                date.setText(response.body().getDate());
                contents.setText(response.body().getContents() + "\n");
                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                LinearLayoutManager manager = new LinearLayoutManager(NoticeContent.this, LinearLayoutManager.VERTICAL,false);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(new CompanyNoticeContentAdapter(response.body().getImageList()));
                result = response.body();

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseNoticeDto> call, Throwable t) {
                Toast.makeText(NoticeContent.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });


        update.setOnClickListener(v -> {
            Intent i = new Intent(NoticeContent.this, NoticeUpdate.class);
            i.putExtra("companyName", companyName);
            i.putExtra("noticeId", noticeId);
            startActivity(i);
        });

        delete.setOnClickListener(v -> {
            progressDialog.show();
            Call<Void> call1 = Http.getInstance().getApiService()
                    .removeNotice(Id.getInstance().getAccessToken(), noticeId);
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<Void> reCall1 = Http.getInstance().getApiService()
                                .removeNotice(Id.getInstance().getAccessToken(), noticeId);
                        reCall1.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                Toast.makeText(NoticeContent.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
                    } else {
                        Toast.makeText(NoticeContent.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(NoticeContent.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
