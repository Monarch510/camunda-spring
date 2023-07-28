package com.example.camundaspring.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * @author wuwenjun
 * @date 2023-04-07
 */
@RestController(value = "/test")
@Slf4j
public class TestController {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    IdentityService identityService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

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

    @GetMapping("/listDefinition")
    public List<ProcessDefinition> listDefinition() {
        log.info("执行listDefinition ");
        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

        List<Deployment> list = deploymentQuery.list();
        for (Deployment deployment : list) {
            System.out.println("sss" + deployment.toString());
        }
        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .latestVersion()//最新结果
                //添加查询条件
                //.processDefinitionName("流程定义的name")
                //.processDefinitionId("流程定义的id")
                //.processDefinitionKey("流程定义的key")
                //排序
                .orderByProcessDefinitionVersion().asc()
                //查询结果
                //.count() //返回结果集数量
                //.listPage(firstResult,  maxResults)//分页查询
                //.singleResult() //唯一结果
                .list();//总的结果集数量
        for (ProcessDefinition processDefinition : processDefinitionList) {
            log.info("id：{}，name：{}，key：{}，version：{}，resourceName：{}",
                    processDefinition.getId(), processDefinition.getName(),
                    processDefinition.getKey(), processDefinition.getVersion(),
                    processDefinition.getDiagramResourceName());
        }
        return null;
    }

    @GetMapping(value = "/startInstance")
    public boolean startInstance(String key) {
        log.info("执行startInstance [key: {}]", key);
        //根据key启动流程实例 这个KEY可以在数据库表 ACT_RE_PROCDEF 的KEY_字段查看到，当然也可以在流程文件中查到，或者通过查询接口查到。
        //开启流程 需要指定当前用户
        // 设置当前用户为操作人
        identityService.setAuthenticatedUserId("admin");
        ProcessInstance simpleTest = runtimeService.startProcessInstanceByKey(key);
        log.info("流程实例的ProcessInstanceId：{}，流程实例的ProcessDefinitionId：{}", simpleTest.getId(),
                simpleTest.getProcessDefinitionId());
        //指定变量启动
       /* Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("userId", "张翠山");
        runtimeService.startProcessInstanceByKey("leave", variables);*/
        //根据消息启动
       /* String messageName = "msg1";//或  String messageName = "msg2";
        runtimeService.startProcessInstanceByMessage(messageName);*/

        if (simpleTest != null && StringUtils.isNotBlank(simpleTest.getProcessDefinitionId())) {
            List<Task> list = taskService.createTaskQuery().processInstanceId(simpleTest.getProcessInstanceId())
                    .active()
                    .list();

            for (Task task : list) {
                log.info("name：{}，id：{}，processDefinitionId：{}，taskDefinition：{}，当前任务执行人：{}",
                        task.getName(), task.getId(), task.getProcessDefinitionId(),
                        task.getTaskDefinitionKey(), task.getAssignee());

            }

        }

        return true;
    }

    @GetMapping("/addCandidateUser")
    public boolean candidateUser(String taskId, String userId) {
        log.info("执行candidateUser [taskId: {}, userId: {}]", taskId, userId);
        taskService.addCandidateUser(taskId, userId);
        return true;
    }

    @GetMapping("/addCandidateGroup")
    public boolean candidateGroup(String taskId, String groupId) {
        log.info("执行candidateGroup [taskId: {}, groupId: {}]", taskId, groupId);
        taskService.addCandidateGroup(taskId, groupId);
        return true;
    }

    @GetMapping("/completeTask")
    public boolean completeTask(String taskId) {
        log.info("执行completeTask [taskId: {}]", taskId);
        taskService.complete(taskId);
        return true;
    }
}
