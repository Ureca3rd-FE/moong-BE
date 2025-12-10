package com.Group4.MiniProject.Message.repository;

import com.Group4.MiniProject.Message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 메시지 조회
    List<Message> findByReceivedUserId(Long userId);

    // userId와 isOpen=true 기준으로 리스트 조회
    List<Message> findByReceivedUserIdAndIsOpenTrue(Long userId);

    // userId와 isOpen=false 기준으로 리스트 조회
    List<Message> findByReceivedUserIdAndIsOpenFalse(Long userId);

    // 메시지 개별 조회
    Optional<Message> findByUuid(UUID uuid);

    // Long PK + 수신자 기준 조회 (필요 시)
    //Optional<Message> findByIdAndReceivedUserId(Long id, Long receiverId);
}
