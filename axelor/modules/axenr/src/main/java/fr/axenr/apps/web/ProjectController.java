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

  public void computeDates(ActionRequest request, ActionResponse response) {
    Long projectId = (Long) request.getContext().get("id");
    Project project = projectRepository.find(projectId);
    taskPlanningService.computeDates(project);
    response.setReload(true);
  }
}
