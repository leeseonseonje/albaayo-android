
package com.example.company_notice;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyNoticeViewHolder extends RecyclerView.ViewHolder {

    private Long id;
    private TextView noticeTitle;
    private TextView noticeName;
    private TextView noticeDate;

    public CompanyNoticeViewHolder(@NonNull View itemView) {
        super(itemView);

        noticeTitle = itemView.findViewById(R.id.notice_title);
        noticeName = itemView.findViewById(R.id.notice_name);
        noticeDate = itemView.findViewById(R.id.notice_date);
    }
}