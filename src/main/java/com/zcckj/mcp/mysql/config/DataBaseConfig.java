package com.zcckj.mcp.mysql.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
public class DataBaseConfig {
    private String url;
    private String username;
    private String password;
    private String database;
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        log.info("------- spring datasource url: {}", url);
        // 检查 JDBC URL 是否正确
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("JDBC URL is required.");
        }
        return new HikariDataSource(config);
    }
}