package com.example.http.dto;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestNoticeDto {

    private String title;
    private String contents;
    private List<NoticeImageDto> image;

    @Getter
    @Setter
    public static class CompanyNoticeViewHolder extends RecyclerView.ViewHolder {

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
}