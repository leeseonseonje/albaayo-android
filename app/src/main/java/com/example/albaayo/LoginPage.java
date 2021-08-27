package com.example.albaayo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.http.Http;
import com.example.http.dto.RequestLoginDto;
import com.example.http.dto.ResponseLoginDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPage extends AppCompatActivity {
    EditText password_hide; //비밀번호 하이드 변수 생성
    CheckBox showPassword; //비밀번호 하이드 변수 생성
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // login2 로그인화면 -> 회원가입화면
        EditText id_input = (EditText) findViewById(R.id.id_input);
        EditText password_input = (EditText) findViewById(R.id.password_input);
        Button login = (Button) findViewById(R.id.login);
        Button sign_up = (Button) findViewById(R.id.sign_up);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestLoginDto request = RequestLoginDto.builder()
                        .userId(id_input.getText().toString())
                        .password(password_input.getText().toString())
                        .build();

                loginApi(request);
            }
        });
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, SignUp.class);
                LoginPage.this.startActivity(intent);
            }
        });

        password_hide = findViewById(R.id.password_input); //비밀번호 하이드 변수에 id값 넣기
        showPassword = findViewById(R.id.showpassword); //비밀번호 하이드 변수에 id값 넣기

        // 체크박스 비밀번호 show 기능
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    password_hide.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password_hide.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        }); //여기까지 체크박스 비밀번호 show 기능
    }

    private void loginApi(RequestLoginDto request) {
        Call<ResponseLoginDto> call = Http.getInstance().getApiService().loginApi(request);
        call.enqueue(new Callback<ResponseLoginDto>() {
            @Override
            public void onResponse(Call<ResponseLoginDto> call, Response<ResponseLoginDto> response) {
                if (response.body() == null) {
                    Toast.makeText(LoginPage.this, "아이디 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                else if (response.body().getRole().equals("ROLE_EMPLOYER")) {
                    intent = new Intent(LoginPage.this, EmployerMainPage.class);
                    intent.putExtra("login", response.body());
                    startActivity(intent);
                } else {
                    intent = new Intent(LoginPage.this, WorkerMainPage.class);
                    intent.putExtra("login", response.body());
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseLoginDto> call, Throwable t) {
                System.out.println("Fail: " + t.getMessage());
                Toast.makeText(LoginPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
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