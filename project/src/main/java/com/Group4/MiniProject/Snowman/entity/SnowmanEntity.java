package com.Group4.MiniProject.Snowman.entity;

import com.Group4.MiniProject.Message.entity.Message;
import com.Group4.MiniProject.User.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "snowman")
public class SnowmanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 눈사람 하나는 하나의 메세지를 열 수 있다. (사용 전은 반드시 null)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "open_message_id")
    private Message openMessage;

    @Builder
    public SnowmanEntity(User user){
        this.user = user;
    }

    // 편지를 열 때 사용하는 메서드
    public void useSnowmanToOpen(Message message){
        this.openMessage = message;
    }
}

public class SnowmanEntity {
}
