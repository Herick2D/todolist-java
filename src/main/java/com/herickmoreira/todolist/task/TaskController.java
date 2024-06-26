package com.herickmoreira.todolist.task;

import com.herickmoreira.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) idUser);

    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      var messageStartAtError = ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("The start date and end date of the activity must be later than the current date.");
      return messageStartAtError;
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      var messageEndAtError = ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("The start date must be less than the end date.");
      return messageEndAtError;
    }

    var task = this.taskRepository.save(taskModel);
    var taskResponse = ResponseEntity.status(HttpStatus.OK).body(task);
    return taskResponse;

  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser((UUID) idUser);
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var idUser = request.getAttribute("idUser");

    var task = this.taskRepository.findById(id).orElse(null);
    Utils.copyNonNullProperties(taskModel, task);

    if (task == null) {
      ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Task not found");
    }

    if (!task.getIdUser().equals(idUser)) {
      var notHavePermission = ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("The user does not have permission to change this task");
      return notHavePermission;

    }

    Utils.copyNonNullProperties(taskModel, task);
    var taskUpdated = this.taskRepository.save(task);
    return ResponseEntity.ok().body(taskUpdated);
  }
}

