package com.hihat.chat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hihat.chat.model.Room;
import com.hihat.chat.model.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class YachtWebSocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Map<String, Room> gameRooms = new ConcurrentHashMap<>();

    @MessageMapping("/createPublicRoom/{clientId}/{roomName}")
    public void createPublicRoom(@DestinationVariable String clientId, @DestinationVariable String roomName) throws IOException {
        log.info("================ createPublicRoom");
        if (clientId == null || roomName == null) {
            return;
        }
        if (isPlayerInGameRoom(clientId)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("이미 방에 입장해 있습니다.")));
            return;
        }
        if (existGameRoom(roomName)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("이미 존재하는 방입니다.")));
            return;
        }

        Room gameRoom = new Room(roomName, true);
        gameRoom.addPlayer(new Player(clientId, clientId));
        gameRooms.put(roomName, gameRoom);
        sendRoomList();
        sendChat(roomName, new MessageDTO(MessageType.SYSTEM, createSysMsg("공개방 ["+roomName+"]이 생성되었습니다.")));
        sendGameMsg(clientId, new MessageDTO(MessageType.GAME_INIT, clientId));
    }

    @MessageMapping("/createPrivateRoom/{clientId}")
    public void createPrivateRoom(@DestinationVariable String clientId) throws IOException {
        log.info("================ createPrivateRoom");
        if (clientId == null) {
            return;
        }
        if (isPlayerInGameRoom(clientId)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("이미 방에 입장해 있습니다.")));
            return;
        }
        String roomName = generateUniqueRoomId();   // 방 코드 생성
        if (existGameRoom(roomName)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("이미 존재하는 방입니다.")));
            return;
        }

        Room gameRoom = new Room(roomName, false);
        gameRoom.addPlayer(new Player(clientId, clientId));
        gameRooms.put(roomName, gameRoom);
        broadcast(roomName, "createRoomInfo/" + clientId);
        sendRoomList();
        sendChat(roomName, new MessageDTO(MessageType.SYSTEM, createSysMsg("비공개방이 생성되었습니다.")));
        sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("접속 코드 : " + roomName)));
        sendGameMsg(clientId, new MessageDTO(MessageType.GAME_INIT, clientId));
    }

    @MessageMapping("/joinRoom/{roomId}/{clientId}")
    public void joinRoom(@DestinationVariable String roomId, @DestinationVariable String clientId) throws IOException {
        log.info("================ joinRoom");
        if (clientId == null || roomId == null) {
            return;
        }
        if (isPlayerInGameRoom(clientId)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("이미 방에 입장해 있습니다.")));
            return;
        }
        if (!existGameRoom(roomId)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("존재하지 않는 방입니다.")));
            return;
        }

        Room room = gameRooms.get(roomId);
        if (room != null && room.getNumPlayers() < 6) {
            room.addPlayer(new Player(clientId, clientId));

            // 방 참여 알림 메시지 전송
//                simpMessagingTemplate.convertAndSend("/topic/" + roomId, "Player-" + clientId + " has joined the room.");
            sendTotalInfo();
            sendChat(roomId, new MessageDTO(MessageType.SYSTEM, createSysMsg("["+clientId+"]이 입장하였습니다.")));
            sendGameMsg(clientId, new MessageDTO(MessageType.GAME_INIT, clientId));
        }
    }

    @MessageMapping("/leaveRoom/{roomId}/{clientId}")
    public void leaveRoom(@DestinationVariable String roomId, @DestinationVariable String clientId) throws IOException {
        log.info("================ leaveRoom");
        if (clientId == null || roomId == null) {
            return;
        }
        if (!isPlayerInGameRoom(clientId)) {
            sendChat(roomId, new MessageDTO(MessageType.SYSTEM, createSysMsg("방에 입장한 상태가 아닙니다.")));
            return;
        }
        if (!existGameRoom(roomId)) {
            sendChat(clientId, new MessageDTO(MessageType.SYSTEM, createSysMsg("존재하지 않는 방입니다.")));
            return;
        }

        Room gameRoom = gameRooms.get(roomId);
        if (gameRoom != null) {
            gameRoom.removePlayer(gameRoom.findPlayer(clientId));
            log.info("[{}] : total - {}", roomId, gameRoom.getNumPlayers());
            if (gameRoom.getNumPlayers() < 1) {
                gameRooms.remove(roomId);
                sendRoomList();
            }
            // 나갔음을 알리는 메시지를 전송
//                simpMessagingTemplate.convertAndSend("/topic/" + roomId, "Player-" + clientId + " has left the room.");
            sendChat(roomId, new MessageDTO(MessageType.SYSTEM, createSysMsg("["+clientId+"] 님이 떠나셨습니다.")));
        }
    }

    @MessageMapping("/loadRoomList")
    public void sendRoomList() throws IOException {
        Map<String, Room> publicGameRooms = gameRooms.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getIsPublic()) // isPublic이 true인 객체들만 필터링
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        broadcast(serializeToJson(publicGameRooms), "loadRoomList");
        sendTotalInfo();    // 방, 플레이어 수도 보냄
    }

    @MessageMapping("/chat/{roomId}/{clientId}")
    public void chat(@DestinationVariable String roomId, @DestinationVariable String clientId, @Payload MessageDTO messageDTO) throws JsonProcessingException {
        if (roomId == null || clientId == null || messageDTO == null) {
            return;
        }
        messageDTO.setMessage(createMsg(clientId, messageDTO.getMessage()));
        sendChat(roomId, messageDTO);
    }

    @MessageMapping("/startGame/{roomId}")
    public void startGame(@DestinationVariable String roomId) {
        if (roomId == null) {
            return;
        }
        Room gameRoom = gameRooms.get(roomId);
        if (gameRoom != null && gameRoom.getNumPlayers() > 1) {
            // 게임 시작 로직 구현
            // ...
        }
    }

    private String generateUniqueRoomId() {
        return UUID.randomUUID().toString();
    }

    private Room findGameRoomByPlayerId(String playerId) {
        // 플레이어 ID를 기반으로 해당 플레이어가 속한 방을 찾아 반환합니다.
        for (Room room : gameRooms.values()) {
            for (Player player : room.getAllPlayers()) {
                if (player.getPlayerId().equals(playerId)) {
                    return room;
                }
            }
        }
        return null;
    }

    private boolean isPlayerInGameRoom(String playerId) {
        // 플레이어 ID를 기반으로 해당 플레이어가 속한 방을 찾아 반환합니다.
        for (Room room : gameRooms.values()) {
            for (Player player : room.getAllPlayers()) {
                if (player.getPlayerId().equals(playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existGameRoom(String roomId) {
        // 플레이어 ID를 기반으로 해당 플레이어가 속한 방을 찾아 반환합니다.
        return gameRooms.containsKey(roomId);
    }

    private void sendGameRoomInfo(Room room) {
        // 게임 방 정보를 연결된 모든 클라이언트에게 전송합니다.
        String gameRoomInfo = buildGameRoomInfo(room);
        broadcast(gameRoomInfo, "createRoom");
    }

    private void sendTotalInfo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("room", String.valueOf(gameRooms.size()));

        int sumPlayers = 0;
        for (Room gameRoom : gameRooms.values()) {
            sumPlayers += gameRoom.getNumPlayers();
        }
        jsonObject.put("player", sumPlayers);
        broadcast(jsonObject.toJSONString(), "total");
    }

    private String buildGameRoomInfo(Room gameRoom) {
        // 게임 방 정보를 JSON 형식으로 빌드합니다.
        // 적절한 로직으로 JSON을 구성하도록 구현합니다.
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomId", gameRoom.getRoomId());

        JSONArray playersArray = new JSONArray();
        for (Player player : gameRoom.getAllPlayers()) {
            JSONObject playerObject = new JSONObject();
            playerObject.put("id", player.getPlayerId());
            playerObject.put("name", player.getPlayerName());
            playersArray.add(playerObject);
        }
        jsonObject.put("players", playersArray);

        return jsonObject.toJSONString();
    }



    // 객체를 JSON 문자열로 직렬화
    public String serializeToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

    // JSON 문자열을 객체로 역직렬화
    public MessageDTO deserializeFromJson(String payload) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload.substring(payload.lastIndexOf("{"), payload.lastIndexOf("}") + 1)); // JSON 문자열을 JsonNode 객체로 변환

        // JsonNode 객체에서 원하는 데이터 추출
        return new MessageDTO(jsonNode.get("type").asText(), jsonNode.get("message").asText());
    }

    private String createSysMsg(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        return sdf.format(new Date()) + " -  - " + message;
    }

    private String createMsg(String clientId, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        Room gameRoom = findGameRoomByPlayerId(clientId);
        if (gameRoom != null) {
            Player player = gameRoom.findPlayer(clientId);
            return sdf.format(new Date()) + " - [" + player.getPlayerName() + "] - " +  message;
        }
        return sdf.format(new Date()) + " - ㅤ - ERROR";
    }

    public void broadcast(String message, String endPoint) {
        simpMessagingTemplate.convertAndSend("/topic/" + endPoint, message);
    }

    private void sendChat(String from, MessageDTO messageDTO) throws JsonProcessingException {
        simpMessagingTemplate.convertAndSend("/topic/chat/" + from, serializeToJson(messageDTO));
    }

    private void sendGameMsg(String from, MessageDTO messageDTO) throws JsonProcessingException {
        simpMessagingTemplate.convertAndSend("/topic/game/" + from, serializeToJson(messageDTO));
    }
}


