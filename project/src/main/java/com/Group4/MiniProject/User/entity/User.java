package com.Group4.MiniProject.User.entity;

import com.Group4.MiniProject.Ingredient.entity.Ingredient;
import com.Group4.MiniProject.Message.entity.Message;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5, nullable = false)
    private String nickname;

    @Column(length = 10, nullable = false)
    private String password;
    
    @Column(length = 150)
    private String RefreshToken;

    // User : Ingredient = 1 : 1
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Ingredient Ingredient;

    // User : Message = 1 : N
    @OneToMany(mappedBy = "receivedUser", cascade = CascadeType.ALL)
    private java.util.List<Message> receivedMessages;
}