package com.example.company_main;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.albaayo.location.EmployerLocationShare;
import com.example.albaayo.location.LocationDto;
import com.example.albaayo.option.WorkerGroupOption;
import com.example.http.Http;
import com.example.http.dto.Id;
import com.example.http.dto.ResponseCompanyWorkerListDto;
import com.example.http.dto.ResponsePayInformationDto;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompanyMainAdapter extends RecyclerView.Adapter<CompanyMainViewHolder> {

    private List<ResponseCompanyWorkerListDto> list;
    private Long companyId;
    private String companyName;
    private String companyLocation;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;
    private DatePickerDialog.OnDateSetListener DateSetListener;
    private String date;
    private int year, month, day;

    public CompanyMainAdapter(List<ResponseCompanyWorkerListDto> list, Long companyId, String companyName, String companyLocation,
                              SharedPreferences sf, SharedPreferences.Editor editor) {
        this.list = list;
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyLocation = companyLocation;
        this.sf = sf;
        this.editor = editor;
    }

    @NonNull
    @Override
    public CompanyMainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = null;
        if (Id.getInstance().getRole().equals("ROLE_EMPLOYER")) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.employer_company_main_view, parent, false);
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.worker_company_main_view, parent, false);
        }

        return new CompanyMainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyMainViewHolder holder, int position) {
        if (list.get(position).getMemberId() != null) {
            holder.getRoleLayout().setVisibility(View.GONE);
            holder.getListLayout().setVisibility(View.VISIBLE);

            if (list.get(position).getMemberId() == Id.getInstance().getId()) {
                holder.getWorkersName().setText(list.get(position).getMemberName() + " (나)");
                if (holder.getLocationShareButton() != null) {
                    holder.getLocationShareButton().setVisibility(View.GONE);
                }
            } else {
                holder.getWorkersName().setText(list.get(position).getMemberName());
            }

            holder.getWorkerBirth().setText(list.get(position).getMemberBirth());
            holder.setWorkerId(list.get(position).getMemberId());

            if (list.get(position).getChatCount() != 0) {
                if (list.get(position).getChatCount() > 999) {
                    holder.getCountImage().setVisibility(View.VISIBLE);
                    holder.getCountText().setVisibility(View.VISIBLE);
                    holder.getCountText().setText("...");
                }
                holder.getCountImage().setVisibility(View.VISIBLE);
                holder.getCountText().setVisibility(View.VISIBLE);
                holder.getCountText().setText(list.get(position).getChatCount().toString());
            }

        } else if (list.get(position).getMemberName().equals("<EMPLOYER>")) {

            holder.getListLayout().setVisibility(View.GONE);
            holder.getRoleLayout().setVisibility(View.VISIBLE);

            holder.getRole().setText("사장님");
        } else {

            holder.getListLayout().setVisibility(View.GONE);
            holder.getRoleLayout().setVisibility(View.VISIBLE);

            holder.getRole().setText("알바생");
        }

        if (holder.getLocationShareButton() != null) {
            holder.getLocationShareButton().setOnClickListener(v -> {

                Call<LocationDto> locationCall = Http.getInstance().getApiService()
                        .location(Id.getInstance().getAccessToken(), holder.getWorkerId(), companyId);
                locationCall.enqueue(new Callback<LocationDto>() {
                    @Override
                    public void onResponse(Call<LocationDto> call, Response<LocationDto> response) {
                        if (response.code() == 401) {
                            Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                            editor.putString("accessToken", response.headers().get("Authorization"));
                            editor.commit();

                            Call<LocationDto> locationCall = Http.getInstance().getApiService()
                                    .location(Id.getInstance().getAccessToken(), holder.getWorkerId(), companyId);
                            locationCall.enqueue(new Callback<LocationDto>() {
                                @Override
                                public void onResponse(Call<LocationDto> call, Response<LocationDto> response) {
                                    if (response.code() != 500) {
                                        Intent intent = new Intent(holder.itemView.getContext(), EmployerLocationShare.class);
                                        intent.putExtra("companyLocation", companyLocation);
                                        intent.putExtra("workerLocation", response.body().getLocation());
                                        intent.putExtra("workerName", holder.getWorkersName().getText().toString());
                                        holder.itemView.getContext().startActivity(intent);
                                    } else {
                                        new AlertDialog.Builder(holder.itemView.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage("근무 중이 아닙니다.")
                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<LocationDto> call, Throwable t) {
                                }
                            });
                        } else if (response.code() != 500) {
                            Intent intent = new Intent(holder.itemView.getContext(), EmployerLocationShare.class);
                            intent.putExtra("companyLocation", companyLocation);
                            intent.putExtra("workerLocation", response.body().getLocation());
                            intent.putExtra("workerName", holder.getWorkersName().getText().toString());
                            holder.itemView.getContext().startActivity(intent);
                        } else {
                            new AlertDialog.Builder(holder.itemView.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                    .setMessage("근무 중이 아닙니다.")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LocationDto> call, Throwable t) {
                    }
                });
            });
        }

        if (holder.getPayButton() != null) {
            holder.getPayButton().setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(holder.itemView.getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        DateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            });;
            DateSetListener = (view, year, month, day) -> {
                month = month + 1;
                Log.d("", "onDateSet: yyyy.MM.dd" + month + "." + day + "." + year);
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                    builder.setTitle(year + "년" + finalMonth + "월 급여정보");
                                    builder.setMessage(response.body().getPay());
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.getWindow().setGravity(Gravity.CENTER);
                                    alertDialog.show();
                                }

                                @Override
                                public void onFailure(Call<ResponsePayInformationDto> call, Throwable t) {

                                }
                            });
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            builder.setTitle(year + "년 " + finalMonth + "월 급여정보");
                            DecimalFormat df = new DecimalFormat("###,###");
                            builder.setMessage(df.format(response.body().getPay()) + "원");
                            AlertDialog alertDialog = builder.create();
                            alertDialog.getWindow().setGravity(Gravity.CENTER);
                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponsePayInformationDto> call, Throwable t) {
                    }
                });
            };
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
