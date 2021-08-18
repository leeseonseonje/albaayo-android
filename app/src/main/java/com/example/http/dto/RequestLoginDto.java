package com.example.http.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestLoginDto {
    private String userId;
    private String password;

    @Builder
    public RequestLoginDto(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
