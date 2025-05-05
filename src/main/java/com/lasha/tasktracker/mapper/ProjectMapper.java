package com.lasha.tasktracker.mapper;

import com.lasha.tasktracker.dto.ProjectDTO;
import com.lasha.tasktracker.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    ProjectDTO toDto(ProjectEntity project);

    @Mapping(target = "owner.id", source = "ownerId")
    ProjectEntity toEntity(ProjectDTO dto);
}
