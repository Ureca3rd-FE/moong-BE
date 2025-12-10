package com.Group4.MiniProject.Snowman.controller;

import com.Group4.MiniProject.Message.dto.MessageCreateResponseDto;
import com.Group4.MiniProject.Snowman.dto.SnowmanCreateResponse;
import com.Group4.MiniProject.Snowman.service.SnowmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Snowman API", description = "눈사람 생성 및 편지 열기 기능") // 스웨거 그룹 이름
@RestController
@RequestMapping("/api/snowman")
@RequiredArgsConstructor
public class SnowmanController {
    private final SnowmanService snowmanService;

    // 눈사람 만들기 API
    @Operation(summary = "눈사람 만들기", description = "재료 5종류를 1개씩 소모하여 눈사람을 생성합니다.")
    @PostMapping
    public ResponseEntity<SnowmanCreateResponse> createSnowman(@RequestParam Long userId){
        SnowmanCreateResponse response = snowmanService.makeSnowman(userId);
        return ResponseEntity.ok(response);
    }

    // 편지 열기 API
    @Operation(summary = "편지 열기", description = "눈사람 1개를 소모하여 잠긴 편지를 엽니다.")
    @PostMapping("/messages/{messageId}/open")
    public ResponseEntity<MessageCreateResponseDto> openMessage(
            @PathVariable UUID messageId,
            @RequestParam Long userId
    ){
        MessageCreateResponseDto response = snowmanService.openMessage(userId, messageId);
        return ResponseEntity.ok(response);
    }
}
