package org.jbpm.compiler.xml;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.drools.core.xml.SemanticModules;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.kie.definition.process.Process;

public class TestXml extends TestCase {

    public void testSimpleXml() throws Exception {
        SemanticModules modules = new SemanticModules();
        modules.addSemanticModule(new ProcessSemanticModule());
        XmlProcessReader reader = new XmlProcessReader(modules, getClass().getClassLoader());
        reader.read(new InputStreamReader(TestXml.class.getResourceAsStream("XmlTest.xml")));
        List<Process> processes = reader.getProcess();
        assertNotNull(processes);
        assertEquals(1, processes.size());
        RuleFlowProcess process = (RuleFlowProcess) processes.get(0);
        assertNotNull(process);

        String output = XmlRuleFlowProcessDumper.INSTANCE.dump(process);
        System.out.println(output);
        reader = new XmlProcessReader(new SemanticModules(), getClass().getClassLoader());
        reader.read(new StringReader(output));
    }
}
