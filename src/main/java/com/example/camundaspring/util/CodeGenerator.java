package com.example.camundaspring.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.springframework.stereotype.Component;

/**
 * @author wuwenjun
 * @date 2023-04-07
 */
@Component
public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/process_engine", "root", "123456")
                .globalConfig(builder -> {
                    builder.author("wwj") // 设置作者
                            //.enableSwagger() // 开启 swagger 模式
                            .outputDir("E:\\workspace\\intelljWorkspace\\camunda-spring\\src\\main\\java\\com\\example\\camundaspring"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.example") // 设置父包名
                            .moduleName("camundaspring"); // 设置父包模块名
                            //.pathInfo(Collections.singletonMap(OutputFile.xml, "D://")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("t_simple") // 设置需要生成的表名
                            .addTablePrefix("act_"); // 设置过滤表前缀
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

}
