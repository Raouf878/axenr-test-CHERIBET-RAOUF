package fr.axenr.apps.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import fr.axenr.apps.db.Project;
import fr.axenr.apps.db.repo.ProjectRepository;
import fr.axenr.apps.service.TaskPlanningService;

public class ProjectController {

  private final ProjectRepository projectRepository;
  private final TaskPlanningService taskPlanningService;

  @Inject
  public ProjectController(
      ProjectRepository projectRepository, TaskPlanningService taskPlanningService) {
    this.projectRepository = projectRepository;
    this.taskPlanningService = taskPlanningService;
  }

  /**
   * Action method to compute task dates (forward planning) Called by the "Calculer les dates"
   * button
   */
  public void computeDates(ActionRequest request, ActionResponse response) {
    try {
      // Get project ID from context
      Long projectId = (Long) request.getContext().get("id");

      // Validate project ID exists
      if (projectId == null) {
        response.setError("Veuillez sauvegarder le projet avant de calculer les dates");
        return;
      }

      // Load project from database
      Project project = projectRepository.find(projectId);

      // Validate project exists
      if (project == null) {
        response.setError("Projet introuvable");
        return;
      }

      // Validate project has tasks
      if (project.getTaskList() == null || project.getTaskList().isEmpty()) {
        response.setError("Le projet ne contient aucune tâche à calculer");
        return;
      }

      // Validate project has start date
      if (project.getStartDate() == null) {
        response.setError("La date de début du projet est obligatoire");
        return;
      }

      // Compute dates
      taskPlanningService.computeDates(project);

      // Reload the view to show updated dates
      response.setReload(true);

      // Show success message
      response.setNotify("Les dates des tâches ont été calculées avec succès");

    } catch (IllegalArgumentException e) {
      // Handle validation errors
      response.setError(e.getMessage());

    } catch (IllegalStateException e) {
      // Handle circular dependency errors
      response.setError("Erreur : " + e.getMessage());

    } catch (Exception e) {
      // Handle unexpected errors
      response.setError("Une erreur s'est produite lors du calcul des dates : " + e.getMessage());
      e.printStackTrace(); // Log the error for debugging
    }
  }

  /**
   * BONUS: Action method to compute task dates backward (retroplanning) Called by the
   * "Rétroplanning" button (if you add it)
   */
  public void computeDatesBackward(ActionRequest request, ActionResponse response) {
    try {
      // Get project ID from context
      Long projectId = (Long) request.getContext().get("id");

      // Validate project ID exists
      if (projectId == null) {
        response.setError("Veuillez sauvegarder le projet avant de calculer les dates");
        return;
      }

      // Load project from database
      Project project = projectRepository.find(projectId);

      // Validate project exists
      if (project == null) {
        response.setError("Projet introuvable");
        return;
      }

      // Validate project has tasks
      if (project.getTaskList() == null || project.getTaskList().isEmpty()) {
        response.setError("Le projet ne contient aucune tâche à calculer");
        return;
      }

      // Validate project has END date for retroplanning
      if (project.getEndDate() == null) {
        response.setError("La date de fin du projet est obligatoire pour le rétroplanning");
        return;
      }

      // Compute dates backward
      taskPlanningService.computeDatesBackward(project);

      // Reload the view to show updated dates
      response.setReload(true);

      // Show success message
      response.setNotify("Rétroplanning calculé avec succès");

    } catch (IllegalArgumentException e) {
      // Handle validation errors
      response.setError(e.getMessage());

    } catch (IllegalStateException e) {
      // Handle circular dependency errors
      response.setError("Erreur : " + e.getMessage());

    } catch (Exception e) {
      // Handle unexpected errors
      response.setError("Une erreur s'est produite lors du rétroplanning : " + e.getMessage());
      e.printStackTrace(); // Log the error for debugging
    }
  }
}
