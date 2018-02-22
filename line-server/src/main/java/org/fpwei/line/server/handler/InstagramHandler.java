package org.fpwei.line.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fpwei.line.core.dao.InstagramDao;
import org.fpwei.line.core.entity.Instagram;
import org.fpwei.line.server.annotation.Command;
import org.fpwei.line.server.enums.InstagramParameter;
import org.fpwei.line.server.enums.Parameter;
import org.fpwei.line.server.instagram.model.Graphql;
import org.fpwei.line.server.instagram.model.Node;
import org.fpwei.line.server.instagram.model.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Command({"IG"})
public class InstagramHandler extends AbstractCommandHandler {
    private static final String INSTAGRAM_BASE_URI = "https://www.instagram.com/";

    @Autowired
    private InstagramDao instagramDao;

    @Override
    protected Message execute(Map<Parameter, Object> parameterMap) {

        if (parameterMap.containsKey(InstagramParameter.ACCOUNT)) {
            String account = parameterMap.get(InstagramParameter.ACCOUNT).toString();
            User user = getAccountInfo(account);
            if (user == null) {
                return new TextMessage("無法取得 " + account + " 相關資料");
            } else {
                if (parameterMap.containsKey(InstagramParameter.COLLECTION)) {
                    List<Node> nodes = user.getTimelineImageNodes();
                    nodes.sort((n1, n2) -> n2.getEdgeMediaPreviewLike().getCount() - n1.getEdgeMediaPreviewLike().getCount());

                    if (nodes.size() > 10) {
                        nodes = nodes.subList(0, 10);
                    } else if (nodes.size() == 0) {
                        return new TextMessage("沒有公開貼文");
                    }


                    List<ImageCarouselColumn> columns = nodes.stream()
                            .map(node -> {
                                URIAction action = new URIAction(null, getNodeUrl(account, node.getShortcode()));
                                return new ImageCarouselColumn(node.getDisplayUrl(), action);
                            }).collect(Collectors.toList());

                    return new TemplateMessage(user.getFullName() + " 精選貼文", new ImageCarouselTemplate(columns));

                } else if (parameterMap.containsKey(InstagramParameter.RECENT)) {
                    List<Node> nodes = user.getTimelineImageNodes();

                    if (nodes.size() > 10) {
                        nodes = nodes.subList(0, 10);
                    } else if (nodes.size() == 0) {
                        return new TextMessage("沒有公開貼文");
                    }

                    List<ImageCarouselColumn> columns = nodes.stream()
                            .map(node -> {
                                URIAction action = new URIAction(null, getNodeUrl(account, node.getShortcode()));
                                return new ImageCarouselColumn(node.getDisplayUrl(), action);
                            }).collect(Collectors.toList());

                    return new TemplateMessage(user.getFullName() + " 最新貼文", new ImageCarouselTemplate(columns));
                } else {
                    recordSearchTimes(user);

                    String content = String.format("貼文：%d, 粉絲：%d\n%s", user.getEdgeOwnerToTimelineMedia().getCount(),
                            user.getEdgeFollowedBy().getCount(), user.getBiography());
                    if (content.length() > 60) {
                        content = content.substring(0, 57) + "...";
                    }
                    String title = StringUtils.isBlank(user.getFullName()) ? user.getUsername() : user.getFullName();
                    ButtonsTemplate template = new ButtonsTemplate(user.getProfilePicUrlHd(), title, content,
                            Arrays.asList(new PostbackAction("最新貼文", "IG " + account + " " + InstagramParameter.RECENT.getValue()),
                                    new PostbackAction("精選貼文", "IG " + account + " " + InstagramParameter.COLLECTION.getValue()),
                                    new URIAction("IG", INSTAGRAM_BASE_URI + account)));

                    return new TemplateMessage(user.getFullName() + " " + user.getBiography(), template);
                }
            }
        } else {

            List<User> users = new ArrayList<>();
            do {
                List<Instagram> instagramList = instagramDao.findInstagramByRandom(10 - users.size());
                users.addAll(instagramList.parallelStream()
                        .map(i -> getAccountInfo(i.getAccount()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            } while (users.size() < 10);


            List<CarouselColumn> columns = users.parallelStream()
                    .map(user -> {
                        String content = String.format("貼文：%d, 粉絲：%d\n%s", user.getEdgeOwnerToTimelineMedia().getCount(),
                                user.getEdgeFollowedBy().getCount(), user.getBiography());
                        if (content.length() > 60) {
                            content = content.substring(0, 57) + "...";
                        }

                        String account = user.getUsername();
                        String title = StringUtils.isBlank(user.getFullName()) ? user.getUsername() : user.getFullName();
                        return new CarouselColumn(user.getProfilePicUrlHd(), title, content,
                                Arrays.asList(new PostbackAction("最新貼文", "IG " + account + " " + InstagramParameter.RECENT.getValue()),
                                        new PostbackAction("精選貼文", "IG " + account + " " + InstagramParameter.COLLECTION.getValue()),
                                        new URIAction("IG", INSTAGRAM_BASE_URI + account)));
                    })
                    .collect(Collectors.toList());

            return new TemplateMessage("隨選 IG 正妹", new CarouselTemplate(columns));
        }
    }

    private void recordSearchTimes(User user) {
        if (!user.isPrivate() && user.getEdgeOwnerToTimelineMedia().getCount() > 100 && user.getEdgeFollowedBy().getCount() > 5000) {
            String account = user.getUsername();
            if (instagramDao.existsByAccount(account)) {
                Instagram instagram = instagramDao.findByAccount(account);
                instagram.setPriority(instagram.getPriority() + 1);
                instagramDao.save(instagram);
            } else {
                Instagram instagram = new Instagram();
                instagram.setAccount(account);
                instagram.setStatus(0);
                instagramDao.save(instagram);
            }
        }
    }

    private User getAccountInfo(String account) {
        Document document;
        try {
            document = Jsoup.connect(INSTAGRAM_BASE_URI + account).get();
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return null;
        }

        String html = document.toString();

        int index = html.indexOf("\"entry_data\":");
        String json = html.substring(index + "\"entry_data\":".length(), html.indexOf(",\"gatekeepers\"", index));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node;
        try {
            node = mapper.readTree(json);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return null;
        }

        Iterator<JsonNode> iterator = node.get("ProfilePage").elements();
        if (iterator.hasNext()) {
            JsonNode graphqlNode = iterator.next().get("graphql");
            if (graphqlNode == null) {
                log.warn("Parse \"graphql\" error");
                return null;
            }
            Graphql graphql;
            try {
                graphql = mapper.readValue(graphqlNode.toString(), Graphql.class);
            } catch (IOException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return null;
            }

            return graphql.getUser();
        } else {
            log.warn("Parse \"graphql\" error");
            return null;
        }
    }


    @Override
    protected Parameter getParameter(String value) {
        return InstagramParameter.getParameter(value);
    }

    @Override
    protected Parameter getDefaultParameter() {
        return InstagramParameter.ACCOUNT;
    }

    private String getNodeUrl(String account, String shortCode) {
        return String.format("https://www.instagram.com/p/%s/?taken-by=%s", shortCode, account);
    }
}
