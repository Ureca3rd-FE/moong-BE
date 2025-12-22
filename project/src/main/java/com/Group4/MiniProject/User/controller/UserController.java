package com.Group4.MiniProject.User.controller;

import com.Group4.MiniProject.User.dto.*;
import com.Group4.MiniProject.common.dto.ErrorResponseDto;
import com.Group4.MiniProject.User.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "회원 관리 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ 추가된 API: 첫 방문 상태 변경
    @Operation(summary = "첫 방문 상태 완료 처리", description = "유저의 isFirst 상태를 false로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PatchMapping("/{userId}/first-visit")
    public ResponseEntity<?> updateFirstVisit(@PathVariable Long userId) {
        try {
            userService.updateFirstVisitStatus(userId);
            return ResponseEntity.ok("성공적으로 첫 방문 처리가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(summary = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequestDto requestDto) {
        try {
            userService.signup(requestDto);
            return ResponseEntity.ok(UserResponseDto.ok());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDto("서버 에러가 발생했습니다."));
        }
    }

    @Operation(summary = "로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserRequestDto requestDto) {
        try {
            LoginResponseDto response = userService.login(requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDto("서버 에러 발생."));
        }
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody UserDeleteRequestDto requestDto) {
        try {
            userService.deleteUser(requestDto);
            return ResponseEntity.ok(UserDeleteRequestDto.ok());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(summary = "사용자 홈 화면 조회")
    @GetMapping("/{userId}") // 경로에 {userId} 추가
    public ResponseEntity<?> getHomeInfo(@PathVariable Long userId) { // 토큰 대신 PathVariable 사용
        try {
            // 내 토큰 정보가 아닌, URL로 넘어온 ID를 사용하여 정보 조회
            UserInfoResponseDto userInfo = userService.getUserInfoById(userId);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // 사용자를 찾을 수 없는 경우 등 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(summary = "API 상태 확인")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API 서버가 정상 작동 중입니다.");
    }
}