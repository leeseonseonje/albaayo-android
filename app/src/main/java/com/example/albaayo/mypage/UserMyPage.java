package com.example.albaayo.mypage;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.albaayo.LoginPage;
import com.example.albaayo.R;
import com.example.http.dto.Id;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserMyPage extends AppCompatActivity {

    private TextView roleText, usernameText, userIdText;
    private Button logoutButton;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_page);

        initData();
        logout();
    }

    private void logout() {
        logoutButton.setOnClickListener(v -> {
            Call<Void> logout = Http.getInstance().getApiService().logout(Id.getInstance().getId());
            logout.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    editor.clear();
                    editor.commit();
                    Intent intent = new Intent(UserMyPage.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(UserMyPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initData() {
        roleText = findViewById(R.id.my_role);
        usernameText = findViewById(R.id.my_name);
        userIdText = findViewById(R.id.my_id);

        if (Id.getInstance().getRole().equals("ROLE_EMPLOYER")) {
            roleText.setText("사장님");
        } else {
            roleText.setText("알바생");
        }

        usernameText.setText(Id.getInstance().getName());
        userIdText.setText(Id.getInstance().getUserId());

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        logoutButton = findViewById(R.id.logout);
    }
}
