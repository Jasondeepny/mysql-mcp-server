package com.zcckj.mcp.mysql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DbTool implements Serializable {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
}