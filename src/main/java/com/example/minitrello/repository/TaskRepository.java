package com.example.minitrello.repository;

import com.example.minitrello.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks in a specific task list
     */
    List<Task> findByTaskListId(Long taskListId);

    /**
     * Find the maximum position for tasks in a task list
     */
    @Query("SELECT MAX(t.position) FROM Task t WHERE t.taskList.id = :taskListId")
    Integer findMaxPositionInTaskList(@Param("taskListId") Long taskListId);
}