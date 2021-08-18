package com.example.http.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSignupDto {

    private String userId;
    private String password;
    private String email;
    private String name;
    private String birth;

    @Builder
    public RequestSignupDto(String userId, String password, String email, String name, String birth) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.birth = birth;
    }
}
