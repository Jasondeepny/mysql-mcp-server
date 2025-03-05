package com.zcckj.mcpserver;

import lombok.Builder;
import lombok.Data;

/**
 * MCP 资源类
 */
@Data
@Builder
public class Resource {
    // 资源 URI
    private String uri;

    // 资源名称
    private String name;

    // 资源类型
    private String mimeType;

    // 资源描述
    private String description;
}