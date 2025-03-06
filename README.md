# mysql-mcp-server
mysql-mcp-server. java version



```json
{
  "mcpServers": {
    "mysql-mcp-server": {
      "command": "java",
      "args": [
        "-Dspring.datasource.url= *",
        "-Dspring.datasource.username= *",
        "-Dspring.datasource.password= *",
        "-jar",
        "/absolute/path/mysql-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```