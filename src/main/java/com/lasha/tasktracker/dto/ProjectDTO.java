package com.lasha.tasktracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
