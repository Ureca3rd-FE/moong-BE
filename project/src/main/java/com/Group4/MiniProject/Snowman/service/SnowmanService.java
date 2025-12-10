package com.Group4.MiniProject.Snowman.service;

import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.Ingredient.repository.IngredientRepository;
import com.Group4.MiniProject.Message.dto.MessageCreateResponseDto;
import com.Group4.MiniProject.Message.entity.Message;
import com.Group4.MiniProject.Message.repository.MessageRepository;
import com.Group4.MiniProject.Snowman.dto.SnowmanCreateResponse;
import com.Group4.MiniProject.Snowman.dto.SnowmanDetailResponse;
import com.Group4.MiniProject.Snowman.entity.SnowmanEntity;
import com.Group4.MiniProject.Snowman.repository.SnowmanRepository;
import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnowmanService {
    private final IngredientRepository ingredientRepository;
    private final SnowmanRepository snowmanRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // 눈사람 만들기
    @Transactional
    public SnowmanCreateResponse makeSnowman(Long userId){
        // 재료 조회 및 소모
        Ingredient ingredient = ingredientRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("재료 정보를 찾을 수 없습니다."));

        ingredient.consumeIngredients();

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 눈사람 생성 및 저장
        SnowmanEntity snowman = SnowmanEntity.builder()
                .user(user)
                .build();

        snowmanRepository.save(snowman);

        // 응답DTO 반환
        return SnowmanCreateResponse.of(snowman.getId());
    }

    // 눈사람으로 편지 열기
    @Transactional
    public MessageCreateResponseDto openMessage(Long userId, UUID messageId){
        // 사용하지 않은 눈사람 조회 (눈사람 사용처리 위해 만듦)
        SnowmanEntity availableSnowman = snowmanRepository.findFirstByUserIdAndOpenMessageIsNull(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용 가능한 눈사람이 없습니다."));

        // UUID로 편지 조회
        Message message = messageRepository.findByUuid(messageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 편지를 찾을 수 없습니다."));

        // 예외처리 (이미 열린 편지)
        if(message.isOpen()){
            throw new IllegalArgumentException("이미 열린 편지입니다.");
        }

        // 편지 상태 변경(열림) + 눈사람 사용 처리
        message.open();
        availableSnowman.useSnowmanToOpen(message);

        // 응답 DTO 반환
        return new MessageCreateResponseDto(message);
    }

    // 눈사람 상세 저회 (메세지, 주고받은 사람, 테마, 등)
    @Transactional(readOnly = true)
    public SnowmanDetailResponse getSnowmanDetail(Long snowmanId){
        SnowmanEntity snowman = snowmanRepository.findById(snowmanId)
                .orElseThrow(() -> new IllegalArgumentException("눈사람이 없습니다."));

        // 응답 DTO 반환
        return SnowmanDetailResponse.from(snowman);
    }
}
