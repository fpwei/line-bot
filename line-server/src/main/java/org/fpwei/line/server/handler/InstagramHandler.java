package org.fpwei.line.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fpwei.line.server.annotation.Command;
import org.fpwei.line.server.enums.InstagramParameter;
import org.fpwei.line.server.enums.Parameter;
import org.fpwei.line.server.instagram.model.Edge;
import org.fpwei.line.server.instagram.model.Graphql;
import org.fpwei.line.server.instagram.model.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Command({"IG"})
public class InstagramHandler extends AbstractCommandHandler {
    private static final String INSTAGRAM_BASE_URI = "https://www.instagram.com/";

    @Override
    protected Message execute(Map<Parameter, Object> parameterMap) {

        if (parameterMap.containsKey(InstagramParameter.ACCOUNT)) {
            String account = parameterMap.get(InstagramParameter.ACCOUNT).toString();
            User user = getAccountInfo(account);
            if (user == null) {
                return new TextMessage("無法取得 " + account + " 相關資料");
            } else {
                if (parameterMap.containsKey(InstagramParameter.RECENT)) {
                    List<Edge> edges = user.getEdgeOwnerToTimelineMedia().getEdges().stream()
                            .filter(edge -> !edge.getNode().isVideo())
                            .sorted((e1, e2) -> e2.getNode().getEdgeMediaPreviewLike().getCount() - e1.getNode().getEdgeMediaPreviewLike().getCount())
                            .collect(Collectors.toList());

                    if (edges.size() > 10) {
                        edges = edges.subList(0, 10);
                    }
                    List<ImageCarouselColumn> columns = edges.stream()
                            .map(edge -> {
                                PostbackAction action = new PostbackAction(null, "null");
                                return new ImageCarouselColumn(edge.getNode().getDisplayUrl(), action);
                            }).collect(Collectors.toList());

                    return new TemplateMessage(user.getFullName() + " top 10", new ImageCarouselTemplate(columns));

                } else {
                    String content = String.format("貼文：%d, 粉絲：%d\n%s", user.getEdgeOwnerToTimelineMedia().getCount(),
                            user.getEdgeFollowedBy().getCount(), user.getBiography());
                    if (content.length() > 60) {
                        content = content.substring(0, 57) + "...";
                    }
                    ButtonsTemplate template = new ButtonsTemplate(user.getProfilePicUrlHd(), user.getFullName(), content,
                            Arrays.asList(new PostbackAction("Top 10 Post", "IG " + account + " " + InstagramParameter.RECENT.getValue()),
                                    new URIAction("IG", INSTAGRAM_BASE_URI + account)));

                    return new TemplateMessage(user.getFullName() + " " + user.getBiography(), template);
                }
            }
        }


        return null;
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
            Graphql graphql = null;
            try {
                graphql = mapper.readValue(graphqlNode.toString(), Graphql.class);
            } catch (IOException e) {
                log.error(ExceptionUtils.getStackTrace(e));
                return null;
            }

            return graphql.getUser();
//            List<Edge> edges = user.getEdgeOwnerToTimelineMedia().getEdges();
//            edges.sort((e1, e2) -> e2.getNode().getEdgeMediaPreviewLike().getCount() - e1.getNode().getEdgeMediaPreviewLike().getCount());
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
}
