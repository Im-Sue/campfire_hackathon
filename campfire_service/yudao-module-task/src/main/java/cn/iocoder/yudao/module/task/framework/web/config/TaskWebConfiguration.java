package cn.iocoder.yudao.module.task.framework.web.config;

import cn.iocoder.yudao.framework.swagger.config.YudaoSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * task 模块的 web 组件的配置
 */
@Configuration(proxyBeanMethods = false)
public class TaskWebConfiguration {

    /**
     * task 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi taskGroupedOpenApi() {
        return YudaoSwaggerAutoConfiguration.buildGroupedOpenApi("task");
    }

}
