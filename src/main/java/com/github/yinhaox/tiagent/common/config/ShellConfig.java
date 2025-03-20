package com.github.yinhaox.tiagent.common.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig {
    @Bean
    public PromptProvider aiPromptProvider() {
        return () -> new AttributedString("User> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
