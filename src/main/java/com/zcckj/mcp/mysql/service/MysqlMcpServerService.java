package com.zcckj.mcp.mysql.service;

import com.zcckj.mcp.mysql.config.DataBaseConfig;
import com.zcckj.mcp.mysql.model.DbTool;
import com.zcckj.mcp.mysql.model.Resource;
import com.zcckj.mcp.mysql.model.TextContent;
import com.zcckj.mcp.mysql.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MysqlMcpServerService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataBaseConfig databaseConfig;

    @Tool(description = "List the available resources")
    public String listResources() {
        try {
            List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);

            List<Resource> resources = tables.stream()
                    .map(table -> Resource.builder()
                            .uri("mysql://" + table + "/data")
                            .name("Table: " + table)
                            .mimeType("text/plain")
                            .description("Data in table: " + table)
                            .build())
                    .collect(Collectors.toList());
            return JsonUtils.toJsonString(resources);
        } catch (Exception e) {
            log.error("Failed to list resources", e);
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Tool(description = "Read a resource from the MySQL server")
    public String readResource(@ToolParam(description = "mysql://table/data") String uri) {
        if (!uri.startsWith("mysql://")) {
            log.error("Invalid URI scheme:{} ", uri);
            return JsonUtils.toJsonString(new TextContent("Invalid URI scheme: " + uri, "text"));
        }
        String table = uri.substring(8).split("/")[0];
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM " + table + " LIMIT 100");
            if (rows.isEmpty()) {
                log.error("No data found in table: {}", table);
                return JsonUtils.toJsonString(new TextContent("No data found in table: " + table, "text"));
            }
            // 获取列名
            String headers = String.join(",", rows.get(0).keySet());
            List<String> dataRows = rows.stream()
                    .map(row -> row.values().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")))
                    .toList();
            List<String> result = new ArrayList<>();
            result.add(headers);
            result.addAll(dataRows);
            return JsonUtils.toJsonString(new TextContent(String.join("\n", result), "text"));
        } catch (Exception e) {
            log.error("Database error reading resource {}", uri, e);
            return JsonUtils.toJsonString(new TextContent("Database error: " + e.getMessage(), "text"));
        }
    }

    @Tool(description = "List the available tools")
    public String listTools() {
        List<DbTool> dbTools = Collections.singletonList(
                DbTool.builder().name("execute_sql")
                        .description("Execute an SQL query on the MySQL server")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "query", Map.of(
                                                "type", "string",
                                                "description", "The SQL query to execute")),
                                "required", List.of("query")))
                        .build());
        return JsonUtils.toJsonString(dbTools);
    }

    @Tool(description = "Execute an SQL query on the MySQL server")
    public String executeSql(@ToolParam(description = "sql str") String query) {
        try {
            if (!isValidSqlQuery(query)) {
                return JsonUtils.toJsonString(new TextContent("Invalid SQL query: " + query, "text"));
            }
            if (query.trim().toUpperCase().startsWith("SHOW TABLES")) {
                List<String> tables = jdbcTemplate.queryForList(query, String.class);
                String result = "Tables_in_" + databaseConfig.getDatabase() + "\n" +
                        String.join("\n", tables);
                return JsonUtils.toJsonString(new TextContent(result, "text"));
            }
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
                if (rows.isEmpty()) {
                    return JsonUtils.toJsonString(new TextContent("No data found.", "text"));
                }
                String headers = String.join(",", rows.get(0).keySet());
                List<String> dataRows = rows.stream()
                        .map(row -> row.values().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")))
                        .toList();
                List<String> result = new ArrayList<>();
                result.add(headers);
                result.addAll(dataRows);
                return JsonUtils.toJsonString(new TextContent(String.join("\n", result), "text"));
            }
            int rowsAffected = jdbcTemplate.update(query);
            return JsonUtils.toJsonString(
                    new TextContent("Query executed successfully. Rows affected: " + rowsAffected, "text"));
        } catch (Exception e) {
            log.error("Error executing SQL '{}': {}", query, e.getMessage());
            return JsonUtils.toJsonString(new TextContent("Error executing SQL: " + e.getMessage(), "text"));
        }
    }

    // 实现 SQL 注入检查逻辑
    private boolean isValidSqlQuery(String query) {
        return query != null && !query.toLowerCase().contains("drop")
                && !query.toLowerCase().contains("delete");
    }
}