package org.fpwei.line.server.controller;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fpwei.line.server.annotation.Command;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@LineMessageHandler
public class LineController implements ApplicationContextAware {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    private ApplicationContext applicationContext;
    private Reflections ref = new Reflections("org.fpwei.line.server.handler");

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String text = event.getMessage().getText().trim();

        handleCommand(text, event.getReplyToken());
    }


    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String text = event.getPostbackContent().getData();

        if ("null".equals(text)) {
            return;
        }

        handleCommand(text, event.getReplyToken());
    }


    private void handleCommand(String text, String replyToken) {
        int splitIndex = text.indexOf(" ");
        String command = (splitIndex == -1) ? text : text.substring(0, splitIndex);
        String parameter = (splitIndex == -1) ? "" : text.substring(splitIndex + 1);


        Optional<Class<?>> clazz = ref.getTypesAnnotatedWith(Command.class).parallelStream()
                .filter(c -> StringUtils.equalsAnyIgnoreCase(command, c.getAnnotation(Command.class).value()))
                .findAny();

        if (clazz.isPresent()) {
            Object obj = applicationContext.getBean(clazz.get());
            if (obj instanceof org.fpwei.line.server.handler.Command) {
                Message message = ((org.fpwei.line.server.handler.Command) obj).execute(parameter);
                reply(replyToken, message);
            }
        }
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
