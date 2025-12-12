package com.Group4.MiniProject.Message.dto;

import com.Group4.MiniProject.Message.entity.Message;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateResponseDto {
    private UUID uuid;
    private String content;
    private String receivedUser;
    private String sendUser;
    private Long themeId;
    private boolean isOpen;


    public MessageCreateResponseDto(Message message) {
        this.uuid = message.getUuid();
        this.content = message.getMessage();
        this.sendUser = message.getNickname();
        this.isOpen = message.isOpen();

        if(message.getTheme() != null) {
            this.themeId = message.getTheme().getThemeId();
        }else {
            this.themeId = null;
        }

        if(message.getNickname() != null){
            this.receivedUser = message.getReceivedUser().getNickname();
        }else {
            this.receivedUser = null;
        }
    }
}