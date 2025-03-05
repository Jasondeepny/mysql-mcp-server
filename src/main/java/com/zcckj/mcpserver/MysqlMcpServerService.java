package com.zcckj.mcpserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MysqlMcpServerService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataBaseConfig databaseConfig;

    @Tool(name = "list resource", description = "List the available resources"

    )
    public List<Resource> listResources() {
        try {
            List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);

            return tables.stream()
                    .map(table -> Resource.builder()
                            .uri("mysql://" + table + "/data")
                            .name("Table: " + table)
                            .mimeType("text/plain")
                            .description("Data in table: " + table)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to list resources", e);
            return Collections.emptyList();
        }
    }

    @Tool(name = "read_resource", description = "Read a resource from the MySQL server")
    public String readResource(@ToolParam(description = "mysql://table/data") String uri) {
        if (!uri.startsWith("mysql://")) {
            throw new IllegalArgumentException("Invalid URI scheme: " + uri);
        }
        String table = uri.substring(8).split("/")[0];
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM " + table + " LIMIT 100");
            if (rows.isEmpty()) {
                return "";
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
            return String.join("\n", result);
        } catch (Exception e) {
            log.error("Database error reading resource {}", uri, e);
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Tool(name = "list_tools", description = "List the available tools")
    public List<DbTool> listTools() {
        return Collections.singletonList(
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
    }

    @Tool(name = "execute_sql", description = "Execute an SQL query on the MySQL server")
    public TextContent executeSql(@ToolParam(description = "sql str") String query) {
        try {
            if (query.trim().toUpperCase().startsWith("SHOW TABLES")) {
                List<String> tables = jdbcTemplate.queryForList(query, String.class);
                String result = "Tables_in_" + databaseConfig.getDatabase() + "\n" +
                        String.join("\n", tables);
                return new TextContent(result, "text");
            } else if (query.trim().toUpperCase().startsWith("SELECT")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
                if (rows.isEmpty()) {
                    return new TextContent("", "text");
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
                return new TextContent(String.join("\n", result), "text");
            } else {
                int rowsAffected = jdbcTemplate.update(query);
                return new TextContent("Query executed successfully. Rows affected: " + rowsAffected, "text");
            }
        } catch (Exception e) {
            log.error("Error executing SQL '{}': {}", query, e.getMessage());
            return new TextContent("Error executing query: " + e.getMessage(), "text");
        }
    }
}