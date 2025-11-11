package fr.axenr.apps.service;

import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;
import fr.axenr.apps.db.Project;
import fr.axenr.apps.db.Task;
import java.time.LocalDate;
import java.util.*;

public class TaskPlanningService {

  @Transactional
  public void computeDates(Project project) {
    // Validate project
    if (project == null) {
      throw new IllegalArgumentException("Project cannot be null");
    }

    if (project.getTaskList() == null || project.getTaskList().isEmpty()) {
      throw new IllegalArgumentException("Project has no tasks");
    }

    if (project.getStartDate() == null) {
      throw new IllegalArgumentException("Project start date is required");
    }

    // Get list of tasks
    List<Task> tasks = new ArrayList<>(project.getTaskList());

    // Detect circular dependencies
    if (hasCircularDependency(tasks)) {
      throw new IllegalStateException("Circular dependency detected in task dependencies");
    }

    // Sort tasks by dependencies (topological sort)
    List<Task> sortedTasks = topologicalSort(tasks);

    // Calculate dates for each task
    LocalDate projectEndDate = project.getStartDate();
    for (Task task : sortedTasks) {
      calculateTaskDates(task, project.getStartDate());

      // Update project end date
      if (task.getEndDate() != null && task.getEndDate().isAfter(projectEndDate)) {
        projectEndDate = task.getEndDate();
      }
    }

    // Update project end date
    project.setEndDate(projectEndDate);

    // Persist changes
    JPA.save(project);
  }

  /** Calculate start and end dates for a single task */
  private void calculateTaskDates(Task task, LocalDate projectStartDate) {
    if (task.getStartDate() != null) {
      return; // Already calculated
    }

    LocalDate startDate;
    if (task.getDependOf() == null) {
      // No dependency: start at project start date
      startDate = projectStartDate;
    } else {
      // Has dependency: start after parent task end date + delay
      Task parent = task.getDependOf();
      if (parent.getEndDate() == null) {
        calculateTaskDates(parent, projectStartDate);
      }

      int delayBeforeStart = task.getDelayToStart() != null ? task.getDelayToStart().intValue() : 0;
      startDate = parent.getEndDate().plusDays(delayBeforeStart);
    }

    task.setStartDate(startDate);

    int duration = task.getDuration() != null ? task.getDuration().intValue() : 1;
    task.setEndDate(startDate.plusDays(duration - 1));
  }

  /** Sort tasks using topological sort (respects dependencies) */
  private List<Task> topologicalSort(List<Task> tasks) {
    List<Task> sorted = new ArrayList<>();
    Set<Task> visited = new HashSet<>();
    Set<Task> visiting = new HashSet<>();

    for (Task task : tasks) {
      if (!visited.contains(task)) {
        topologicalSortDFS(task, visited, visiting, sorted);
      }
    }

    Collections.reverse(sorted);
    return sorted;
  }

  /** Depth-first search for topological sort */
  private void topologicalSortDFS(
      Task task, Set<Task> visited, Set<Task> visiting, List<Task> sorted) {
    visiting.add(task);

    if (task.getDependOf() != null && !visited.contains(task.getDependOf())) {
      if (visiting.contains(task.getDependOf())) {
        throw new IllegalStateException("Circular dependency detected!");
      }
      topologicalSortDFS(task.getDependOf(), visited, visiting, sorted);
    }

    visiting.remove(task);
    visited.add(task);
    sorted.add(task);
  }

  /** Check if there are circular dependencies in the task list */
  private boolean hasCircularDependency(List<Task> tasks) {
    try {
      topologicalSort(tasks);
      return false;
    } catch (IllegalStateException e) {
      return true;
    }
  }

  /** BONUS: Compute dates backward from project end date (retroplanning) */
  @Transactional
  public void computeDatesBackward(Project project) {
    if (project == null) {
      throw new IllegalArgumentException("Project cannot be null");
    }

    if (project.getTaskList() == null || project.getTaskList().isEmpty()) {
      throw new IllegalArgumentException("Project has no tasks");
    }

    if (project.getEndDate() == null) {
      throw new IllegalArgumentException("Project end date is required for retroplanning");
    }

    List<Task> tasks = new ArrayList<>(project.getTaskList());

    // Detect circular dependencies
    if (hasCircularDependency(tasks)) {
      throw new IllegalStateException("Circular dependency detected in task dependencies");
    }

    // Sort tasks in reverse order for backward calculation
    List<Task> sortedTasks = topologicalSort(tasks);
    Collections.reverse(sortedTasks);

    // Build dependent tasks map (reverse dependencies)
    Map<Task, List<Task>> dependentsMap = buildDependentsMap(tasks);

    // Calculate dates backward
    LocalDate projectStartDate = project.getEndDate();
    for (Task task : sortedTasks) {
      calculateTaskDatesBackward(task, project.getEndDate(), dependentsMap);

      if (task.getStartDate() != null && task.getStartDate().isBefore(projectStartDate)) {
        projectStartDate = task.getStartDate();
      }
    }

    // Update project start date
    project.setStartDate(projectStartDate);

    // Persist changes
    JPA.save(project);
  }

  /** Calculate task dates backward (from end date) */
  private void calculateTaskDatesBackward(
      Task task, LocalDate projectEndDate, Map<Task, List<Task>> dependentsMap) {
    if (task.getEndDate() != null) {
      return; // Already calculated
    }

    List<Task> dependents = dependentsMap.get(task);
    LocalDate endDate;

    if (dependents == null || dependents.isEmpty()) {
      // No dependents: end at project end date
      endDate = projectEndDate;
    } else {
      // Has dependents: end before the earliest dependent start date
      LocalDate earliestDependentStart = null;
      for (Task dependent : dependents) {
        if (dependent.getStartDate() == null) {
          calculateTaskDatesBackward(dependent, projectEndDate, dependentsMap);
        }

        int delayBeforeStart =
            dependent.getDelayToStart() != null ? dependent.getDelayToStart().intValue() : 0;
        LocalDate calculatedEnd = dependent.getStartDate().minusDays(delayBeforeStart);

        if (earliestDependentStart == null || calculatedEnd.isBefore(earliestDependentStart)) {
          earliestDependentStart = calculatedEnd;
        }
      }
      endDate = earliestDependentStart;
    }

    task.setEndDate(endDate);

    int duration = task.getDuration() != null ? task.getDuration().intValue() : 1;
    task.setStartDate(endDate.minusDays(duration - 1));
  }

  /** Build a map of tasks to their dependent tasks (reverse dependency lookup) */
  private Map<Task, List<Task>> buildDependentsMap(List<Task> tasks) {
    Map<Task, List<Task>> dependentsMap = new HashMap<>();

    for (Task task : tasks) {
      if (task.getDependOf() != null) {
        dependentsMap.computeIfAbsent(task.getDependOf(), k -> new ArrayList<>()).add(task);
      }
    }

    return dependentsMap;
  }
}
