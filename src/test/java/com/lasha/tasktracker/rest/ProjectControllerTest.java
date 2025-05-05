package com.lasha.tasktracker.rest;

import com.lasha.tasktracker.dto.ProjectDTO;
import com.lasha.tasktracker.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController controller;

    private ProjectDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = new ProjectDTO();
        sampleDto.setId(1L);
        sampleDto.setName("Test Project");
        sampleDto.setDescription("Sample description");
        sampleDto.setOwnerId(1L);
    }

    @Test
    void createProject_shouldReturnCreatedEntity() {
        when(projectService.createProject(sampleDto)).thenReturn(sampleDto);

        ResponseEntity<ProjectDTO> response = controller.createProject(sampleDto);

        verify(projectService).createProject(sampleDto);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(sampleDto, response.getBody());
    }

    @Test
    void getProject_shouldReturnOkEntity() {
        when(projectService.getProject(1L)).thenReturn(sampleDto);

        ResponseEntity<ProjectDTO> response = controller.getProject(1L);

        verify(projectService).getProject(1L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(sampleDto, response.getBody());
    }

    @Test
    void getAllProjects_shouldReturnList() {
        ProjectDTO another = new ProjectDTO();
        another.setId(2L);
        another.setName("Another Project");
        List<ProjectDTO> list = Arrays.asList(sampleDto, another);
        when(projectService.getAllProjects()).thenReturn(list);

        ResponseEntity<List<ProjectDTO>> response = controller.getAllProjects();

        verify(projectService).getAllProjects();
        assertEquals(200, response.getStatusCode().value());
        assertIterableEquals(list, response.getBody());
    }

    @Test
    void updateProject_shouldReturnUpdatedEntity() {
        ProjectDTO updated = new ProjectDTO();
        updated.setId(1L);
        updated.setName("Updated Project");
        when(projectService.updateProject(1L, sampleDto)).thenReturn(updated);

        ResponseEntity<ProjectDTO> response = controller.updateProject(1L, sampleDto);

        verify(projectService).updateProject(1L, sampleDto);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
    }

    @Test
    void deleteProject_shouldReturnNoContent() {
        doNothing().when(projectService).deleteProject(1L);

        ResponseEntity<Void> response = controller.deleteProject(1L);

        verify(projectService).deleteProject(1L);
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
