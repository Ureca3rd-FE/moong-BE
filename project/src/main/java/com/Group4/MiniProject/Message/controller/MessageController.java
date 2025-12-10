package com.Group4.MiniProject.Message.controller;

import com.Group4.MiniProject.Message.dto.MessageCreateRequestDto;
import com.Group4.MiniProject.Message.dto.MessageCreateResponseDto;
import com.Group4.MiniProject.Message.dto.MessageListResponseDto;
import com.Group4.MiniProject.Message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 메시지 작성 API
    @PostMapping
    public ResponseEntity<String> createMessage(@Valid @RequestBody MessageCreateRequestDto requestDto) {
        String senderNickname = requestDto.getSenderNickname();
        messageService.createMessage(requestDto, senderNickname);
        return ResponseEntity.ok("ok");
    }

    @GetMapping
    public ResponseEntity<List<MessageListResponseDto>> getMessageList(
            @RequestParam(name = "userId") Long userId // userId 쿼리 파라미터로 받습니다.
    ) {
        // Service 메서드 호출
        List<MessageListResponseDto> messages =
                messageService.getMessageListByUserIdAndOpenStatus(userId);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unopened")
    public ResponseEntity<List<MessageListResponseDto>> getUnopenedMessageList(
            @RequestParam(name = "userId") Long userId // userId 쿼리 파라미터로 받습니다.
    ) {
        List<MessageListResponseDto> messages = messageService.getUnopenedMessageList(userId);
        return ResponseEntity.ok(messages);
    }


    // 메시지 개별 조회
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageCreateResponseDto> getMessageDetail(@PathVariable UUID messageId) {
        MessageCreateResponseDto message = messageService.getMessageDetail(messageId);
        return ResponseEntity.ok(message);
    }  
}
