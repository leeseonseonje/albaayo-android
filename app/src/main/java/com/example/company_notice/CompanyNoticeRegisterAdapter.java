package com.example.company_notice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.albaayo.NoticeImageFull;
import com.example.albaayo.R;
import com.example.albaayo.WorkerCompanyMain;
import com.example.http.dto.ImageDto;
import com.example.http.dto.NoticeImageDto;
import com.example.http.dto.ResponseNoticeListDto;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class CompanyNoticeRegisterAdapter extends RecyclerView.Adapter<CompanyNoticeRegisterViewHolder> {

    private List<ImageDto> list;
    private List<NoticeImageDto> imageList;

    public CompanyNoticeRegisterAdapter(List<ImageDto> list) {
        this.list = list;
    }



    @NonNull
    @Override
    public CompanyNoticeRegisterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.notice_image_view, parent, false);

        return new CompanyNoticeRegisterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyNoticeRegisterViewHolder holder, int position) {
        holder.getImage().setImageBitmap(list.get(position).getImage());
        holder.getImageText().setText(list.get(position).getText());
        holder.getImageText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (position < list.size()) {
                    list.get(holder.getAdapterPosition()).setText(s.toString()
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        holder.getImageDeleteButton().setOnClickListener(v -> {
            list.remove(position);
            notifyDataSetChanged();
        });

        holder.getImage().setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), NoticeImageFull.class);
            intent.putExtra("image", list.get(position).getPath());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItem(ImageDto imageDto){
        list.add(imageDto);
        notifyDataSetChanged();
    }
}

