# Mysql-Mcp-Server

## Project Features:
    -- mcp server for Java version
    -- Support for multiple data sources
    -- STDIO 
    -- springboot starter

```bash 
  ./mvnw clean package -Dmaven.test.skip=true
```

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