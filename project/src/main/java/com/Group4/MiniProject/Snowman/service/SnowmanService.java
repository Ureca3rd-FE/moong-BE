package com.Group4.MiniProject.Snowman.service;

import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.Ingredient.repository.IngredientRepository;
import com.Group4.MiniProject.Message.entity.Message;
import com.Group4.MiniProject.Message.repository.MessageRepository;
import com.Group4.MiniProject.Snowman.dto.SnowmanCreateResponse;
import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnowmanService {
    private final IngredientRepository ingredientRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // 눈사람 만들기
    @Transactional
    public SnowmanCreateResponse makeSnowman(Long userId){
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 재료 조회 및 소모
        Ingredient ingredient = ingredientRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("재료 정보를 찾을 수 없습니다."));

        ingredient.consumeIngredients();

        List<Message> unopenedMessages = messageRepository.findByReceivedUserIdAndIsOpenFalse(userId);

        long unopenSize = unopenedMessages.size();
        long openCount = unopenSize - 1;

        if(openCount > 0){
            for(int i = 0; i < openCount; i++){
                Message message = unopenedMessages.get(i);
                message.open();
            }
        }

        return SnowmanCreateResponse.builder()
                .message("눈사람이 만들어졌습니다.")
                .build();
    }
}
