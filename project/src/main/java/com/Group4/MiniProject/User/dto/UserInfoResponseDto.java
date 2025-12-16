package com.Group4.MiniProject.User.dto;

import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDto {
    private String nickname;
    private IngredientDto ingredient;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDto {
        private Integer snow;
        private Integer rock;
        private Integer carrot;
        private Integer branch;
        private Integer muffler;
    }
}
