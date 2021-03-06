/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.task.impl;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.seam.transaction.Transactional;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.jbpm.task.TaskEvent;
import org.jbpm.task.api.TaskEventsService;

/**
 *
 */
@Transactional
@ApplicationScoped
public class TaskEventsServiceImpl implements TaskEventsService {

    @Inject
    private JbpmServicesPersistenceManager pm;

    public List<TaskEvent> getTaskEventsById(long taskId) {
        return (List<TaskEvent>) pm.queryStringWithParametersInTransaction("select te from TaskEvent te where te.taskId =:taskId ", 
                        pm.addParametersToMap("taskId", taskId));
    }

    public void removeTaskEventsById(long taskId) {
        List<TaskEvent> taskEventsById = getTaskEventsById(taskId);
        for (TaskEvent e : taskEventsById) {
            pm.remove(e);
        }
    }
}
