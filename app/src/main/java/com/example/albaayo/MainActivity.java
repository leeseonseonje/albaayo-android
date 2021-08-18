                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                package com.example.albaayo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sf;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티 시작할때 처음실행되는 생명주기
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, //상태바 투명
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.loading);

        sf = getSharedPreferences("sFile", MODE_PRIVATE);

        // login1 -> login2
        Handler hand = new Handler();
        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sf.getString("role", "").equals("ROLE_WORKER")) {
                    Intent intent = new Intent(MainActivity.this, WorkerMainPage.class);
                    startActivity(intent);
                    finish();
                } else if (sf.getString("role","").equals("ROLE_EMPLOYER")) {
                    Intent intent = new Intent(MainActivity.this, EmployerMainPage.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000); // 2초뒤 화면전환
    }
}

