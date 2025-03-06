package com.zcckj.mcp.mysql;

import com.zcckj.mcp.mysql.service.MysqlMcpServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MySQLMCPServerTests {

    @Autowired
    private MysqlMcpServerService mcpClient;

    @Test
    void testListResources() {
        // 测试资源列表
        mcpClient.listResources();
    }

    @Test
    void testExecuteSqlTool() {
        // 测试SQL执行工具
        String query = "SELECT * FROM t_supplier_info LIMIT 10";
        // 如果需要测试executeSql方法，可以这样：
        mcpClient.executeSql(query);
//        assertThat(textContent.getText()).isNotNull();
    }

    @Test
    void testInvalidQuery() {
        mcpClient.listTools();
//        for (DbTool dbTool : dbTools) {
//            if (dbTool.getName().equals("execute_sql")) {
        // 测试无效的SQL查询
//                String invalidQuery = "SELECT * FROM t_supplier_info LIMIT 10";
//                TextContent textContent = mcpClient.executeSql(invalidQuery);
//                assertThat(textContent.getText()).isNotNull();
//            }
//        }
    }
}