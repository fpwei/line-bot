package org.fpwei.line.server.controller;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fpwei.line.core.dao.ImageDao;
import org.fpwei.line.core.entity.Image;
import org.fpwei.line.server.annotation.Command;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@LineMessageHandler
public class LineController implements ApplicationContextAware {
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private ImageDao imageDao;

    private ApplicationContext applicationContext;
    private Reflections ref = new Reflections("org.fpwei.line.server.handler");


    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String text = event.getMessage().getText().trim();

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
                reply(event.getReplyToken(), message);
            }
        }
    }


    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String data = event.getPostbackContent().getData();

        if ("null".equals(data)) {
            return;
        }

        int postId;
        int page = 1;
        if (StringUtils.isNumeric(data)) {
            postId = Integer.valueOf(data);

        } else {
            String[] arr = data.split(",", 2);
            postId = Integer.valueOf(arr[0]);
            page = Integer.valueOf(arr[1]);
        }

        List<Image> images = imageDao.findImagesByPostId(postId, new PageRequest((page - 1), DEFAULT_PAGE_SIZE)); //page base index = 0

        List<ImageCarouselColumn> columns = images.stream()
                .map(image -> {
                    String url = generateImageUrl(image.getPath());
                    Action action = new PostbackAction(null, "null");
                    return new ImageCarouselColumn(url, action);
                })
                .collect(Collectors.toList());

        //if exist more images, set get next page action in last column
        int totalPage = (imageDao.countByPostId(postId) + DEFAULT_PAGE_SIZE - 1) / DEFAULT_PAGE_SIZE;
        if (totalPage > page) {
            Image image = images.get(DEFAULT_PAGE_SIZE - 1);

            String url = generateImageUrl(image.getPath());
            Action action = new PostbackAction("Next Page", String.format("%d,%d", postId, page + 1));
            ImageCarouselColumn lastColumn = new ImageCarouselColumn(url, action);

            columns.set(DEFAULT_PAGE_SIZE - 1, lastColumn);
        }

        ImageCarouselTemplate template = new ImageCarouselTemplate(columns);

        String postTitle = images.get(0).getPost().getTitle();

        reply(event.getReplyToken(), new TemplateMessage(String.format("%s(%d/%d)", postTitle, page, totalPage), template));

    }


    private String generateImageUrl(String imagePath) {
        String path = imagePath.replace("/data/img/", "").replace(".jpg", "");

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .pathSegment("image", path)
                .build()
                .toUriString();
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
