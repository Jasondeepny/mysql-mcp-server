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
        String result = mcpClient.listDatabases();
        System.out.println(result);
        String result2 = mcpClient.listTables("purch_order");
        System.out.println(result2);
    }

    @Test
    void testReadResource() {
        String s = "mysql://purch_order/t_purch_order/data";
        String result = mcpClient.readResource(s);
        System.out.println(result);
    }

    @Test
    void testExecuteSqlTool() {
        // 测试SQL执行工具
        String query = "SELECT * FROM t_supplier_info LIMIT 10";
        // 如果需要测试executeSql方法，可以这样：
        String result = mcpClient.executeSql(query);
        System.out.println(result);


        String[] parts = "mysql://purch_order/t_supplier_info/data".substring("mysql://".length()).split("/");
        System.out.println(parts[0]);
        System.out.println(parts[1]);
        System.out.println(parts[2]);
//        assertThat(textContent.getText()).isNotNull();
    }

    @Test
    void testInvalidQuery() {
        String result = mcpClient.listTools();
        System.out.println(result);
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