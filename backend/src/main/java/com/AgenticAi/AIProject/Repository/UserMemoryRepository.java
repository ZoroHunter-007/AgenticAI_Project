package com.AgenticAi.AIProject.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgenticAi.AIProject.Entity.UserMemoryEntity;

public interface UserMemoryRepository
        extends JpaRepository<UserMemoryEntity, Long> {

    List<UserMemoryEntity> findByUserId(Long userId);
}