package com.example.albaayo.chat;


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
public class RequestChattingMessage {

    private Long memberId;
    private Long companyId;
    private String name;
    private String message;
}

