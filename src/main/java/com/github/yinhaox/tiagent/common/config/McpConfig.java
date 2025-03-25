package com.github.yinhaox.tiagent.common.config;

import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.transport.ServerParameters;
import org.springframework.ai.mcp.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class McpConfig {
    @Bean
    @ConditionalOnProperty(prefix = "mcp.sqlite", name = "enable", havingValue = "true")
    public ToolCallbackProvider toolCallbackProvider(McpSyncClient mcpClient) {
        List<McpFunctionCallback> mcpFunctionCallbacks = mcpClient.listTools(null).tools().stream().map(tool -> new McpFunctionCallback(mcpClient, tool)).toList();
        return ToolCallbackProvider.from(mcpFunctionCallbacks);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "mcp.sqlite", name = "enable", havingValue = "true")
    public McpSyncClient mcpClient(@Value("${mcp.sqlite.path}") String path) {
        System.out.println("sqlite path: " + path);
        var stdioParams = ServerParameters.builder("uvx").args("mcp-server-sqlite", "--db-path", path).build();
        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams)).requestTimeout(Duration.ofSeconds(10)).build();
        mcpClient.initialize();
        return mcpClient;
    }

    @Bean
    public ChatClientCustomizer chatClientCustomizer() {
        return builder -> builder.defaultOptions(OpenAiChatOptions.builder().temperature(0.6).topP(0.95).build());
    }
}
