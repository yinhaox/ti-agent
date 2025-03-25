package com.github.yinhaox.tiagent.step.controller;

import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Lazy
@ShellComponent
public class ChatController {
    private final Terminal terminal;

    private final ChatClient chatClient;

    private final List<Message> inputHistory = new LinkedList<>();

    private final List<Message> outputHistory = new LinkedList<>();

    public ChatController(Terminal terminal, ChatClient.Builder builder, List<ToolCallbackProvider> toolCallbackProviders) {
        this.terminal = terminal;
        if (toolCallbackProviders.isEmpty()) {
            this.chatClient = builder.build();
        } else {
            this.chatClient = builder.clone().defaultTools(toolCallbackProviders.toArray(new ToolCallbackProvider[0])).build();
        }
    }

    @ShellMethod(key = "chat", value = "开始对话")
    public void chat(@ShellOption(help = "你要说的话") String input) {
        Attributes prvAttr = terminal.getAttributes();
        terminal.enterRawMode();
        try {
            print("AI> ");
            Prompt prompt = getPrompt(input);
            ChatClient.ChatClientRequestSpec clientRequestSpec = chatClient.prompt(prompt).user(input);
            StringBuilder sb = new StringBuilder();
            clientRequestSpec.stream().content().doOnNext(msg -> {
                sb.append(msg);
                print(msg);
            }).doOnError(err -> {
                log.warn("call error.", err);
            }).doOnComplete(() -> {
                outputHistory.add(new AssistantMessage(sb.toString()));
            }).blockLast();
            print("\n");
        } catch (Exception e) {
            print("\n");
            if (e instanceof WebClientResponseException.BadRequest badRequest) {
                print(String.format("[Response] %s\n", badRequest.getResponseBodyAsString()));
            }
            print(String.format("[Error] %s\n", e.getMessage()));
        } finally {
            terminal.setAttributes(prvAttr);
        }
    }

    private void print(String s) {
        terminal.writer().print(s);
        terminal.writer().flush();
    }

    private Prompt getPrompt(String input) {
        UserMessage userMessage = new UserMessage(input);
        List<Message> messages = Stream.of(inputHistory, outputHistory).flatMap(List::stream).toList();
        inputHistory.add(userMessage);
        return new Prompt(messages);
    }
}
