package com.lasha.tasktracker.service;

import com.lasha.tasktracker.dto.TaskDTO;
import com.lasha.tasktracker.entity.ProjectEntity;
import com.lasha.tasktracker.entity.TaskEntity;
import com.lasha.tasktracker.entity.UserEntity;
import com.lasha.tasktracker.enums.Role;
import com.lasha.tasktracker.enums.TaskPriority;
import com.lasha.tasktracker.enums.TaskStatus;
import com.lasha.tasktracker.exception.ApiException;
import com.lasha.tasktracker.mapper.TaskMapper;
import com.lasha.tasktracker.repository.ProjectRepository;
import com.lasha.tasktracker.repository.TaskRepository;
import com.lasha.tasktracker.repository.UserRepository;
import com.lasha.tasktracker.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final CurrentUser currentUser;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TaskDTO createTask(TaskDTO dto) {
        UserEntity caller = currentUser.get();

        ProjectEntity project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found: " + dto.getProjectId()));

        if (!(caller.getRole() == Role.ADMIN || project.getOwner().getId().equals(caller.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to create task for project: " + project.getId());
        }

        TaskEntity entity = taskMapper.toEntity(dto);
        entity.setProject(project);
        updateAssignedUser(dto, entity);

        TaskEntity saved = taskRepository.save(entity);
        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDTO getTask(Long id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found: " + id));
        ensureAccess(task);
        return taskMapper.toDto(task);
    }

    @Override
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasksByProject(Long projectId, TaskStatus status, TaskPriority priority, Pageable pageable) {
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Project not found: " + projectId));
        UserEntity caller = currentUser.get();
        if (!(caller.getRole() == Role.ADMIN || project.getOwner().getId().equals(caller.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Not allowed to view tasks for project: " + projectId);
        }

        Specification<TaskEntity> spec = (root, query, cb) ->
                cb.equal(root.get("project"), project);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasksByAssignedUser(Long userId, TaskStatus status, TaskPriority priority, Pageable pageable) {
        UserEntity caller = currentUser.get();
        UserEntity target;
        if (userId != null) {
            if (caller.getRole() != Role.ADMIN) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "Only ADMIN may fetch tasks for other users");
            }
            target = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "User not found: " + userId));
        } else {
            target = caller;
        }

        Specification<TaskEntity> spec = (root, query, cb) ->
                cb.equal(root.get("assignedUser"), target);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toDto);
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO dto) {
        TaskEntity task = taskRepository.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Task not found: " + id));
        ensureAccess(task);

        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }
        updateAssignedUser(dto, task);

        TaskEntity updated = taskRepository.save(task);
        return taskMapper.toDto(updated);
    }

    @Override
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TaskDTO assignTask(Long taskId, Long userId) {
        UserEntity caller = currentUser.get();

        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Task not found: " + taskId));

        if (caller.getRole() == Role.MANAGER) {
            Long ownerId = task.getProject().getOwner().getId();
            if (!ownerId.equals(caller.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Managers can only assign tasks for their own projects");
            }
        } else if (caller.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only MANAGER or ADMIN can assign tasks");
        }

        UserEntity assignee = userRepository.findById(userId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        task.setAssignedUser(assignee);
        TaskEntity saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    public TaskDTO updateTaskStatus(Long taskId, TaskStatus status) {
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Task not found: " + taskId));

        UserEntity caller = currentUser.get();
        if (task.getAssignedUser() == null || !task.getAssignedUser().getId().equals(caller.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned user can update task status");
        }

        task.setStatus(status);
        TaskEntity saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    public void deleteTask(Long id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found: " + id));
        ensureAccess(task);
        taskRepository.delete(task);
    }

    private void updateAssignedUser(TaskDTO dto, TaskEntity entity) {
        if (dto.getAssignedUserId() != null) {
            UserEntity assigned = userRepository.findById(dto.getAssignedUserId()).orElseThrow(() ->
                    new ApiException(HttpStatus.NOT_FOUND, "User not found: " + dto.getAssignedUserId()));
            entity.setAssignedUser(assigned);
        } else {
            entity.setAssignedUser(null);
        }
    }


    private void ensureAccess(TaskEntity task) {
        UserEntity caller = currentUser.get();
        boolean isOwner = task.getProject().getOwner().getId().equals(caller.getId());
        boolean isAssigned = task.getAssignedUser() != null
                && task.getAssignedUser().getId().equals(caller.getId());
        if (!(caller.getRole() == Role.ADMIN || isOwner || isAssigned)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No permission on task: " + task.getId());
        }
    }
}
