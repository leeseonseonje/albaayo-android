package com.example.http.dto;

import java.util.ArrayList;
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
public class RequestNoticeUpdateDto {

    private Long noticeId;
    private String title;
    private String contents;
    private List<NoticeImageDto> imageList;
}
