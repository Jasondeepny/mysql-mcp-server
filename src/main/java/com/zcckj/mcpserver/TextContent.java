package com.zcckj.mcpserver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Zyy
 * @date 2025/3/5
 * @description
 */

@AllArgsConstructor
@Data
public class TextContent implements Serializable {

    private String text;

    private String type;
}
