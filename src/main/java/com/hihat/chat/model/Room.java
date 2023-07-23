package com.hihat.chat.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
public class Room {
    private String roomId;
    private Boolean isPublic;
    private List<Player> players;
    // 게임 진행에 필요한 정보 및 상태를 추가로 정의할 수 있습니다.

    public Room(String roomId, boolean isPublic) {
        this.roomId = roomId;
        this.isPublic = isPublic;
        this.players = new ArrayList<>();
    }

    // 게임 방에 플레이어 추가
    public void addPlayer(Player player) {
        players.add(player);
    }

    // 게임 방에서 플레이어 찾기
    public Player findPlayer(String clientId) {
        Optional<Player> fplayer = players.stream()
                .filter(p -> p.getPlayerId().equals(clientId))
                .findFirst();
        if (fplayer.isPresent()) {
            return fplayer.get();
        }
        return null;
    }

    // 게임 방에서 플레이어 제거
    public void removePlayer(Player player) {
        players.remove(player);
    }

    // 게임 방에서 플레이어 제거
    public void removePlayer(int index) {
        players.remove(index);
    }

    // 게임 방에 참여한 플레이어 수 반환
    public int getNumPlayers() {
        return players.size();
    }

    // 게임 방에 참여한 모든 플레이어 반환
    public List<Player> getAllPlayers() {
        return players;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public String getRoomId() {
        return roomId;
    }
}
