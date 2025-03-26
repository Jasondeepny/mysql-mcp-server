package com.zcckj.mcp.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcckj.mcp.mysql.model.DbTool;
import com.zcckj.mcp.mysql.model.TextContent;
import com.zcckj.mcp.mysql.service.MysqlMcpServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MySQLMCPServerTests {

    @Autowired
    private MysqlMcpServerService mcpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testListResources() {
        // 测试列出数据库
        String result = mcpClient.listDatabases();
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        // 测试列出表
        String result2 = mcpClient.listTables("purch_order");
        assertThat(result2).isNotNull();
        assertThat(result2).isNotEmpty();
    }

    @Test
    void testReadResource() {
        // 测试有效的URI
        String validUri = "mysql://purch_order/t_purch_order";
        String result = mcpClient.readResource(validUri);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        // 测试无效的URI
        String invalidUri = "invalid://uri";
        String invalidResult = mcpClient.readResource(invalidUri);
        TextContent textContent = parseTextContent(invalidResult);
        assertThat(textContent.getText()).contains("Invalid URI scheme");
    }

    @Test
    void testExecuteSqlTool() {
        // 测试有效的SQL查询
        String query = "SELECT * FROM purch_order.t_supplier_info LIMIT 10";
        String result = mcpClient.executeSql(query);
        assertThat(result).isNotNull();

        TextContent textContent = parseTextContent(result);
        assertThat(textContent.getText()).isNotNull();
    }

    @Test
    void testListTools() {
        String result = mcpClient.listTools();
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        try {
            DbTool[] dbTools = objectMapper.readValue(result, DbTool[].class);
            assertThat(dbTools).isNotEmpty();

            // 查找execute_sql工具
            boolean hasExecuteSqlTool = false;
            for (DbTool dbTool : dbTools) {
                if (dbTool.getName().equals("execute_sql")) {
                    hasExecuteSqlTool = true;
                    break;
                }
            }
            assertThat(hasExecuteSqlTool).isTrue();
        } catch (JsonProcessingException e) {
            // 处理JSON解析异常
            assertThat(false).as("解析JSON失败: " + e.getMessage()).isTrue();
        }
    }

    @Test
    void testInvalidQuery() {
        // 测试无效的SQL查询（包含DROP关键字）
        String invalidQuery = "DROP TABLE test_table";
        String result = mcpClient.executeSql(invalidQuery);

        TextContent textContent = parseTextContent(result);
        assertThat(textContent.getText()).contains("Invalid SQL query");
    }

    private TextContent parseTextContent(String json) {
        try {
            return objectMapper.readValue(json, TextContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析JSON失败: " + e.getMessage(), e);
        }
    }
}