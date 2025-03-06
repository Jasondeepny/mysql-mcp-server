package com.zcckj.mcp.mysql;

import com.zcckj.mcp.mysql.service.MysqlMcpServerService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider dBTools(MysqlMcpServerService mySQLMCPServerService) {
        return MethodToolCallbackProvider.builder().toolObjects(mySQLMCPServerService).build();
    }
}
