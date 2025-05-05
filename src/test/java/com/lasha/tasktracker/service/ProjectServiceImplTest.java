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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private CurrentUser currentUser;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectServiceImpl service;

    private UserEntity admin;
    private UserEntity manager;
    private ProjectEntity sampleEntity;
    private ProjectDTO sampleDTO;

    @BeforeEach
    void setUp() {
        admin = UserEntity.builder().id(1L).role(Role.ADMIN).build();
        manager = UserEntity.builder().id(2L).role(Role.MANAGER).build();

        sampleEntity = ProjectEntity.builder()
                .id(42L)
                .owner(manager)
                .name("Test")
                .build();

        sampleDTO = new ProjectDTO();
        sampleDTO.setName("Test");
    }

    @Test
    void createProject_asManager_setsOwner() {
        when(currentUser.get()).thenReturn(manager);
        when(projectMapper.toEntity(any())).thenReturn(new ProjectEntity());
        when(projectRepository.save(any())).thenReturn(sampleEntity);
        when(projectMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        ProjectDTO result = service.createProject(sampleDTO);

        verify(projectRepository).save(argThat(e -> e.getOwner().equals(manager)));
        assertThat(result).isEqualTo(sampleDTO);
    }

    @Test
    void createProject_asAdmin_canAssignOwner() {
        when(currentUser.get()).thenReturn(admin);
        sampleDTO.setOwnerId(manager.getId());
        ProjectEntity ent = new ProjectEntity();
        when(projectMapper.toEntity(sampleDTO)).thenReturn(ent);
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(projectRepository.save(ent)).thenReturn(sampleEntity);
        when(projectMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        ProjectDTO result = service.createProject(sampleDTO);
        assertThat(ent.getOwner()).isEqualTo(manager);
        assertThat(result).isEqualTo(sampleDTO);
    }

    @Test
    void getProject_notFound_throws404() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProject(99L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllProjects_managerSeesOwn() {
        when(currentUser.get()).thenReturn(manager);
        when(projectRepository.findByOwner(manager)).thenReturn(List.of(sampleEntity));
        when(projectMapper.toDto(sampleEntity)).thenReturn(sampleDTO);

        List<ProjectDTO> list = service.getAllProjects();
        assertThat(list).containsExactly(sampleDTO);
    }

    @Test
    void updateProject_changesName() {
        ProjectDTO update = new ProjectDTO();
        update.setName("NewName");

        ProjectEntity copy = new ProjectEntity();
        copy.setId(42L);
        copy.setOwner(manager);
        copy.setName("Test");

        when(projectRepository.findById(42L)).thenReturn(Optional.of(copy));
        when(currentUser.get()).thenReturn(manager);
        when(projectRepository.save(copy)).thenReturn(copy);
        when(projectMapper.toDto(copy)).thenReturn(update);

        ProjectDTO out = service.updateProject(42L, update);
        assertThat(copy.getName()).isEqualTo("NewName");
        assertThat(out).isEqualTo(update);
    }

    @Test
    void deleteProject_delegates() {
        when(projectRepository.findById(42L)).thenReturn(Optional.of(sampleEntity));
        when(currentUser.get()).thenReturn(manager);

        service.deleteProject(42L);
        verify(projectRepository).delete(sampleEntity);
    }
}
