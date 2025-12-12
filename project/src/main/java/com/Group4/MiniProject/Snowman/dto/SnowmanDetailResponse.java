package com.Group4.MiniProject.Snowman.dto;

import com.Group4.MiniProject.Message.entity.Message;
import com.Group4.MiniProject.Snowman.entity.SnowmanEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SnowmanDetailResponse {
    private Long snowmanId;

    // 편지 관련 정보
    private UUID messageUuid;

    private boolean isOpened;
    private String themeName;
    private String messageContent;
    private String senderNickname;
    private String receiverNickname;

    // 엔티티를 DTO로 변환
    public static SnowmanDetailResponse from(SnowmanEntity snowman){
        Message message = snowman.getOpenMessage();

        // 아직 사용하지 않은 눈사람
        if(message == null){
            return SnowmanDetailResponse.builder()
                    .snowmanId(snowman.getId())
                    .isOpened(false)
                    .build();
        }

        // 사용한 눈사람은 편지 정보를 채워서 반환
        return SnowmanDetailResponse.builder()
                .snowmanId(snowman.getId())
                .isOpened(true)
                .messageUuid(message.getUuid())
                .themeName(message.getTheme().getName())
                .messageContent(message.getMessage())
                .senderNickname(message.getNickname())

                // 받는 사람이 탈퇴했을 경우를 대비해서 예외처리
                .receiverNickname(message.getReceivedUser() != null
                ? message.getReceivedUser().getNickname(): "알 수 없음")
                .build();
    }
}
