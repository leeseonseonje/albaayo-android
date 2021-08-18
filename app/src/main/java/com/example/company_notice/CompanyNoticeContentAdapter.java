package com.example.company_notice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.http.dto.NoticeImageDto;

import java.util.List;

public class CompanyNoticeContentAdapter extends RecyclerView.Adapter<CompanyNoticeContentViewHolder> {

    private List<NoticeImageDto> list;

    public CompanyNoticeContentAdapter(List<NoticeImageDto> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public CompanyNoticeContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.notice_content_view, parent, false);

        return new CompanyNoticeContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyNoticeContentViewHolder holder, int position) {
        String base = list.get(position).getImage();
        if (base != null) {
            byte[] b = Base64.decode(base, 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            holder.getImage().setImageBitmap(bitmap);
        }

        holder.getImageText().setText(list.get(position).getImageContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

