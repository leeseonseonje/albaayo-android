package com.example.albaayo.personalchat;

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
public class RequestPersonalChatMessage {

    private Long sendMemberId;
    private Long recvMemberId;
    private String name;
    private String message;
}
