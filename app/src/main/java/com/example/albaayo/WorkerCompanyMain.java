package com.example.albaayo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.chat.CompanyChat;
import com.example.albaayo.option.EmployerGroupOption;
import com.example.albaayo.option.WorkerGroupOption;
import com.example.commute.CommuteAdapter;
import com.example.company_main.CompanyMainAdapter;
import com.example.company_notice.CompanyNoticeAdapter;
import com.example.http.Http;
import com.example.http.dto.Id;
import com.example.http.dto.IdAndName;
import com.example.http.dto.RequestScheduleDto;
import com.example.http.dto.ResponseCommuteListDto;
import com.example.http.dto.ResponseCompanyWorkerListDto;
import com.example.http.dto.ResponseNoticeListDto;
import com.example.http.dto.ResponseScheduleDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkerCompanyMain extends AppCompatActivity {

    private TextView headerName;
    private Button home, main, notice, commute, commuteList, schedule, chatting, optionButton;
    private ImageView noticeRegister;
    private Long companyId;
    private String companyName, companyLocation;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    List<ResponseNoticeListDto> list;
    private ProgressDialog progressDialog;
    private ConstraintLayout calendarLayout;
    private CalendarView calendarView;
    private TextView dateText;
    private EditText inputSchedule;
    private Button scheduleAdd;
    private String date;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.worker_company_main);

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");
        companyLocation = intent.getStringExtra("companyLocation");

        recyclerView = findViewById(R.id.recycler_view);
        manager = new LinearLayoutManager(WorkerCompanyMain.this, LinearLayoutManager.VERTICAL,false);
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(),LinearLayoutManager.VERTICAL);
//        dividerItemDecoration.setDrawable(getApplication().getResources().getDrawable(R.drawable.c));
//        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(manager);

        calendarLayout = findViewById(R.id.calendar_layout);
        calendarView = findViewById(R.id.calendar);
        dateText = findViewById(R.id.date);
        inputSchedule = findViewById(R.id.input_schedule);

        headerName = findViewById(R.id.header_name_text);
        headerName.setText(companyName);
        main = findViewById(R.id.company_main);
        mainList(companyId);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        header();
        footer();
        noticeRegisterActivity();

    }

    private void noticeRegisterActivity() {
        noticeRegister.setOnClickListener(v -> {
            Intent noticeRegisterIntent = new Intent(WorkerCompanyMain.this, CompanyNoticeRegister.class);
            noticeRegisterIntent.putExtra("companyId", companyId);
            noticeRegisterIntent.putExtra("companyName", companyName);
            startActivity(noticeRegisterIntent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void header() {
        notice = findViewById(R.id.notice);
        noticeRegister = findViewById(R.id.notice_register);
        notice.setOnClickListener(v -> {
            progressDialog.show();
            recyclerView.setVisibility(View.VISIBLE);
            calendarLayout.setVisibility(View.GONE);
            noticeRegister.setVisibility(View.VISIBLE);
            commuteList.setTextColor(Color.BLACK);
            schedule.setTextColor(Color.BLACK);
            main.setTextColor(Color.BLACK);
            notice.setTextColor(Color.parseColor("#8ABFE8"));
            int page = 0;
            Call<List<ResponseNoticeListDto>> call = Http.getInstance().getApiService()
                    .noticeList(Id.getInstance().getAccessToken(), companyId, page);
            call.enqueue(new Callback<List<ResponseNoticeListDto>>() {
                @Override
                public void onResponse(Call<List<ResponseNoticeListDto>> call, Response<List<ResponseNoticeListDto>> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<List<ResponseNoticeListDto>> reCall = Http.getInstance().getApiService()
                                .noticeList(Id.getInstance().getAccessToken(), companyId, page);
                        reCall.enqueue(new Callback<List<ResponseNoticeListDto>>() {
                            @Override
                            public void onResponse(Call<List<ResponseNoticeListDto>> call, Response<List<ResponseNoticeListDto>> response) {
                                list = response.body();
                                recyclerView.setAdapter(new CompanyNoticeAdapter(list, companyName));
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<List<ResponseNoticeListDto>> call, Throwable t) {

                            }
                        });
                    } else {
                        list = response.body();
                        recyclerView.setAdapter(new CompanyNoticeAdapter(list, companyName));
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<List<ResponseNoticeListDto>> call, Throwable t) {
                    Toast.makeText(WorkerCompanyMain.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });

        commuteList = findViewById(R.id.commute_List);
        commuteList.setOnClickListener(v -> {
            progressDialog.show();
            recyclerView.setVisibility(View.VISIBLE);
            calendarLayout.setVisibility(View.GONE);
            main.setTextColor(Color.BLACK);
            notice.setTextColor(Color.BLACK);
            schedule.setTextColor(Color.BLACK);
            commuteList.setTextColor(Color.parseColor("#8ABFE8"));
            if (noticeRegister.getVisibility() == View.VISIBLE) {
                noticeRegister.setVisibility(View.GONE);
            }
            Call<List<ResponseCommuteListDto>> commuteCall = Http.getInstance().getApiService()
                    .commuteList(Id.getInstance().getAccessToken(), Id.getInstance().getId(), companyId);
            commuteCall.enqueue(new Callback<List<ResponseCommuteListDto>>() {
                @Override
                public void onResponse(Call<List<ResponseCommuteListDto>> call, Response<List<ResponseCommuteListDto>> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<List<ResponseCommuteListDto>> commuteCall = Http.getInstance().getApiService()
                                .commuteList(Id.getInstance().getAccessToken(), Id.getInstance().getId(), companyId);
                        commuteCall.enqueue(new Callback<List<ResponseCommuteListDto>>() {
                            @Override
                            public void onResponse(Call<List<ResponseCommuteListDto>> call, Response<List<ResponseCommuteListDto>> response) {
                                recyclerView.setAdapter(new CommuteAdapter(response.body()));
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<List<ResponseCommuteListDto>> call, Throwable t) {
                            }
                        });
                    } else {
                        recyclerView.setAdapter(new CommuteAdapter(response.body()));
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<List<ResponseCommuteListDto>> call, Throwable t) {
                }
            });
        });

        schedule = findViewById(R.id.schedule);
        schedule.setOnClickListener(v -> {
            progressDialog.show();
            main.setTextColor(Color.BLACK);
            notice.setTextColor(Color.BLACK);
            commuteList.setTextColor(Color.BLACK);
            schedule.setTextColor(Color.parseColor("#8ABFE8"));
            if (noticeRegister.getVisibility() == View.VISIBLE) {
                noticeRegister.setVisibility(View.GONE);
            }

            recyclerView.setVisibility(View.GONE);
            calendarLayout.setVisibility(View.VISIBLE);
            if (dateText.getText().toString().replace(" ", "").equals("")) {
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 dd일"));
                dateText.setText(date);
                schedule(date);
            }
            progressDialog.dismiss();

            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                progressDialog.show();
                date = String.format("%d년 %d월 %d일", year, month + 1, dayOfMonth);
                dateText.setText(date);
                schedule(date);
                System.out.println(date);
                progressDialog.dismiss();

            });


            scheduleAdd = findViewById(R.id.add_button);
            scheduleAdd.setOnClickListener(v1 -> {
                progressDialog.show();
                RequestScheduleDto request = RequestScheduleDto.builder().companyId(companyId).workSchedule(inputSchedule.getText().toString()).date(date).build();
                Call<Void> call = Http.getInstance().getApiService()
                        .registerSchedule(Id.getInstance().getAccessToken(), request);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.code() == 401) {
                            Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                            editor.putString("accessToken", response.headers().get("Authorization"));
                            editor.commit();

                            Call<Void> reCall = Http.getInstance().getApiService()
                                    .registerSchedule(Id.getInstance().getAccessToken(), request);
                            reCall.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    progressDialog.dismiss();
                                    new AlertDialog.Builder(WorkerCompanyMain.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                            .setMessage("일정 등록 완료")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            })
                                            .show();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(WorkerCompanyMain.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            new AlertDialog.Builder(WorkerCompanyMain.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                    .setMessage("일정 등록 완료")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(WorkerCompanyMain.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            progressDialog.dismiss();

        });

        main.setOnClickListener(v -> {
            progressDialog.show();
            recyclerView.setVisibility(View.VISIBLE);
            calendarLayout.setVisibility(View.GONE);
            commuteList.setTextColor(Color.BLACK);
            notice.setTextColor(Color.BLACK);
            schedule.setTextColor(Color.BLACK);
            main.setTextColor(Color.parseColor("#8ABFE8"));
            if (noticeRegister.getVisibility() == View.VISIBLE) {
                noticeRegister.setVisibility(View.GONE);
            }
            mainList(companyId);
            progressDialog.dismiss();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void schedule(String date) {
        Call<ResponseScheduleDto> scheduleCall = Http.getInstance().getApiService()
                .schedule(Id.getInstance().getAccessToken(), companyId, date);
        scheduleCall.enqueue(new Callback<ResponseScheduleDto>() {
            @Override
            public void onResponse(Call<ResponseScheduleDto> call, Response<ResponseScheduleDto> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<ResponseScheduleDto> scheduleCall = Http.getInstance().getApiService()
                            .schedule(Id.getInstance().getAccessToken(), companyId, date);
                    scheduleCall.enqueue(new Callback<ResponseScheduleDto>() {
                        @Override
                        public void onResponse(Call<ResponseScheduleDto> call, Response<ResponseScheduleDto> response) {
                            if (response.body() != null)
                                inputSchedule.setText(response.body().getWorkSchedule());
                        }

                        @Override
                        public void onFailure(Call<ResponseScheduleDto> call, Throwable t) {

                        }
                    });
                } else {
                    if (response.body() != null)
                        inputSchedule.setText(response.body().getWorkSchedule());
                }
            }

            @Override
            public void onFailure(Call<ResponseScheduleDto> call, Throwable t) {

            }
        });
    }

    private void footer() {
        commute = findViewById(R.id.commute);
        commute.setOnClickListener(v -> {
            Intent intent = new Intent(WorkerCompanyMain.this, WorkerCommute.class);
            intent.putExtra("companyId", companyId);
            intent.putExtra("companyName", companyName);
            intent.putExtra("companyLocation", companyLocation);
            startActivity(intent);
        });

        chatting = findViewById(R.id.chatting);
        chatting.setOnClickListener(v -> {
            Intent intent = new Intent(WorkerCompanyMain.this, CompanyChat.class);
            intent.putExtra("companyId", companyId);
            intent.putExtra("companyName", companyName);
            startActivity(intent);
        });

        home = findViewById(R.id.home);
        home.setOnClickListener(v -> {
            finish();
        });

        optionButton = findViewById(R.id.my_page);
        optionButton.setOnClickListener(v -> {
            Intent intent = new Intent(WorkerCompanyMain.this, WorkerGroupOption.class);
            intent.putExtra("companyId", companyId);
            intent.putExtra("companyName", companyName);
            startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void mainList(long companyId) {
        Call<List<ResponseCompanyWorkerListDto>> call = Http.getInstance().getApiService()
                .companyMain(Id.getInstance().getAccessToken(), companyId);
        call.enqueue(new Callback<List<ResponseCompanyWorkerListDto>>() {
            @Override
            public void onResponse(Call<List<ResponseCompanyWorkerListDto>> call, Response<List<ResponseCompanyWorkerListDto>> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<List<ResponseCompanyWorkerListDto>> reCall = Http.getInstance().getApiService()
                            .companyMain(Id.getInstance().getAccessToken(), companyId);
                    reCall.enqueue(new Callback<List<ResponseCompanyWorkerListDto>>() {
                        @Override
                        public void onResponse(Call<List<ResponseCompanyWorkerListDto>> call, Response<List<ResponseCompanyWorkerListDto>> response) {
                            response.body().add(0, ResponseCompanyWorkerListDto.builder().memberName("<EMPLOYER>").build());
                            response.body().add(2, ResponseCompanyWorkerListDto.builder().memberName("<WORKER>").build());
                            recyclerView.setAdapter(new CompanyMainAdapter(response.body(), companyId, companyName, companyLocation, sf, editor));
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<List<ResponseCompanyWorkerListDto>> call, Throwable t) {

                        }
                    });
                } else {
                    response.body().add(0, ResponseCompanyWorkerListDto.builder().memberName("<EMPLOYER>").build());
                    response.body().add(2, ResponseCompanyWorkerListDto.builder().memberName("<WORKER>").build());
                    recyclerView.setAdapter(new CompanyMainAdapter(response.body(), companyId, companyName, companyLocation, sf, editor));
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<List<ResponseCompanyWorkerListDto>> call, Throwable t) {
                Toast.makeText(WorkerCompanyMain.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onRestart() {
        super.onRestart();
        if (notice.getCurrentTextColor() == Color.parseColor("#8ABFE8")) {
            Call<List<ResponseNoticeListDto>> call = Http.getInstance().getApiService()
                    .noticeList(Id.getInstance().getAccessToken(), companyId, 0);
            call.enqueue(new Callback<List<ResponseNoticeListDto>>() {
                @Override
                public void onResponse(Call<List<ResponseNoticeListDto>> call, Response<List<ResponseNoticeListDto>> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<List<ResponseNoticeListDto>> reCall = Http.getInstance().getApiService()
                                .noticeList(Id.getInstance().getAccessToken(), companyId, 0);
                        reCall.enqueue(new Callback<List<ResponseNoticeListDto>>() {
                            @Override
                            public void onResponse(Call<List<ResponseNoticeListDto>> call, Response<List<ResponseNoticeListDto>> response) {
                                list = response.body();
                                recyclerView.setAdapter(new CompanyNoticeAdapter(list, companyName));
                            }

                            @Override
                            public void onFailure(Call<List<ResponseNoticeListDto>> call, Throwable t) {
                                Toast.makeText(WorkerCompanyMain.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        list = response.body();
                        recyclerView.setAdapter(new CompanyNoticeAdapter(list, companyName));
                    }
                }

                @Override
                public void onFailure(Call<List<ResponseNoticeListDto>> call, Throwable t) {
                    Toast.makeText(WorkerCompanyMain.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (main.getCurrentTextColor() == Color.parseColor("#8ABFE8")) {
            mainList(companyId);
        } else if (schedule.getCurrentTextColor() == Color.parseColor("#8ABFE8")) {
            progressDialog.show();
            recyclerView.setVisibility(View.GONE);
            calendarLayout.setVisibility(View.VISIBLE);
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 dd일"));

            dateText.setText(date);
            schedule(date);
            progressDialog.dismiss();

            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                progressDialog.show();
                date = String.format("%d년 %d월 %d일", year, month + 1, dayOfMonth);
                System.out.println("dayOfMonth = " + dayOfMonth);
                dateText.setText(date);
                schedule(date);
                System.out.println(date);
                progressDialog.dismiss();

            });

        }
    }
}
