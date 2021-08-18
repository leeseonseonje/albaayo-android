package com.example.http.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseFindWorkerDto {

    private String userId;
    private String name;
    private String birth;
}

