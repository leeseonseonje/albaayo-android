package com.example.list.accept_company;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.http.Http;
import com.example.http.dto.CompanyDto;
import com.example.http.dto.Id;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotAcceptCompanyListAdapter extends RecyclerView.Adapter<NotAcceptCompanyListViewHolder> {

    private List<CompanyDto> list;
    private TextView emptyText;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    public NotAcceptCompanyListAdapter(List<CompanyDto> list, TextView emptyText, SharedPreferences sf, SharedPreferences.Editor editor) {
        this.list = list;
        this.emptyText = emptyText;
        this.sf = sf;
        this.editor = editor;
    }

    @NonNull
    @Override
    public NotAcceptCompanyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.not_accept_card_view, parent, false);

        return new NotAcceptCompanyListViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull NotAcceptCompanyListViewHolder viewHolder, int position) {

        viewHolder.getName().setText(list.get(position).getName());
        viewHolder.getLocation().setText(list.get(position).getLocation());
        viewHolder.setId(list.get(position).getCompanyId());
        
        viewHolder.getAcceptButton().setOnClickListener(v -> {
            Call<Void> call = Http.getInstance().getApiService()
                    .acceptCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list.get(position).getCompanyId());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<Void> reCall = Http.getInstance().getApiService()
                                .acceptCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list.get(position).getCompanyId());
                        reCall.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                list.remove(position);
                                notifyDataSetChanged();
                                if (list.size() == 0) {
                                    emptyText.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
                    } else {
                        list.remove(position);
                        notifyDataSetChanged();
                        if (list.size() == 0) {
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        });

        viewHolder.getRefusalButton().setOnClickListener(v -> {
            Call<Void> call = Http.getInstance().getApiService()
                    .notAcceptCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list.get(position).getCompanyId());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<Void> reCall = Http.getInstance().getApiService()
                                .notAcceptCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list.get(position).getCompanyId());

                        reCall.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                list.remove(position);
                                notifyDataSetChanged();
                                if (list.size() == 0) {
                                    emptyText.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
                    } else {
                        list.remove(position);
                        notifyDataSetChanged();
                        if (list.size() == 0) {
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

