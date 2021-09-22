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
public class ResponseCompanyWorkerListDto {

    private Long memberId;
    private String memberName;
    private String memberBirth;
    private String memberRole;
    private Long chatCount;
}
