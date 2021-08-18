package com.example.albaayo.personalchat;

import java.time.LocalDateTime;

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
public class ResponsePersonalChatMessage {

    private Long sendMemberId;
    private Long recvMemberId;
    private String name;
    private String message;
    private LocalDateTime time;
}
