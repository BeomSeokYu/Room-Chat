package com.hihat.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private String playerId;
    private String playerName;

    // 생성자, Getter 및 Setter 메서드 생략

    // 플레이어 아이디 반환
    public String getPlayerId() {
        return playerId;
    }

    // 플레이어 이름 반환
    public String getPlayerName() {
        return playerName;
    }
}