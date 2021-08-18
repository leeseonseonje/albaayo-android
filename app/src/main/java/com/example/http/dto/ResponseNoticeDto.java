package com.example.http.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseNoticeDto {

    private Long noticeId;
    private Long memberId;
    private String name;
    private String title;
    private String contents;
    private String date;
    private List<NoticeImageDto> imageList;
}
