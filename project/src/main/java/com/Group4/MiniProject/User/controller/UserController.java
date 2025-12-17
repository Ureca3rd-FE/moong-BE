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
@RequestMapping("/api/member")
@Tag(name = "회원 관리 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("서버 에러가 발생했습니다."));
        }
    }

    @Operation(summary = "로그인")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (닉네임 또는 비밀번호 불일치)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 에러",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserRequestDto requestDto) {
        try {
            LoginResponseDto response = userService.login(requestDto);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("서버 에러 발생."));
        }
    }

    @Operation(summary = "회원 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody UserDeleteRequestDto requestDto) {
        try {
            userService.deleteUser(requestDto);
            return ResponseEntity.ok(UserDeleteRequestDto.ok());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("서버 에러 발생."));
        }
    }

    @Operation(summary = "사용자 홈 화면 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "홈화면 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserInfoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/homeinfo")
    public ResponseEntity<?> getHomeInfo(@RequestHeader("Authorization")String token){
        System.out.println("========================================");
        System.out.println("[HomeInfo] 요청 수신");
        System.out.println("[HomeInfo] Authorization 헤더 : " + (token != null));
        System.out.println("========================================");
        try{
            String accessToken = token.replace("Bearer ", "");
            System.out.println("[HomeInfo] 토큰 추출");

            UserInfoResponseDto userInfo = userService.getCurrentUserInfo(accessToken);
            System.out.println("[HomeInfo] 홈 정보 조회 성공");
            System.out.println("[HomeInfo] nickname: " + userInfo.getNickname());
            System.out.println("========================================");

            return ResponseEntity.ok(userInfo);

        } catch(IllegalArgumentException e){
            System.err.println("[HomeInfo] 인증 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(e.getMessage()));
        } catch (Exception e){
            System.err.println("[HomeInfo] 서버 에러 발생");
            System.err.println("에러 타입: " + e.getClass().getName());
            System.err.println("에러 메시지: " + e.getMessage());

            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("서버에러발생"));
        }
    }

    @Operation(summary = "API 상태 확인")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API 서버가 정상 작동 중입니다.");
    }
}