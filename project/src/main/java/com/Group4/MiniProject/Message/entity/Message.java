package com.Group4.MiniProject.Message.entity;

import com.Group4.MiniProject.Theme.entity.Theme;
import com.Group4.MiniProject.User.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "message")
public class Message {


    /**
     * 'index' (자동 증가 숫자) 형태의 ID를 사용합니다.
     * DB 컬럼명은 ERD에 맞게 "message_id"로 지정합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 증가하는 숫자 ID
    @JoinColumn(name = "message_id")
    private Long id; // message_id라는 index 칼럼


    /*
    * 조회를 위해 UUID 칼럼을 추가
    * UUID는 고유 식별자로 메시지 조회에 사용됩니다.
     */
    @Column(name = "uuid", unique = true, nullable = false, updatable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR) // UUID를 CHAR(36) 타입으로 매핑
    private UUID uuid; // UUID 칼럼 추가

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "received_user_id")
    private User receivedUser;

    @Column(length = 500)
    private String message;

    // 편지가 열려있는지 아닌지 확인
    @Column(name = "is_open")
    private boolean isOpen;

    // 닉네임 글자수 제한 없음
    @Column(length = 100)
    private String nickname;


    /*
     * 예외처리
     * UUID 값이 없을 경우 생성하는 메서드
     * 이걸 안하면 uuid가 null로 들어갑니다
     */
    @PrePersist
    protected  void onCreate(){
        if(uuid == null){
            uuid = UUID.randomUUID();
        }
    }

    // 편지 열기
    public void open(){
        this.isOpen = true;
    }
}