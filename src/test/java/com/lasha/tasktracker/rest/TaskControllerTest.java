package com.lasha.tasktracker.rest;

import com.lasha.tasktracker.dto.TaskDTO;
import com.lasha.tasktracker.enums.TaskPriority;
import com.lasha.tasktracker.enums.TaskStatus;
import com.lasha.tasktracker.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController controller;

    private TaskDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleDTO = new TaskDTO();
        sampleDTO.setId(1L);
        sampleDTO.setTitle("Sample Task");
        sampleDTO.setStatus(TaskStatus.TODO);
        sampleDTO.setPriority(TaskPriority.MEDIUM);
        sampleDTO.setProjectId(100L);
        sampleDTO.setAssignedUserId(200L);
    }

    @Test
    void createTask_shouldReturnCreated() {
        when(taskService.createTask(sampleDTO)).thenReturn(sampleDTO);

        ResponseEntity<TaskDTO> response = controller.createTask(sampleDTO);

        verify(taskService).createTask(sampleDTO);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(sampleDTO, response.getBody());
    }

    @Test
    void getTask_shouldReturnOk() {
        when(taskService.getTask(1L)).thenReturn(sampleDTO);

        ResponseEntity<TaskDTO> response = controller.getTask(1L);

        verify(taskService).getTask(1L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(sampleDTO, response.getBody());
    }

    @Test
    void getTasksByProject_shouldReturnPage() {
        Page<TaskDTO> page = new PageImpl<>(List.of(sampleDTO));
        when(taskService.getTasksByProject(100L, null, null, Pageable.unpaged()))
                .thenReturn(page);

        ResponseEntity<Page<TaskDTO>> response = controller.getTasksByProject(
                100L, null, null, Pageable.unpaged());

        verify(taskService).getTasksByProject(100L, null, null, Pageable.unpaged());
        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void getTasksByAssignedUser_shouldReturnPage() {
        Page<TaskDTO> page = new PageImpl<>(List.of(sampleDTO));
        when(taskService.getTasksByAssignedUser(200L, null, null, Pageable.unpaged()))
                .thenReturn(page);

        ResponseEntity<Page<TaskDTO>> response = controller.getTasksByAssignedUser(
                200L, null, null, Pageable.unpaged());

        verify(taskService).getTasksByAssignedUser(200L, null, null, Pageable.unpaged());
        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    @Test
    void updateTask_shouldReturnOk() {
        TaskDTO updated = new TaskDTO();
        updated.setId(1L);
        updated.setStatus(TaskStatus.IN_PROGRESS);
        when(taskService.updateTask(1L, sampleDTO)).thenReturn(updated);

        ResponseEntity<TaskDTO> response = controller.updateTask(1L, sampleDTO);

        verify(taskService).updateTask(1L, sampleDTO);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
    }

    @Test
    void assignTaskToUser_shouldReturnOk() {
        TaskDTO assigned = new TaskDTO();
        assigned.setId(1L);
        assigned.setAssignedUserId(300L);
        when(taskService.assignTask(1L, 300L)).thenReturn(assigned);

        ResponseEntity<TaskDTO> response = controller.assignTaskToUser(1L, 300L);

        verify(taskService).assignTask(1L, 300L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(assigned, response.getBody());
    }

    @Test
    void updateTaskStatus_shouldReturnOk() {
        TaskDTO updated = new TaskDTO();
        updated.setId(1L);
        updated.setStatus(TaskStatus.DONE);
        when(taskService.updateTaskStatus(1L, TaskStatus.DONE)).thenReturn(updated);

        ResponseEntity<TaskDTO> response = controller.updateTaskStatus(1L, TaskStatus.DONE);

        verify(taskService).updateTaskStatus(1L, TaskStatus.DONE);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
    }

    @Test
    void deleteTask_shouldReturnNoContent() {
        doNothing().when(taskService).deleteTask(1L);

        ResponseEntity<Void> response = controller.deleteTask(1L);

        verify(taskService).deleteTask(1L);
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
