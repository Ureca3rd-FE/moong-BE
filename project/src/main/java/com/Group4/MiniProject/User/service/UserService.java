package com.Group4.MiniProject.User.service;

import com.Group4.MiniProject.User.dto.UserDeleteRequestDto;
import com.Group4.MiniProject.User.dto.UserRequestDto;
import com.Group4.MiniProject.User.dto.LoginResponseDto;  // ✅ 추가
import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.Ingredient.repository.IngredientRepository;
import com.Group4.MiniProject.User.repository.UserRepository;
import com.Group4.MiniProject.jwt.components.JwtProvider;  // ✅ 추가
import com.Group4.MiniProject.jwt.components.JwtValidator;  // ✅ 추가
import com.Group4.MiniProject.jwt.components.JwtParser;  // ✅ 추가
import com.Group4.MiniProject.jwt.util.TokenInfo;  // ✅ 추가
import com.Group4.MiniProject.common.exception.common.NotFoundException;  // ✅ 추가
import com.Group4.MiniProject.common.exception.auth.UnAuthorizedException;  // ✅ 추가
import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;  // ✅ 추가
import io.jsonwebtoken.Claims;  // ✅ 추가
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;  // ✅ 추가
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final PasswordEncoder passwordEncoder;  // ✅ 추가
    private final JwtProvider jwtProvider;  // ✅ 추가
    private final JwtValidator jwtValidator;  // ✅ 추가
    private final JwtParser jwtParser;  // ✅ 추가

    /**
     * 회원가입
     */
    @Transactional
    public void signup(UserRequestDto requestDto) {
        // 유효성 검사
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().length() > 4) {
            throw new IllegalArgumentException("비밀번호는 4자리 이하여야 합니다.");
        }
        if (!requestDto.getPassword().matches("\\d{4}")) {
            throw new IllegalArgumentException("비밀번호는 숫자 4자리로 구성되어야 합니다.");
        }
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // ✅ 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        // User 객체 생성 (암호화된 비밀번호 사용)
        User user = User.builder()
                .nickname(requestDto.getNickname())
                .password(encryptedPassword)  // ✅ 암호화된 비밀번호
                .build();

        // User 저장
        User savedUser = userRepository.save(user);

        // Ingredient 객체 생성
        Ingredient ingredient = Ingredient.builder()
                .snow(0)
                .rock(0)
                .carrot(0)
                .branch(0)
                .neck(0)
                .user(savedUser)
                .build();

        // Ingredient 저장
        ingredientRepository.save(ingredient);
        savedUser.setIngredient(ingredient);
    }

    /**
     * 로그인 (JWT 토큰 발급)
     */
    @Transactional  // ✅ RefreshToken 저장 때문에 트랜잭션 필요
    public LoginResponseDto login(UserRequestDto requestDto) {
        System.out.println("[Login]들어온 요청 :" + requestDto.getNickname());
        // 유효성 검사
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        // 1. 닉네임으로 회원 조회
        User user = userRepository.findByNickname(requestDto.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));

        // 2. 비밀번호 검증 (암호화된 비밀번호와 비교) ✅
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 토큰 생성 ✅
        TokenInfo accessToken = jwtProvider.generateAccessToken(user);
        TokenInfo refreshToken = jwtProvider.generateRefreshToken(user);

        // 4. RefreshToken DB 저장 ✅
        user.setRefreshToken(refreshToken.token());

        // 5. 응답 DTO 생성 ✅
        LoginResponseDto dto = LoginResponseDto.of(
                accessToken.token(),
                accessToken.expiredAt(),
                refreshToken.token(),
                refreshToken.expiredAt()
        );
        System.out.println("[Login]응답 DTO : " + dto.getAccessToken());
        return dto;
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteUser(UserDeleteRequestDto requestDto) {
        // 유효성 검사
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        // 회원 조회
        User user = userRepository.findByNickname(requestDto.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));

        // 비밀번호 검증 ✅
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Ingredient 먼저 삭제
        Ingredient ingredient = user.getIngredient();
        if (ingredient != null) {
            ingredientRepository.delete(ingredient);
        }

        // User 삭제
        userRepository.delete(user);
    }

    // ========== JWT 토큰 관리 메서드들 ✅ ==========

    /**
     * 사용자 조회 (ID)
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 사용자 조회 (닉네임)
     */
    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * RefreshToken 저장
     */
    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken) {
        User user = findById(userId);
        user.setRefreshToken(refreshToken);
    }

    /**
     * RefreshToken 조회
     */
    public String getRefreshToken(Long userId) {
        User user = findById(userId);
        return user.getRefreshToken();
    }

    /**
     * RefreshToken 검증 (DB의 토큰과 비교)
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String savedToken = getRefreshToken(userId);
        return savedToken != null && savedToken.equals(refreshToken);
    }

    /**
     * RefreshToken 삭제 (로그아웃)
     */
    @Transactional
    public void deleteRefreshToken(Long userId) {
        User user = findById(userId);
        user.setRefreshToken(null);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long userId) {
        deleteRefreshToken(userId);
    }

    /**
     * 토큰 재발급 (AccessToken + RefreshToken 모두)
     */
    @Transactional
    public LoginResponseDto refreshTokens(String refreshToken) {
        // 1. RefreshToken 검증
        jwtValidator.verifyToken(refreshToken);

        // 2. Claims 파싱
        Claims claims = jwtParser.parseClaims(refreshToken).getPayload();

        // 3. 토큰 타입 확인
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new TokenInvalidException();
        }

        // 4. 사용자 ID 추출
        Long userId = claims.get("userId", Long.class);

        // 5. DB의 RefreshToken과 비교
        if (!validateRefreshToken(userId, refreshToken)) {
            throw new UnAuthorizedException();
        }

        // 6. 사용자 조회
        User user = findById(userId);

        // 7. 새 토큰 발급
        TokenInfo newAccessToken = jwtProvider.generateAccessToken(user);
        TokenInfo newRefreshToken = jwtProvider.generateRefreshToken(user);

        // 8. 새 RefreshToken DB 저장
        user.setRefreshToken(newRefreshToken.token());

        // 9. 응답
        return LoginResponseDto.of(
                newAccessToken.token(),
                newAccessToken.expiredAt(),
                newRefreshToken.token(),
                newRefreshToken.expiredAt()
        );
    }

    /**
     * AccessToken만 재발급 (RefreshToken은 유지)
     */
    public String refreshAccessToken(String refreshToken) {
        // 1. RefreshToken 검증
        jwtValidator.verifyToken(refreshToken);

        // 2. Claims 파싱
        Claims claims = jwtParser.parseClaims(refreshToken).getPayload();

        // 3. 토큰 타입 확인
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new TokenInvalidException();
        }

        // 4. 사용자 ID 추출
        Long userId = claims.get("userId", Long.class);

        // 5. DB의 RefreshToken과 비교
        if (!validateRefreshToken(userId, refreshToken)) {
            throw new UnAuthorizedException();
        }

        // 6. 사용자 조회
        User user = findById(userId);

        // 7. 새 AccessToken만 발급
        TokenInfo newAccessToken = jwtProvider.generateAccessToken(user);

        return newAccessToken.token();
    }
}