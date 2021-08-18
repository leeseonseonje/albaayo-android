package com.example.http.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Id {

    private String accessToken;
    private Long id;
    private String role;
    private String name;
    private String userId;
    private static final Id instance = new Id();

    public static Id getInstance() {
        return instance;
    }
}
