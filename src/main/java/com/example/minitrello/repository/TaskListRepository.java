package com.example.minitrello.repository;

import com.example.minitrello.model.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    /**
     * Find all task lists for a specific project
     */
    List<TaskList> findByProjectId(Long projectId);

    /**
     * Find the maximum position for task lists in a project
     */
    @Query("SELECT MAX(tl.position) FROM TaskList tl WHERE tl.project.id = :projectId")
    Integer findMaxPositionInProject(@Param("projectId") Long projectId);
}