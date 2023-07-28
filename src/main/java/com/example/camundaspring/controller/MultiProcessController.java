package com.example.camundaspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * 多实例流程-会签
 * @author wuwenjun
 * @date 2023-07-28
 */
@Slf4j
@RestController
@RequestMapping("/multiprocess")
@RequiredArgsConstructor
public class MultiProcessController {

    private final RepositoryService repositoryService;

    private final TaskService taskService;

    private final RuntimeService runtimeService;

    @GetMapping(value = "/createDefinition")
    public boolean createDefinition(String name, String url) throws IOException {
        log.info("执行createDefinition [name: {}, url: {}]", name, url);
        File f = new File(url);

        InputStream inputStreamBpmn = Files.newInputStream(f.toPath());

        Deployment deployment = repositoryService// 与流程定义和部署对象相关的sevices
                .createDeployment()// 创建一个部署对象
                .name(name)// 添加部署名称
                .addInputStream(name+".bpmn", inputStreamBpmn)
                .deploy();// 完成部署
        log.info("部署ID：{},部署名称：{}", deployment.getId(), deployment.getName());

        inputStreamBpmn.close();
        return true;
    }

    @GetMapping("/addCountersignTask")
    public void addCountersignTask(String processInstanceId ,String taskKey, String assignees){
        log.info("执行 addCountersignTask [processInstanceId: {}, taskKey: {}, assignees: {}]", processInstanceId, taskKey, assignees);
        String collectionElementVariable = getCollectionElementVariable(processInstanceId, taskKey);

        for (String assignee : assignees.split(",")) {
            runtimeService.createProcessInstanceModification(processInstanceId)
                    .startBeforeActivity(taskKey)
                    .setVariable(collectionElementVariable, assignee)
                    .execute();
        }
    }

    private String getCollectionElementVariable(String processInstanceId, String taskKey) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(instance.getProcessDefinitionId());

        ActivityImpl activityImpl = processDefinitionEntity.findActivity(taskKey);
        if ((boolean) activityImpl.getProperty("isMultiInstance")) {
            activityImpl = processDefinitionEntity.findActivity(taskKey + "#multiInstanceBody");
        }

        MultiInstanceActivityBehavior multiInstance = (MultiInstanceActivityBehavior) activityImpl
                .getActivityBehavior();
        return multiInstance.getCollectionElementVariable();
    }

    @GetMapping("/deleteCountersignTask")
    public void deleteCountersignTask(String processInstanceId,String taskKey, String assignees) {
        log.info("执行 deleteCountersignTask [processInstanceId: {}, taskKey: {}, assignees: {}]", processInstanceId, taskKey, assignees);
        Set<String> taskIdSet = new HashSet<>();

        Arrays.asList(assignees.split(",")).forEach(e -> {
            List<Task> taskList = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKey(taskKey)
                    .or().taskAssignee(e).taskCandidateUser(e).endOr()
                    .active()
                    .list();
            taskList.forEach(item -> {
                taskIdSet.add(item.getId());
            });
        });
        taskIdSet.forEach(taskService::complete);
    }

    @GetMapping("/rollback")
    public void rollback(String processInstanceId ,String taskKey){
        log.info("执行 rollback [processInstanceId: {}, taskKey: {}]", processInstanceId, taskKey);
        runtimeService.createProcessInstanceModification(processInstanceId)
                .startBeforeActivity("huiqian")
               .cancelAllForActivity(taskKey)
               .execute();

    }
}
