package com.lasha.tasktracker.mapper;

import com.lasha.tasktracker.dto.TaskDTO;
import com.lasha.tasktracker.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskDTO toDto(TaskEntity task);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "assignedUser.id", source = "assignedUserId")
    TaskEntity toEntity(TaskDTO dto);

    List<TaskDTO> toDtoList(List<TaskEntity> tasks);
}
