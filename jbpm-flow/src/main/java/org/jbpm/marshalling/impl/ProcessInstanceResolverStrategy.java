/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */
package org.jbpm.marshalling.impl;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.PersisterHelper;
import org.drools.core.reteoo.ReteooStatefulSession;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.ProcessRuntimeImpl;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.kie.definition.process.Process;
import org.kie.marshalling.ObjectMarshallingStrategy;
import org.kie.runtime.process.ProcessInstance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * When using this strategy, knowledge session de/marshalling process will make sure that
 * the processInstance is <i>not</i> serialized as a part of the the session/network. 
 * </p>
 * Instead, this strategy, which may only be used for {@link ProcessInstance} objects, 
 * saves the process instance in the {@link ProcessInstanceManager}, and later retrieves
 * it from there. 
 * </p>
 * If you're doing tricky things with serialization and persistence, please make sure
 * to remember that the {@link ProcessInstanceManager} cache of process instances is emptied 
 * at the end of every transaction (commit). 
 */
public class ProcessInstanceResolverStrategy
        implements
        ObjectMarshallingStrategy {

    public boolean accept(Object object) {
        if ( object instanceof ProcessInstance ) {
            return true;
        }
        else {
            return false;
        }
    }

    public void write(ObjectOutputStream os,
                      Object object) throws IOException {
        ProcessInstance processInstance = (ProcessInstance) object;

        connectProcessInstanceToRuntimeAndProcess( processInstance, os );

        os.writeLong( processInstance.getId() );
    }

    public Object read(ObjectInputStream is) throws IOException,
                                            ClassNotFoundException {
        long processInstanceId = is.readLong();
        ProcessInstanceManager pim = retrieveProcessInstanceManager( is );
        ProcessInstance processInstance = pim.getProcessInstance( processInstanceId );

        connectProcessInstanceToRuntimeAndProcess( processInstance, is );

        return processInstance;
    }

    /**
     * Retrieve the {@link ProcessInstanceManager} object from the ObjectOutput- or ObjectInputStream.
     * The stream object will secretly also either be a {@link MarshallerReaderContext} or a
     * {@link MarshallerWriteContext}.
     * @param streamContext The marshaller stream/context.
     * @return A {@link ProcessInstanceManager} object. 
     */
    public static ProcessInstanceManager retrieveProcessInstanceManager(Object streamContext) {
        ProcessInstanceManager pim = null;
        if ( streamContext instanceof MarshallerWriteContext ) {
            MarshallerWriteContext context = (MarshallerWriteContext) streamContext;
            pim = ((ProcessRuntimeImpl) ((ReteooStatefulSession) context.wm).getProcessRuntime()).getProcessInstanceManager();
        }
        else if ( streamContext instanceof MarshallerReaderContext ) {
            MarshallerReaderContext context = (MarshallerReaderContext) streamContext;
            pim = ((ProcessRuntimeImpl) ((ReteooStatefulSession) context.wm).getProcessRuntime()).getProcessInstanceManager();
        }
        else {
            throw new UnsupportedOperationException( "Unable to retrieve " + ProcessInstanceManager.class.getSimpleName() + " from "
                                                     + streamContext.getClass().getName() );
        }
        return pim;
    }

    /**
     * Fill the process instance .kruntime and .process fields with the appropriate values.
     * @param processInstance
     * @param streamContext
     */
    private void connectProcessInstanceToRuntimeAndProcess(ProcessInstance processInstance,
                                                           Object streamContext) {
        ProcessInstanceImpl processInstanceImpl = (ProcessInstanceImpl) processInstance;
        InternalKnowledgeRuntime kruntime = processInstanceImpl.getKnowledgeRuntime();

        // Attach the kruntime if not present
        if ( kruntime == null ) {
            kruntime = retrieveKnowledgeRuntime( streamContext );
            processInstanceImpl.setKnowledgeRuntime( kruntime );
        }
        // Attach the process if not present
        if ( processInstance.getProcess() == null ) {
            Process process = kruntime.getKieBase().getProcess( processInstance.getProcessId() );
            processInstanceImpl.setProcess( process );
        }
    }

    /**
     * Retrieve the {@link ProcessInstanceManager} object from the ObjectOutput- or ObjectInputStream.
     * The stream object will secretly also either be a {@link MarshallerReaderContext} or a
     * {@link MarshallerWriteContext}.
     * </p>
     * The knowledge runtime object is useful in order to reconnect the process instance to the
     * process and the knowledge runtime object. 
     * @param streamContext The marshaller stream/context.
     * @return A {@link InternalKnowledgeRuntime} object. 
     */
    public static InternalKnowledgeRuntime retrieveKnowledgeRuntime(Object streamContext) {
        InternalKnowledgeRuntime kruntime = null;
        if ( streamContext instanceof MarshallerWriteContext ) {
            MarshallerWriteContext context = (MarshallerWriteContext) streamContext;
            kruntime = ((ReteooStatefulSession) context.wm).getKnowledgeRuntime();
        }
        else if ( streamContext instanceof MarshallerReaderContext ) {
            MarshallerReaderContext context = (MarshallerReaderContext) streamContext;
            kruntime = ((ReteooStatefulSession) context.wm).getKnowledgeRuntime();
        }
        else {
            throw new UnsupportedOperationException( "Unable to retrieve " + ProcessInstanceManager.class.getSimpleName() + " from "
                                                     + streamContext.getClass().getName() );
        }
        return kruntime;
    }

    public byte[] marshal(Context context,
                          ObjectOutputStream os,
                          Object object) throws IOException {
        ProcessInstance processInstance = (ProcessInstance) object;
        connectProcessInstanceToRuntimeAndProcess( processInstance, os );
        return PersisterHelper.longToByteArray( processInstance.getId() );
    }

    public Object unmarshal(Context context,
                            ObjectInputStream is,
                            byte[] object,
                            ClassLoader classloader) throws IOException,
                                                    ClassNotFoundException {
        long processInstanceId = PersisterHelper.byteArrayToLong( object );
        ProcessInstanceManager pim = retrieveProcessInstanceManager( is );
        ProcessInstance processInstance = pim.getProcessInstance( processInstanceId );
        connectProcessInstanceToRuntimeAndProcess( processInstance, is );
        return processInstance;
    }

    public Context createContext() {
        // no context needed
        return null;
    }

}
