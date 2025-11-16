package fr.axenr.apps;

import com.axelor.app.AxelorModule;
import fr.axenr.apps.service.TaskPlanningService;

public class AxEnrModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(TaskPlanningService.class);
  }
}
