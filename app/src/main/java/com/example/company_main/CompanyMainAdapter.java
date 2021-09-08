package com.example.company_main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.albaayo.location.EmployerLocationShare;
import com.example.albaayo.location.LocationDto;
import com.example.http.dto.Id;
import com.example.http.dto.ResponseCompanyWorkerListDto;

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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
