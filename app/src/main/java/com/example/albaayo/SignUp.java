package com.example.albaayo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.http.Http;
import com.example.http.dto.RequestSignupDto;
import com.example.http.dto.ResponseSignupDto;
import com.example.http.dto.ValidateDuplicateCheckMessage;

import java.util.Arrays;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUp";
    private static int year, month, day;

    private TextView displayDate;
    private DatePickerDialog.OnDateSetListener DateSetListener;

    private EditText idText;
    private EditText password;
    private EditText passwordCheck;
    private EditText name;
    private EditText email;
    private Button idCheck;
    private Button signUp;
    private TextView duplicateCheck;
    private RadioButton worker;
    private RadioButton employer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        initData();

        idCheck.setOnClickListener(v -> {
            duplicateCheck(idText.getText().toString());
        });

        birthSelect();

        signUp();
    }

    private void signUp() {
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (duplicateCheck.getText().toString().equals("사용 가능한 ID 입니다.")) {
                    if (isPassChecked()) { // pw 체크
                        if (isNameChecked()) { // 이름 체크
                            // 체크에 다 일치해서 왔을때 회원가입 성공여부를 알려주고 다시 로그인 페이지로 간다.
                            RequestSignupDto request = RequestSignupDto.builder()
                                    .userId(idText.getText().toString())
                                    .password(password.getText().toString())
                                    .email(email.getText().toString())
                                    .name(name.getText().toString())
                                    .birth(displayDate.getText().toString())
                                    .build();

                            if (roleSelect(request)) {
                                Toast.makeText(SignUp.this, "회원가입에 성공하였습니다!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUp.this, LoginPage.class);
                                SignUp.this.startActivity(intent);
                            }
                        }
                    }
                } else {
                    Toast.makeText(SignUp.this, "사용 불가능한 ID 입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            public boolean isPassChecked() {
                boolean x = false;

                if(password.length() >= 8 && password.length() <= 12) {
                    x = true;
                } else {
                    Toast.makeText(SignUp.this, "비밀번호 형식이 일치하지 않습니다.(8~12자)", Toast.LENGTH_SHORT).show();
                    x = false;
                }
                if(password.getText().toString().equals(passwordCheck.getText().toString())) { // pw가 pwcheck랑 같아야하고, 값이 공백이 아니어야하고, 8자리이상 이어야함.
                    x = true;
                } else {
                    Toast.makeText(SignUp.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    x = false;
                }
                return x;
            }

            public boolean isNameChecked() {
                if(name.length() <= 50) {
                    return true;
                } else {
                    Toast.makeText(SignUp.this,"이름이 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }

    private void birthSelect() {
        displayDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(SignUp.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        DateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        DateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: yyyy.MM.dd" + month + "." + day + "." + year );
                String date = year + "년 " + month + "월 " + day + "일";
                displayDate.setText(date);
            }
        };
    }

    private void initData() {
        idText = (EditText) findViewById(R.id.input_id); // ID
        password = (EditText) findViewById(R.id.input_password); // PW
        passwordCheck = (EditText) findViewById(R.id.input_password_check); // PW 체크
        name = (EditText) findViewById(R.id.input_name); // 이름
        //final EditText birth = (EditText) findViewById(R.id.input_birth); // 생년월일
        email = (EditText) findViewById(R.id.input_email); // 이메일
        idCheck = (Button) findViewById(R.id.id_ok); // ID 중복-체크 버튼
        signUp = (Button) findViewById(R.id.sign_up); // 회원가입 확인하는 버튼
        duplicateCheck = (TextView) findViewById(R.id.duplicate_check);
        displayDate = (TextView) findViewById(R.id.input_birth);
        worker = (RadioButton) findViewById(R.id.workerRB);
        employer = (RadioButton) findViewById(R.id.employerRB);
    }

    private void duplicateCheck(String text) {
        if (!text.isEmpty()) {
            Call<ValidateDuplicateCheckMessage> call = Http.getInstance().getApiService().duplicateCheckApi(idText.getText().toString());
            call.enqueue(new Callback<ValidateDuplicateCheckMessage>() {
                @Override
                public void onResponse(Call<ValidateDuplicateCheckMessage> call, Response<ValidateDuplicateCheckMessage> response) {
                    duplicateCheck.setText(response.body().getMessage());
                }

                @Override
                public void onFailure(Call<ValidateDuplicateCheckMessage> call, Throwable t) {
                    System.out.println("Fail: " + t.getMessage() + "\n" + Arrays.toString(t.getStackTrace()));
                    Toast.makeText(SignUp.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(SignUp.this, "ID를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean roleSelect(RequestSignupDto requestSignupDto) {
        if (worker.isChecked()) {
            Call<ResponseSignupDto> call = Http.getInstance().getApiService().workerSignup(requestSignupDto);
            call.enqueue(new Callback<ResponseSignupDto>() {
                @Override
                public void onResponse(Call<ResponseSignupDto> call, Response<ResponseSignupDto> response) {
                    System.out.println("response = " + response);
                }

                @Override
                public void onFailure(Call<ResponseSignupDto> call, Throwable t) {
                    System.out.println("t.getMessage() = " + t.getMessage());
                    System.out.println("t.getStackTrace() = " + t.getStackTrace());
                }
            });
            return true;
        } else if (employer.isChecked()) {
            Call<ResponseSignupDto> call = Http.getInstance().getApiService().employerSignup(requestSignupDto);
            call.enqueue(new Callback<ResponseSignupDto>() {
                @Override
                public void onResponse(Call<ResponseSignupDto> call, Response<ResponseSignupDto> response) {
                    System.out.println("response = " + response);
                }

                @Override
                public void onFailure(Call<ResponseSignupDto> call, Throwable t) {
                    System.out.println("t.getMessage() = " + t.getMessage());
                    System.out.println("t.getStackTrace() = " + t.getStackTrace());
                }
            });
            return true;
        } else {
            return false;
        }
    }
}