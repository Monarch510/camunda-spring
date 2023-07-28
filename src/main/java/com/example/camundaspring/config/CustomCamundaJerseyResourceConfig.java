package com.example.camundaspring.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

/**
 * 修改配置或注册额外的资源
 *
 * @author wuwenjun
 * @date 2023-04-07
 */
@Component
@ApplicationPath("/engine-rest")
public class CustomCamundaJerseyResourceConfig extends CamundaJerseyResourceConfig {

}
