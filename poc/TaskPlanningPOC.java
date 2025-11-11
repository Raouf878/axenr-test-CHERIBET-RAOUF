import java.time.LocalDate;
import java.util.*;

// Task model
class Task {
    private String name;
    private int duration; // in days
    private int delayBeforeStart; // in days
    private Task dependency; // parent task
    private LocalDate startDate;
    private LocalDate endDate;

    public Task(String name, int duration, int delayBeforeStart) {
        this.name = name;
        this.duration = duration;
        this.delayBeforeStart = delayBeforeStart;
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getDuration() { return duration; }
    public int getDelayBeforeStart() { return delayBeforeStart; }
    public Task getDependency() { return dependency; }
    public void setDependency(Task dependency) { this.dependency = dependency; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return String.format("%-20s | %2d jours | %2d jours | %-20s | %s | %s",
                name, duration, delayBeforeStart,
                dependency != null ? dependency.getName() : "-",
                startDate != null ? startDate : "-",
                endDate != null ? endDate : "-");
    }
}

// Project model
class Project {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Task> tasks;

    public Project(String name, LocalDate startDate) {
        this.name = name;
        this.startDate = startDate;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public List<Task> getTasks() { return tasks; }
}

// Planning Service
class TaskPlanningService {

    public void computeDates(Project project) {
        if (project.getTasks().isEmpty()) {
            return;
        }

        // Detect circular dependencies
        if (hasCircularDependency(project.getTasks())) {
            throw new IllegalStateException("Circular dependency detected!");
        }

        // Sort tasks by dependencies (topological sort)
        List<Task> sortedTasks = topologicalSort(project.getTasks());

        // Calculate dates for each task
        LocalDate projectEndDate = project.getStartDate();
        for (Task task : sortedTasks) {
            calculateTaskDates(task, project.getStartDate());
            
            // Update project end date
            if (task.getEndDate().isAfter(projectEndDate)) {
                projectEndDate = task.getEndDate();
            }
        }

        project.setEndDate(projectEndDate);
    }

    private void calculateTaskDates(Task task, LocalDate projectStartDate) {
        if (task.getStartDate() != null) {
            return; // Already calculated
        }

        LocalDate startDate;
        if (task.getDependency() == null) {
            // No dependency: start at project start date
            startDate = projectStartDate;
        } else {
            // Has dependency: start after parent task end date + delay
            Task parent = task.getDependency();
            if (parent.getEndDate() == null) {
                calculateTaskDates(parent, projectStartDate);
            }
            startDate = parent.getEndDate().plusDays(task.getDelayBeforeStart());
        }

        task.setStartDate(startDate);
        task.setEndDate(startDate.plusDays(task.getDuration() - 1));
    }

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

    private void topologicalSortDFS(Task task, Set<Task> visited, 
                                    Set<Task> visiting, List<Task> sorted) {
        visiting.add(task);

        if (task.getDependency() != null && !visited.contains(task.getDependency())) {
            if (visiting.contains(task.getDependency())) {
                throw new IllegalStateException("Circular dependency detected!");
            }
            topologicalSortDFS(task.getDependency(), visited, visiting, sorted);
        }

        visiting.remove(task);
        visited.add(task);
        sorted.add(task);
    }

    private boolean hasCircularDependency(List<Task> tasks) {
        try {
            topologicalSort(tasks);
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    // Bonus: Retroplanning
    public void computeDatesBackward(Project project) {
        if (project.getTasks().isEmpty() || project.getEndDate() == null) {
            return;
        }

        List<Task> sortedTasks = topologicalSort(project.getTasks());
        Collections.reverse(sortedTasks); // Reverse order for backward calculation

        LocalDate projectStartDate = project.getEndDate();
        for (Task task : sortedTasks) {
            calculateTaskDatesBackward(task, project.getEndDate());
            
            if (task.getStartDate().isBefore(projectStartDate)) {
                projectStartDate = task.getStartDate();
            }
        }

        project.setStartDate(projectStartDate);
    }

    private void calculateTaskDatesBackward(Task task, LocalDate projectEndDate) {
        // Find tasks that depend on this task
        List<Task> dependents = new ArrayList<>();
        // This would require reverse lookup - simplified for POC
    }
}

// Main class with example
public class TaskPlanningPOC {

    public static void main(String[] args) {
        // Create project
        Project project = new Project("Installation Photovoltaïque", LocalDate.of(2025, 1, 1));

        // Create tasks
        Task etudeSite = new Task("Étude du site", 3, 0);
        Task commandeMateriel = new Task("Commande matériel", 2, 1);
        Task installation = new Task("Installation", 4, 0);
        Task raccordement = new Task("Raccordement réseau", 2, 1);
        Task miseEnService = new Task("Mise en service", 1, 0);

        // Set dependencies
        commandeMateriel.setDependency(etudeSite);
        installation.setDependency(commandeMateriel);
        raccordement.setDependency(installation);
        miseEnService.setDependency(raccordement);

        // Add tasks to project
        project.addTask(etudeSite);
        project.addTask(commandeMateriel);
        project.addTask(installation);
        project.addTask(raccordement);
        project.addTask(miseEnService);

        // Calculate dates
        TaskPlanningService service = new TaskPlanningService();
        service.computeDates(project);

        // Display results
        System.out.println("=".repeat(110));
        System.out.println("PROJET: " + project.getName());
        System.out.println("Date début projet: " + project.getStartDate());
        System.out.println("Date fin projet: " + project.getEndDate());
        System.out.println("=".repeat(110));
        System.out.println();
        System.out.printf("%-20s | %-10s | %-15s | %-20s | %-12s | %-12s%n",
                "Tâche", "Durée", "Délai avant", "Dépend de", "Date début", "Date fin");
        System.out.println("-".repeat(110));

        for (Task task : project.getTasks()) {
            System.out.println(task);
        }

        System.out.println("=".repeat(110));
    }
}