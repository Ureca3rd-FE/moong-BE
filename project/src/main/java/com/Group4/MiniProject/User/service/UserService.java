package com.Group4.MiniProject.User.service;

import com.Group4.MiniProject.User.dto.UserDeleteRequestDto;
import com.Group4.MiniProject.User.dto.UserInfoResponseDto;
import com.Group4.MiniProject.User.dto.UserRequestDto;
import com.Group4.MiniProject.User.dto.LoginResponseDto;
import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.Ingredient.repository.IngredientRepository;
import com.Group4.MiniProject.User.repository.UserRepository;
import com.Group4.MiniProject.jwt.components.JwtProvider;
import com.Group4.MiniProject.jwt.components.JwtValidator;
import com.Group4.MiniProject.jwt.components.JwtParser;
import com.Group4.MiniProject.jwt.util.TokenInfo;
import com.Group4.MiniProject.common.exception.common.NotFoundException;
import com.Group4.MiniProject.common.exception.auth.UnAuthorizedException;
import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtValidator jwtValidator;
    private final JwtParser jwtParser;

    @Transactional
    public void signup(UserRequestDto requestDto) {
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

        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .nickname(requestDto.getNickname())
                .password(encryptedPassword)
                .isFirst(true) // 회원가입 시 기본값 true
                .build();

        User savedUser = userRepository.save(user);

        Ingredient ingredient = Ingredient.builder()
                .snow(0).rock(0).carrot(0).branch(0).muffler(0)
                .user(savedUser)
                .build();

        ingredientRepository.save(ingredient);
        savedUser.setIngredient(ingredient);
    }

    // ✅ 추가: 첫 방문 여부 업데이트 로직
    @Transactional
    public void updateFirstVisitStatus(Long userId) {
        User user = findById(userId);
        user.setFirst(false);
    }

    public UserInfoResponseDto getUserInfoById(Long userId) {

        User user = findById(userId);

        Ingredient ingredient = user.getIngredient();
        UserInfoResponseDto.IngredientDto ingredientDto = UserInfoResponseDto.IngredientDto.builder()
                .snow(ingredient.getSnow())
                .rock(ingredient.getRock())
                .carrot(ingredient.getCarrot())
                .branch(ingredient.getBranch())
                .muffler(ingredient.getMuffler())
                .build();
        return UserInfoResponseDto.builder()
                .nickname(user.getNickname())
                .ingredient(ingredientDto)
                .build();
    }

    @Transactional
    public LoginResponseDto login(UserRequestDto requestDto) {
        User user = userRepository.findByNickname(requestDto.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        TokenInfo accessToken = jwtProvider.generateAccessToken(user);
        TokenInfo refreshToken = jwtProvider.generateRefreshToken(user);

        user.setRefreshToken(refreshToken.token());

        return LoginResponseDto.of(
                accessToken.token(),
                accessToken.expiredAt(),
                refreshToken.token(),
                refreshToken.expiredAt(),
                user.getId(),
                user.isFirst() // ✅ 수정: isFirst 반환
        );
    }

    @Transactional
    public void deleteUser(UserDeleteRequestDto requestDto) {
        User user = userRepository.findByNickname(requestDto.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getIngredient() != null) {
            ingredientRepository.delete(user.getIngredient());
        }
        userRepository.delete(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken) {
        findById(userId).setRefreshToken(refreshToken);
    }

    public String getRefreshToken(Long userId) {
        return findById(userId).getRefreshToken();
    }

    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String savedToken = getRefreshToken(userId);
        return savedToken != null && savedToken.equals(refreshToken);
    }

    @Transactional
    public void deleteRefreshToken(Long userId) {
        findById(userId).setRefreshToken(null);
    }

    @Transactional
    public void logout(Long userId) {
        deleteRefreshToken(userId);
    }

    @Transactional
    public LoginResponseDto refreshTokens(String refreshToken) {
        jwtValidator.verifyToken(refreshToken);
        Claims claims = jwtParser.parseClaims(refreshToken).getPayload();
        if (!"REFRESH".equals(claims.get("tokenType", String.class))) throw new TokenInvalidException();

        Long userId = claims.get("userId", Long.class);
        if (!validateRefreshToken(userId, refreshToken)) throw new UnAuthorizedException();

        User user = findById(userId);
        TokenInfo newAccessToken = jwtProvider.generateAccessToken(user);
        TokenInfo newRefreshToken = jwtProvider.generateRefreshToken(user);
        user.setRefreshToken(newRefreshToken.token());

        return LoginResponseDto.of(
                newAccessToken.token(),
                newAccessToken.expiredAt(),
                newRefreshToken.token(),
                newRefreshToken.expiredAt(),
                userId,
                user.isFirst() // ✅ 수정: 상태 유지
        );
    }

    public String refreshAccessToken(String refreshToken) {
        jwtValidator.verifyToken(refreshToken);
        Claims claims = jwtParser.parseClaims(refreshToken).getPayload();
        Long userId = claims.get("userId", Long.class);
        if (!validateRefreshToken(userId, refreshToken)) throw new UnAuthorizedException();

        return jwtProvider.generateAccessToken(findById(userId)).token();
    }
}