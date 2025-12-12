package com.Group4.MiniProject.Ingredient.entity;

import com.Group4.MiniProject.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Ingredient")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IngredientId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int snow;
    private int rock;
    private int carrot;
    private int branch;
    private int muffler;

    // 눈사람 생성 가능 여부 확인
    // 모든 재료가 각각 최소 1이 있어야 true 반환, 아니라면 false 반환
    public boolean canBuildSnowman(){
        return this.snow >= 1 && this.rock >= 1 && this.carrot >= 1 &&
                this.branch >= 1 && this.muffler >= 1;
    }

    // 재료 랜덤 획득, 5개 중에서 3개를 랜덤으로 획득
    public void addRandomIngredients(int count){
        List<String> types = Arrays.asList("snow", "rock", "carrot", "branch", "muffler");
        Random random = new Random();

        for(int i = 0; i < count; i++){
            String type = types.get(random.nextInt(types.size()));
            switch(type){
                case "snow" -> this.snow++;
                case "rock" -> this.rock++;
                case "carrot" -> this.carrot++;
                case "branch" -> this.branch++;
                case "muffler" -> this.muffler++;

            }
        }
    }

    // 재료 소모, canBuildSnowman이 false면 IllegalArgumentException 실행, true면 모든 재료 1 카운트 소모
    public void consumeIngredients(){
        if(!canBuildSnowman()){
            throw new IllegalArgumentException("재료가 부족하여 눈사람을 만들 수 없습니다.");
        }
        this.snow--;
        this.rock--;
        this.carrot--;
        this.branch--;
        this.muffler--;
    }
}