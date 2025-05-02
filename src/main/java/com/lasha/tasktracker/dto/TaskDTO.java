package com.lasha.tasktracker.dto;

import com.lasha.tasktracker.enums.TaskPriority;
import com.lasha.tasktracker.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private TaskPriority priority;
    private Long projectId;
    private Long assignedUserId;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
