package com.example.http.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseSignupDto {

    private Long id;
    private String name;

    public ResponseSignupDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
