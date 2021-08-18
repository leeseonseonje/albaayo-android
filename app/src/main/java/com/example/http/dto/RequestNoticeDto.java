package com.example.http.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestNoticeDto {

    private String title;
    private String contents;
    private List<NoticeImageDto> image;
}