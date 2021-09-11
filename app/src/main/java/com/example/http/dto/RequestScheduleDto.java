package com.example.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestScheduleDto {

    private Long companyId;

    private Long memberId;

    private String workSchedule;

    private String date;
}
