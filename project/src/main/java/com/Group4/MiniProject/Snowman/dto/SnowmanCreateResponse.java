package com.Group4.MiniProject.Snowman.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SnowmanCreateResponse {
    private Long snowmanId;
    private String message;

    public static SnowmanCreateResponse of(Long snowmanId){
        return SnowmanCreateResponse.builder()
                .snowmanId(snowmanId)
                .message("눈사람이 만들어졌습니다.")
                .build();
    }
}
