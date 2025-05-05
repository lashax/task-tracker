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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    CurrentUser currentUser;
    @Mock
    TaskRepository taskRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    TaskMapper taskMapper;

    @InjectMocks
    TaskServiceImpl service;

    private UserEntity admin, manager, other;
    private ProjectEntity project;
    private TaskDTO sampleDTO;
    private TaskEntity sampleEntity;

    @BeforeEach
    void setUp() {
        admin = UserEntity.builder().id(1L).role(Role.ADMIN).build();
        manager = UserEntity.builder().id(2L).role(Role.MANAGER).build();
        other = UserEntity.builder().id(3L).role(Role.USER).build();

        project = new ProjectEntity();
        project.setId(10L);
        project.setOwner(manager);

        sampleDTO = new TaskDTO();
        sampleDTO.setProjectId(10L);
        sampleDTO.setTitle("T");
        sampleDTO.setStatus(TaskStatus.TODO);
        sampleDTO.setPriority(TaskPriority.HIGH);

        sampleEntity = new TaskEntity();
        sampleEntity.setId(5L);
        sampleEntity.setProject(project);
    }

    @Test
    void createTask_managerAllowed() {
        when(currentUser.get()).thenReturn(manager);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(sampleDTO)).thenReturn(sampleEntity);
        when(taskRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(taskMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        TaskDTO out = service.createTask(sampleDTO);
        assertThat(out).isEqualTo(sampleDTO);
        verify(taskRepository).save(sampleEntity);
    }

    @Test
    void createTask_forbiddenWhenNotOwnerOrAdmin() {
        when(currentUser.get()).thenReturn(other);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ApiException ex = catchThrowableOfType(ApiException.class, () -> service.createTask(sampleDTO));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getTask_notFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = catchThrowableOfType(ApiException.class, () -> service.getTask(99L));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTask_ownerAllowed() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(manager);
        when(taskMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        TaskDTO out = service.getTask(5L);
        assertThat(out).isEqualTo(sampleDTO);
    }

    @Test
    void getTask_forbiddenForOthers() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(other);

        ApiException ex = catchThrowableOfType(ApiException.class, () -> service.getTask(5L));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateTask_updatesFields() {
        TaskDTO update = new TaskDTO();
        update.setTitle("New");
        update.setDueDate(LocalDate.now());

        sampleEntity.setAssignedUser(other);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(other);
        when(taskRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(taskMapper.toDto(sampleEntity)).thenReturn(update);

        TaskDTO out = service.updateTask(5L, update);
        assertThat(out).isEqualTo(update);
        assertThat(sampleEntity.getTitle()).isEqualTo("New");
    }

    @Test
    void assignTask_adminAllowed() {
        UserEntity assignee = UserEntity.builder().id(4L).role(Role.USER).build();
        when(taskRepository.findById(5L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(admin);
        when(userRepository.findById(4L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(taskMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        TaskDTO out = service.assignTask(5L, 4L);
        assertThat(out).isEqualTo(sampleDTO);
        assertThat(sampleEntity.getAssignedUser()).isEqualTo(assignee);
    }

    @Test
    void updateTaskStatus_assignedAllowed() {
        sampleEntity.setAssignedUser(other);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(other);
        when(taskRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(taskMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        TaskDTO out = service.updateTaskStatus(5L, TaskStatus.DONE);
        assertThat(out).isEqualTo(sampleDTO);
        assertThat(sampleEntity.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void deleteTask_ownerAllowed() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(manager);

        service.deleteTask(5L);
        verify(taskRepository).delete(sampleEntity);
    }
}