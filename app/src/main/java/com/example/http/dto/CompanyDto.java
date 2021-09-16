package com.example.http.dto;


import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import okhttp3.ResponseBody;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {

    private Long companyId;
    private String name;
    private String location;
    private String picture;
}
