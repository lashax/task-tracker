package com.lasha.tasktracker.service;

import com.lasha.tasktracker.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO dto);

    ProjectDTO getProject(Long id);

    List<ProjectDTO> getAllProjects();

    ProjectDTO updateProject(Long id, ProjectDTO dto);

    void deleteProject(Long id);
}
