/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.bpmn2.handler;

import java.util.HashMap;
import java.util.Map;

import org.kie.runtime.KieSession;
import org.kie.runtime.KnowledgeRuntime;
import org.kie.runtime.process.*;

public class ReceiveTaskHandler implements WorkItemHandler {
    
    // TODO: use correlation instead of message id
    private Map<String, Long> waiting = new HashMap<String, Long>();
    private ProcessRuntime ksession;
    
    public ReceiveTaskHandler(KieSession ksession) {
        this.ksession = ksession;
    }
    
    public void setKnowledgeRuntime(KieSession ksession) {
    	this.ksession = ksession;
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        String messageId = (String) workItem.getParameter("MessageId");
        waiting.put(messageId, workItem.getId());
    }
    
    public void messageReceived(String messageId, Object message) {
        Long workItemId = waiting.get(messageId);
        if (workItemId == null) {
            return;
        }
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("Message", message);
        ksession.getWorkItemManager().completeWorkItem(workItemId, results);
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    	String messageId = (String) workItem.getParameter("MessageId");
        waiting.remove(messageId);
    }

}
