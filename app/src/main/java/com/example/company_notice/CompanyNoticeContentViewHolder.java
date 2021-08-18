package com.example.company_notice;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyNoticeContentViewHolder extends RecyclerView.ViewHolder {

    private ImageView image;
    private TextView imageText;

    public CompanyNoticeContentViewHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.notice_image);
        imageText = itemView.findViewById(R.id.notice_image_content);
    }
}