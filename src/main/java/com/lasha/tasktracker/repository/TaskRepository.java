package com.lasha.tasktracker.repository;

import com.lasha.tasktracker.entity.ProjectEntity;
import com.lasha.tasktracker.entity.TaskEntity;
import com.lasha.tasktracker.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {
    Page<TaskEntity> findByProject(ProjectEntity project, Pageable pageable);

    Page<TaskEntity> findByAssignedUser(UserEntity user, Pageable pageable);
}
