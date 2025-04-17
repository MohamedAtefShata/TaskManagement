package com.example.minitrello.util;

import com.example.minitrello.model.Task;
import com.example.minitrello.model.TaskList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling position-related operations for TaskLists and Tasks.
 * Helps with reordering and maintaining ordering.
 */
public class PositionUtils {

    /**
     * Shifts tasks in a task list to insert a new task at the specified position.
     * Tasks at or after the specified position will be moved down.
     *
     * @param tasks List of tasks to reorder
     * @param position Position where the new task will be inserted
     */
    public static void shiftTasksForInsertion(List<Task> tasks, int position) {
        for (Task task : tasks) {
            if (task.getPosition() >= position) {
                task.setPosition(task.getPosition() + 1);
            }
        }
    }

    /**
     * Shifts task lists in a project to insert a new task list at the specified position.
     * Task lists at or after the specified position will be moved down.
     *
     * @param taskLists List of task lists to reorder
     * @param position Position where the new task list will be inserted
     */
    public static void shiftTaskListsForInsertion(List<TaskList> taskLists, int position) {
        for (TaskList taskList : taskLists) {
            if (taskList.getPosition() >= position) {
                taskList.setPosition(taskList.getPosition() + 1);
            }
        }
    }

    /**
     * Normalizes positions for a list of tasks.
     * Ensures tasks are positioned sequentially starting from 1.
     *
     * @param tasks List of tasks to normalize
     * @return List of tasks with normalized positions
     */
    public static List<Task> normalizeTaskPositions(List<Task> tasks) {
        List<Task> sortedTasks = tasks.stream()
                .sorted(Comparator.comparingInt(Task::getPosition))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedTasks.size(); i++) {
            sortedTasks.get(i).setPosition(i + 1);
        }

        return sortedTasks;
    }

    /**
     * Normalizes positions for a list of task lists.
     * Ensures task lists are positioned sequentially starting from 1.
     *
     * @param taskLists List of task lists to normalize
     * @return List of task lists with normalized positions
     */
    public static List<TaskList> normalizeTaskListPositions(List<TaskList> taskLists) {
        List<TaskList> sortedTaskLists = taskLists.stream()
                .sorted(Comparator.comparingInt(TaskList::getPosition))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedTaskLists.size(); i++) {
            sortedTaskLists.get(i).setPosition(i + 1);
        }

        return sortedTaskLists;
    }
}