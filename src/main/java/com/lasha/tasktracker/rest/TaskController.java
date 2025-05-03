package com.lasha.tasktracker.rest;

import com.lasha.tasktracker.dto.TaskDTO;
import com.lasha.tasktracker.enums.TaskPriority;
import com.lasha.tasktracker.enums.TaskStatus;
import com.lasha.tasktracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO dto) {
        TaskDTO created = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(@RequestParam(required = false) TaskStatus status,
                                                     @RequestParam(required = false) TaskPriority priority,
                                                     @PageableDefault(size = 20) Pageable pageable) {

        Page<TaskDTO> page = taskService.getAllTasks(status, priority, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskDTO>> getTasksByProject(@PathVariable Long projectId,
                                                           @RequestParam(required = false) TaskStatus status,
                                                           @RequestParam(required = false) TaskPriority priority,
                                                           @PageableDefault(size = 20) Pageable pageable) {

        Page<TaskDTO> page = taskService.getTasksByProject(projectId, status, priority, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasksByAssignedUser(@RequestParam(required = false) Long userId,
                                                                @RequestParam(required = false) TaskStatus status,
                                                                @RequestParam(required = false) TaskPriority priority,
                                                                @PageableDefault(size = 20) Pageable pageable) {

        Page<TaskDTO> page = taskService.getTasksByAssignedUser(userId, status, priority, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO dto) {
        return ResponseEntity.ok(taskService.updateTask(id, dto));
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskDTO> assignTaskToUser(@PathVariable("id") Long taskId,
                                                    @RequestParam("userId") Long userId) {
        TaskDTO dto = taskService.assignTask(taskId, userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable("id") Long taskId,
                                                    @RequestParam("status") TaskStatus status) {
        TaskDTO updated = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
