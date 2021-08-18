package com.example.company_notice;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyNoticeRegisterViewHolder extends RecyclerView.ViewHolder {

    private ImageView image;
    private EditText imageText;
    private Button imageDeleteButton;

    public CompanyNoticeRegisterViewHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.notice_register_image_view);
        imageText = itemView.findViewById(R.id.notice_register_image_view_edit);
        imageDeleteButton = itemView.findViewById(R.id.image_delete_button);
    }
}
