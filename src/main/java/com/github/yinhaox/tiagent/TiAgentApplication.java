package com.github.yinhaox.tiagent;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TiAgentApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TiAgentApplication.class).web(WebApplicationType.NONE).run(args);
    }
}
