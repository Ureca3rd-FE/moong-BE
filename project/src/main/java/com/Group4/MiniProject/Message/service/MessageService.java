package com.Group4.MiniProject.Message.service;

import com.Group4.MiniProject.Message.dto.MessageCreateRequestDto;
import com.Group4.MiniProject.Message.dto.MessageCreateResponseDto;
import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.Message.dto.MessageListResponseDto;
import com.Group4.MiniProject.Message.entity.Message;
import com.Group4.MiniProject.Theme.entity.Theme;
import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.Ingredient.repository.IngredientRepository;
import com.Group4.MiniProject.Message.repository.MessageRepository;
import com.Group4.MiniProject.Theme.repository.ThemeRepository;
import com.Group4.MiniProject.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ThemeRepository themeRepository;
    private final IngredientRepository ingredientRepository;

    // 메시지 개별 조회
    public MessageCreateResponseDto getMessageDetail(UUID messageId) {
        Message message = messageRepository.findByUuid(messageId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 메시지를 찾을 수 없습니다."
                ));

        // DTO로 변환하여 반환
        return new MessageCreateResponseDto(message);

    }

    // 메시지 작성 및 재료 지급
    /**
    * 메시지를 생성하고, 수신자에게 랜덤 재료 3개를 지급합니다.
    * 이 메소드는 2개의 DB 쓰기 작업을 하므로 @Transactional을 사용합니다.
    * @param requestDto 클라이언트로부터 받은 메시지 작성 데이터
    * @param senderNickname 메시지를 보내는 사람(로그인한 유저)의 닉네임
    */
    @Transactional
    public void createMessage(MessageCreateRequestDto requestDto, String senderNickname) {

        // 수신자 조회
        User receiver = userRepository.findByNickname(requestDto.getReceivedNickname())
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 사용자를 찾을 수 없습니다."));

        // 테마 조회
        Theme theme = themeRepository.findById(requestDto.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 테마를 찾을 수 없습니다."));

        // 메시지 생성
        Message message = Message.builder()
                .message(requestDto.getMessage())
                .nickname(senderNickname)
                .isOpen(false)
                .receivedUser(receiver)
                .theme(theme)
                .build();

        messageRepository.save(message);

        // 수신자 재료 조회
        Ingredient ingredient = ingredientRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자의 재료 정보를 찾을 수 없습니다."));

        // 랜덤 3개 재료 지급
        ingredient.addRandomIngredients(3);
        }

        // 열린 메시지 리스트 조회
        public List<MessageListResponseDto> getMessageListByUserIdAndOpenStatus(Long userId) {
            // userId와 isOpen=true를 조건으로 사용
            List<Message> messages = messageRepository.findByReceivedUserIdAndIsOpenTrue(userId);

            if (messages.isEmpty()) {
                throw new IllegalArgumentException("열린 메시지가 존재하지 않습니다.");
            }
            // 엔티티 리스트를 DTO 리스트로 변환하여 반환
            return messages.stream()
                    .map(MessageListResponseDto::new)
                    .collect(Collectors.toList());
        }

        // 열지 않은 편지 리스트 조회
        public List<MessageListResponseDto> getUnopenedMessageList(Long userId){
            // Repository 호출
            List<Message> messages = messageRepository.findByReceivedUserIdAndIsOpenFalse(userId);

            // 예외 처리
            if(messages.isEmpty()) throw new IllegalArgumentException("받은 편지가 없습니다.");

            // 엔티티 리스트를 DTO 리스트로 변환하여 반환
            return messages.stream()
                    .map(MessageListResponseDto::new)
                    .collect(Collectors.toList());
        }
    }
