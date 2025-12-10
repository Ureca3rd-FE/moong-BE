package com.Group4.MiniProject.User.service;

import com.Group4.MiniProject.User.dto.UserDeleteRequestDto;
import com.Group4.MiniProject.User.dto.UserRequestDto;
import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.Ingredient.repository.IngredientRepository;
import com.Group4.MiniProject.User.repository.UserRepository;
import jakarta.transaction.Transactional; // @Transactional import (login 메소드와 동일하게)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository; // 3. Ingredient 저장을 위해 주입

    /**
     * 회원가입을 처리하고, 동시에 회원의 재료(Ingredient) 인벤토리를 생성합니다.
     * @param requestDto 닉네임, 비밀번호가 담긴 DTO
     */
    @Transactional // 1. User와 Ingredient 두 테이블에 저장하므로 트랜잭션 처리를 추가합니다.
    public void signup(UserRequestDto requestDto) {
        // --- 기존 유효성 검사 로직 ---
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().length() > 4) {
            throw new IllegalArgumentException("비밀번호는 4자리 이하여야 합니다.");
        }
        if (!requestDto.getPassword().matches("\\d{4}")) {
            throw new IllegalArgumentException("비밀번호는 숫자 4자리로 구성되어야 합니다.");
        }
        if(userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // User 객체 생성
        User user = User.builder()
                .nickname(requestDto.getNickname())
                .password(requestDto.getPassword())
                .build();

        // User를 먼저 저장하여 ID를 할당받습니다.
        // Cascade에 의존하지 않고 User를 명시적으로 먼저 저장하여 ID를 할당받습니다.
        User savedUser = userRepository.save(user);

        // Ingredient 객체 생성
        Ingredient ingredient = Ingredient.builder()
                .snow(0)
                .rock(0)
                .carrot(0)
                .branch(0)
                .muffler(0)
                .user(savedUser) // 4. '저장된 User'(ID가 있음)를 Ingredient에 연결
                .build();

        // Ingredient를 '별도로' 저장합니다.
        ingredientRepository.save(ingredient);
        savedUser.setIngredient(ingredient);
    }

    @Transactional
    public String login(UserRequestDto requestDto) {
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        User user = userRepository.findByNickname(requestDto.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));
        if (!user.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user.getNickname();
    }
    @Transactional
    public void deleteUser(UserDeleteRequestDto requestDto) {
        // JWT가 없기 때문에 닉네임과 비밀번호로 본인 확인
        // JWT를 적용한다면 유효성 검사 없이 삭제 가능합니다
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        User user = userRepository.findByNickname(requestDto.getNickname())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));

        if (!user.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        //Ingredient를 먼저 삭제후 delete 진행합니다
        Ingredient ingredient = user.getIngredient();
        if (ingredient != null) {
            ingredientRepository.delete(ingredient);
        }
        userRepository.delete(user);
    }
}
