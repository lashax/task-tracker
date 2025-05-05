package com.lasha.tasktracker.service;

import com.lasha.tasktracker.dto.TaskDTO;
import com.lasha.tasktracker.enums.TaskPriority;
import com.lasha.tasktracker.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskDTO createTask(TaskDTO dto);

    TaskDTO getTask(Long id);

    Page<TaskDTO> getTasksByProject(Long projectId, TaskStatus status, TaskPriority priority, Pageable pageable);

    Page<TaskDTO> getTasksByAssignedUser(Long userId, TaskStatus status, TaskPriority priority, Pageable pageable);

    TaskDTO updateTask(Long id, TaskDTO dto);

    TaskDTO assignTask(Long taskId, Long userId);

    TaskDTO updateTaskStatus(Long taskId, TaskStatus status);

    void deleteTask(Long id);
}
