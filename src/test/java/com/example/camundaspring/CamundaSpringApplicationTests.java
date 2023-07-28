package com.example.camundaspring;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class CamundaSpringApplicationTests {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    @Deployment
    public void ruleUsageExample() {
        // 从文件中读取一个模型
        File file = new File("E:\\workspace\\intelljWorkspace\\camunda-spring\\src\\main\\resources\\loan-approval.bpmn");
        BpmnModelInstance modelInstance = Bpmn.readModelFromFile(file);

    }

}
