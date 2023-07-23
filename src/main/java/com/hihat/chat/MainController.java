package com.hihat.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;

@Controller
public class MainController {

    @RequestMapping("/")
    public String mainView() {
        return "main";
    }

    @ResponseBody
    @RequestMapping("/createId")
    public String createId() {
        return generateClientId();
    }

    private String generateClientId() {
        // 고유한 게임 방 ID를 생성하여 반환합니다.
        // 적절한 로직으로 ID를 생성하도록 구현합니다.
        Random random = new Random();
        StringBuilder randomBuf = new StringBuilder();
        randomBuf.append("Player-");
        for (int i = 0; i < 6; i++) {
            // Random.nextBoolean() : 랜덤으로 true, false 리턴 (true : 랜덤 소문자 영어, false : 랜덤 숫자)
            if (random.nextBoolean()) {
                // 26 : a-z 알파벳 개수
                // 97 : letter 'a' 아스키코드
                // (int)(random.nextInt(26)) + 97 : 랜덤 소문자 아스키코드
                randomBuf.append((char)(random.nextInt(26) + 97));
            } else {
                randomBuf.append(random.nextInt(10));
            }
        }
        return randomBuf.toString();
    }
}
