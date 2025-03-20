package com.github.yinhaox.tiagent.step.controller;

import org.jline.terminal.Terminal;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Lazy
@ShellComponent
public class ChatController {
    private final Terminal terminal;

    private final ChatClient chatClient;

    private final List<ToolCallbackProvider> toolCallbackProviders;

    private final List<Message> messages = new LinkedList<>();

    public ChatController(Terminal terminal, ChatClient.Builder builder, List<ToolCallbackProvider> toolCallbackProviders) {
        this.terminal = terminal;
        this.chatClient = builder.build();
        this.toolCallbackProviders = toolCallbackProviders;
    }

    @ShellMethod(key = "chat", value = "开始对话")
    public void chat(@ShellOption(help = "你要说的话") String input) {
        try {
            print("AI>\n");
            messages.add(new UserMessage(input));
            ChatClient.ChatClientRequestSpec clientRequestSpec = chatClient.prompt(new Prompt(messages)).user(input);
            if (!toolCallbackProviders.isEmpty()) {
                clientRequestSpec.tools(toolCallbackProviders.toArray(new ToolCallbackProvider[0]));
            }
            ChatClient.CallResponseSpec callResponseSpec = clientRequestSpec.call();
            ChatResponse chatResponse = callResponseSpec.chatResponse();
            if (chatResponse != null) {
                for (Generation result : chatResponse.getResults()) {
                    messages.add(result.getOutput());
                }
            }
            String message = Optional.ofNullable(chatResponse)
                    .map(ChatResponse::getResult)
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .orElse(null);
            print(message);
            print("\n");
        } catch (Exception e) {
            print("\n");
            if (e instanceof WebClientResponseException.BadRequest badRequest) {
                print(String.format("[Response] %s\n", badRequest.getResponseBodyAsString()));
            }
            print(String.format("[Error] %s\n", e.getMessage()));
        }
    }

    private void print(String s) {
        terminal.writer().print(s);
        terminal.writer().flush();
    }
}
