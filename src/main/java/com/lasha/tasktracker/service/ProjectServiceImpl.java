package com.lasha.tasktracker.service;

import com.lasha.tasktracker.dto.ProjectDTO;
import com.lasha.tasktracker.entity.ProjectEntity;
import com.lasha.tasktracker.entity.UserEntity;
import com.lasha.tasktracker.enums.Role;
import com.lasha.tasktracker.exception.ApiException;
import com.lasha.tasktracker.mapper.ProjectMapper;
import com.lasha.tasktracker.repository.ProjectRepository;
import com.lasha.tasktracker.repository.UserRepository;
import com.lasha.tasktracker.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final CurrentUser currentUser;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;

    @Override
    public ProjectDTO createProject(ProjectDTO dto) {
        UserEntity current = currentUser.get();
        ProjectEntity entity = projectMapper.toEntity(dto);

        Long ownerIdFromDto = dto.getOwnerId();
        if (ownerIdFromDto != null) {
            if (current.getRole() != Role.ADMIN) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only ADMIN can assign project owner");
            }
            UserEntity specifiedOwner = userRepository.findById(ownerIdFromDto)
                    .orElseThrow(() -> new IllegalArgumentException("User was not found: " + ownerIdFromDto));
            entity.setOwner(specifiedOwner);
        } else {
            entity.setOwner(current);
        }

        ProjectEntity saved = projectRepository.save(entity);
        return projectMapper.toDto(saved);
    }

    @Override
    public ProjectDTO getProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project was not found: " + id));
        ensureAccess(project);
        return projectMapper.toDto(project);
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        UserEntity current = currentUser.get();
        List<ProjectEntity> projects;
        if (current.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByOwner(current);
        }
        return projects.stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project was not not found: " + id));

        UserEntity current = currentUser.get();
        ensureAccess(project);

        Long newOwnerId = dto.getOwnerId();
        if (newOwnerId != null && !newOwnerId.equals(project.getOwner().getId())) {
            if (current.getRole() != Role.ADMIN) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only ADMIN can change project owner");
            }
            UserEntity newOwner = userRepository.findById(newOwnerId)
                    .orElseThrow(() -> new IllegalArgumentException("User was not found: " + newOwnerId));
            project.setOwner(newOwner);
        }

        if (dto.getOwnerId() != null) {
            project.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }

        ProjectEntity updated = projectRepository.save(project);
        return projectMapper.toDto(updated);
    }

    @Override
    public void deleteProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project was not found: " + id));
        ensureAccess(project);
        projectRepository.delete(project);
    }

    private void ensureAccess(ProjectEntity project) {
        UserEntity current = currentUser.get();
        if (current.getRole() == Role.MANAGER && !project.getOwner().getId().equals(current.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don’t have permission to access project " + project.getId());
        }
    }
}
