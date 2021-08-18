package com.example.http.dto;

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
public class RequestCompanyDto {

    private String name;
    private String location;
    private String businessRegistrationNumber;
    private String picture;
}
