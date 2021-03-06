package org.jbpm.process.workitem;

import java.util.Collection;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.*;

public abstract class AbstractWorkItemHandler implements WorkItemHandler {
	
	private StatefulKnowledgeSession ksession;
	
	public AbstractWorkItemHandler(StatefulKnowledgeSession ksession) {
		if (ksession == null) {
			throw new IllegalArgumentException("ksession cannot be null");
		}
		this.ksession = ksession;
	}
	
	public StatefulKnowledgeSession getSession() {
		return ksession;
	}
	
	public long getProcessInstanceId(WorkItem workItem) {
		return ((WorkItemImpl) workItem).getProcessInstanceId();
	}
	
	public ProcessInstance getProcessInstance(WorkItem workItem) {
		ProcessInstance processInstance = ksession.getProcessInstance(getProcessInstanceId(workItem));
		return processInstance;
	}
	
	public NodeInstance getNodeInstance(WorkItem workItem) {
		ProcessInstance processInstance = getProcessInstance(workItem);
		if (!(processInstance instanceof WorkflowProcessInstance)) {
			return null;
		}
		return findWorkItemNodeInstance(workItem.getId(), ((WorkflowProcessInstance) processInstance).getNodeInstances());
	}
	
	private WorkItemNodeInstance findWorkItemNodeInstance(long workItemId, Collection<NodeInstance> nodeInstances) {
		for (NodeInstance nodeInstance: nodeInstances) {
			if (nodeInstance instanceof WorkItemNodeInstance) {
				WorkItemNodeInstance workItemNodeInstance = (WorkItemNodeInstance) nodeInstance;
				if (workItemId == workItemNodeInstance.getWorkItem().getId()) {
					return workItemNodeInstance;
				}
			}
			if (nodeInstance instanceof NodeInstanceContainer) {
				WorkItemNodeInstance result = findWorkItemNodeInstance(workItemId, ((NodeInstanceContainer) nodeInstance).getNodeInstances());
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

}
