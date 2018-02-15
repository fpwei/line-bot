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
import org.fpwei.line.core.dao.PostDao;
import org.fpwei.line.core.entity.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@LineMessageHandler
public class LineController {
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private PostDao postDao;

    @Autowired
    private ImageDao imageDao;


    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String text = event.getMessage().getText();

        if (StringUtils.equalsAnyIgnoreCase(text, "beauty", "表特")) {
            List<Integer> postIds = postDao.queryPostIdByRandom(10);

            List<Image> images = postIds.stream()
                    .map(i -> imageDao.findFirstByPostId(i))
                    .collect(Collectors.toList());


            List<ImageCarouselColumn> columns = images.stream()
                    .map(image -> {
                        String url = generateImageUrl(image.getPath());
                        Action action = new PostbackAction("View More", String.valueOf(image.getPost().getId()));
                        return new ImageCarouselColumn(url, action);
                    })
                    .collect(Collectors.toList());


            ImageCarouselTemplate template = new ImageCarouselTemplate(columns);

            reply(event.getReplyToken(), new TemplateMessage("隨選表特正妹", template));
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
}
