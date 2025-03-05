package com.zcckj.mcpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
public class MySQLMCPServerTests {

    @Autowired
    private MysqlMcpServerService mcpClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testListResources() {
        // 测试资源列表
        List<Resource> resources = mcpClient.listResources();
        assertThat(resources).isNotEmpty();
        assertThat(resources.get(0).getUri()).startsWith("mysql://");
    }

    @Test
    void testExecuteSqlTool() {
        // 测试SQL执行工具
        String query = "SELECT * FROM t_supplier_info LIMIT 10";
        // 如果需要测试executeSql方法，可以这样：
        TextContent textContent = mcpClient.executeSql(query);
        // 修改为统一格式
        System.out.println(textContent.getText());
        // assertThat(textContent.getText()).isNotNull();
    }

    @Test
    void testInvalidQuery() {
    }
}