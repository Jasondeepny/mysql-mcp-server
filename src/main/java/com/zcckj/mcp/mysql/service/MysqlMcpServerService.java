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
import java.util.Arrays;
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

    @Tool(description = "List the available tables", name = "list_tables")
    public String listTables(@ToolParam(description = "database") String database) {
        try {
            List<String> tables = jdbcTemplate.queryForList(String.format("SHOW TABLES FROM %s", database),
                    String.class);
            if (tables.isEmpty()) {
                return JsonUtils.toJsonString(new TextContent("No tables found in database: " + database, "text"));
            }
            List<Resource> resources = tables.stream()
                    .map(table -> Resource.builder()
                            .uri("mysql://" + database + "/" + table)
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

    @Tool(description = "List the available databases", name = "list_databases")
    public String listDatabases() {
        try {
            List<String> databases = jdbcTemplate.queryForList("SHOW DATABASES", String.class);

            List<Resource> resources = databases.stream()
                    .map(database -> Resource.builder()
                            .uri("mysql://" + database + "/table")
                            .name("Database: " + database)
                            .mimeType("text/plain")
                            .description("Table in database: " + database)
                            .build())
                    .collect(Collectors.toList());
            return JsonUtils.toJsonString(resources);
        } catch (Exception e) {
            log.error("Failed to list resources", e);
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Tool(description = "Read a data resource from the table in the database", name = "read_resource")
    public String readResource(@ToolParam(description = "mysql://database/table") String uri) {
        if (!uri.startsWith("mysql://")) {
            log.error("Invalid URI scheme:{} ", uri);
            return JsonUtils.toJsonString(new TextContent("Invalid URI scheme: " + uri, "text"));
        }
        String[] parts = uri.substring("mysql://".length()).split("/");
        if (parts.length < 2) {
            log.error("Invalid URI format: {}", uri);
            return JsonUtils.toJsonString(new TextContent("Invalid URI format: " + uri, "text"));
        }
        String database = parts[0];
        String table = parts[1];
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    String.format("SELECT * FROM %s.%s ORDER BY id DESC LIMIT 100", database, table));
            if (rows.isEmpty()) {
                log.error("No data found in table: {}", table);
                return JsonUtils.toJsonString(new TextContent("No data found in table: " + table, "text"));
            }
            // 获取列名
            String headers = String.join("|", rows.get(0).keySet());
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

    @Tool(description = "List the available tools", name = "list_tools")
    public String listTools() {
        List<DbTool> dbTools = Arrays.asList(
                DbTool.builder().name("execute_sql")
                        .description("Execute an SQL query on the MySQL server")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "query", Map.of(
                                                "type", "string",
                                                "description", "The SQL query to execute")),
                                "required", List.of("query")))
                        .build(),
                DbTool.builder().name("list_tables")
                        .description("List all tables in the database")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "database", Map.of(
                                                "type", "string",
                                                "description", "The database to list tables from")),
                                "required", List.of("database")))
                        .build());
        return JsonUtils.toJsonString(dbTools);
    }

    @Tool(description = "Execute an SQL query on the MySQL server", name = "execute_sql")
    public String executeSql(@ToolParam(description = "sql str") String query) {
        try {
            if (!isValidSqlQuery(query)) {
                return JsonUtils.toJsonString(new TextContent("Invalid SQL query: " + query, "text"));
            }
            if (query.trim().toUpperCase().startsWith("SHOW TABLES")) {
                List<String> tables = jdbcTemplate.queryForList(query, String.class);
                String result = String.format("Tables_in_%s\n%s", databaseConfig.getDatabase(),
                        String.join("\n", tables));
                return JsonUtils.toJsonString(new TextContent(result, "text"));
            }
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
                if (rows.isEmpty()) {
                    return JsonUtils.toJsonString(new TextContent("No data found.", "text"));
                }
                String headers = String.join("|", rows.get(0).keySet());
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

    // 检查是否为有效的SQL查询
    private boolean isValidSqlQuery(String query) {
        // 判断是否为 sql
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        // 转换为小写进行检查
        String lowerQuery = query.toLowerCase().trim();
        // 检查是否以常见SQL关键字开头
        String[] validKeywords = {
                "select", "show", "desc", "describe", "explain",
                "insert", "update", "create", "alter", "grant",
                "use", "set"
        };
        return Arrays.stream(validKeywords)
                .anyMatch(keyword -> lowerQuery.startsWith(keyword + " "))
                && !lowerQuery.contains("drop")
                && !lowerQuery.contains("delete");
    }
}