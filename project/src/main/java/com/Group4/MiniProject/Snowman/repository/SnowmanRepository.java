package com.Group4.MiniProject.Snowman.repository;

import com.Group4.MiniProject.Snowman.entity.SnowmanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnowmanRepository extends JpaRepository<SnowmanEntity, Long> {
    Optional<SnowmanEntity> findFirstByUserIdAndOpenMessageIsNull(Long userId);
}
