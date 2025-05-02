package com.lasha.tasktracker.repository;

import com.lasha.tasktracker.entity.ProjectEntity;
import com.lasha.tasktracker.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findByOwner(UserEntity owner);
}
