package com.example.company_notice;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.albaayo.WorkerCompanyMain;
import com.example.albaayo.employer.NoticeContent;
import com.example.company_main.CompanyMainViewHolder;
import com.example.http.dto.IdAndName;
import com.example.http.dto.ResponseNoticeListDto;

import java.util.List;

public class CompanyNoticeAdapter extends RecyclerView.Adapter<CompanyNoticeViewHolder> {

    private List<ResponseNoticeListDto> list;
    private String companyName;

    public CompanyNoticeAdapter(List<ResponseNoticeListDto> list, String companyName) {
        this.list = list;
        this.companyName = companyName;
    }

    @NonNull
    @Override
    public CompanyNoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.company_notice, parent, false);

        return new CompanyNoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyNoticeViewHolder holder, int position) {
        holder.getNoticeTitle().setText("   " + list.get(position).getTitle());
        holder.getNoticeName().setText(list.get(position).getName());
        holder.getNoticeDate().setText(list.get(position).getDate());
        holder.setId(list.get(position).getId());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), NoticeContent.class);
            intent.putExtra("companyName", companyName);
            intent.putExtra("noticeId", list.get(position).getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
