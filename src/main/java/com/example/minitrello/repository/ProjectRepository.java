package com.example.minitrello.repository;

import com.example.minitrello.model.Project;
import com.example.minitrello.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    /**
     * Find all projects owned by a specific user
     * Uses method name pattern: findBy[Property]
     */
    Page<Project> findByOwner(User owner, Pageable pageable);

    /**
     * Find all projects where a user is a member
     * Uses method name pattern: findBy[Property][NestedProperty]
     */
    Page<Project> findByMembersId(Long userId, Pageable pageable);

    /**
     * Find all projects that a user can access (either as owner or member)
     * This query is complex enough to justify using a custom JPQL query
     */
    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    Page<Project> findAccessibleProjects(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find a project by id and check if user has access to it
     * Uses method name pattern with OR condition
     */
    Optional<Project> findByIdAndOwnerIdOrIdAndMembersId(
            Long projectId, Long ownerUserId, Long sameProjectId, Long memberUserId);

    /**
     * Check if a user has access to a project
     * Uses method name pattern with OR condition and exists prefix
     */
    boolean existsByIdAndOwnerIdOrIdAndMembersId(
            Long projectId, Long ownerUserId, Long sameProjectId, Long memberUserId);

    /**
     * Helper method to check access with a simplified interface
     * Uses a default method to simplify calls from service layer
     */
    default Optional<Project> findByIdWithAccessCheck(Long projectId, Long userId) {
        return findByIdAndOwnerIdOrIdAndMembersId(projectId, userId, projectId, userId);
    }

    /**
     * Helper method to check access with a simplified interface
     * Uses a default method to simplify calls from service layer
     */
    default boolean hasUserAccess(Long projectId, Long userId) {
        return existsByIdAndOwnerIdOrIdAndMembersId(projectId, userId, projectId, userId);
    }
}