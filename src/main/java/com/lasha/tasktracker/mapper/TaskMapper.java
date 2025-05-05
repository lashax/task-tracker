package com.lasha.tasktracker.mapper;

import com.lasha.tasktracker.dto.TaskDTO;
import com.lasha.tasktracker.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    TaskDTO toDto(TaskEntity task);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "assignedUser", ignore = true)
    TaskEntity toEntity(TaskDTO dto);
}
